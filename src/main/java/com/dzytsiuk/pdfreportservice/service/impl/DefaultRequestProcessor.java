package com.dzytsiuk.pdfreportservice.service.impl;


import com.dzytsiuk.pdfreportservice.entity.ReportRequest;
import com.dzytsiuk.pdfreportservice.entity.ReportStatus;
import com.dzytsiuk.pdfreportservice.service.FtpService;
import com.dzytsiuk.pdfreportservice.service.ReportGeneratorService;
import com.dzytsiuk.pdfreportservice.service.RequestProcessor;
import com.dzytsiuk.pdfreportservice.service.postprocessor.ReplyMessagePostProcessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;

import javax.jms.Queue;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
public class DefaultRequestProcessor implements RequestProcessor {
    private static final MessagePostProcessor REPLY_MESSAGE_POST_PROCESSOR = new ReplyMessagePostProcessor();
    private static final TypeReference<List<ReportRequest>> TO_VALUE_TYPE_REF = new TypeReference<List<ReportRequest>>() {
    };
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final FtpService ftpService;
    private final ExecutorService executor;
    private final ObjectMapper objectMapper;
    private final ReportGeneratorService reportGeneratorService;
    private final JmsTemplate jmsTemplate;
    private final Queue processingQueue;


    public DefaultRequestProcessor(FtpService ftpService, ExecutorService executor, ObjectMapper objectMapper, ReportGeneratorService reportGeneratorService, JmsTemplate jmsTemplate, Queue processingQueue) {
        this.ftpService = ftpService;
        this.executor = executor;
        this.objectMapper = objectMapper;
        this.reportGeneratorService = reportGeneratorService;
        this.jmsTemplate = jmsTemplate;
        this.processingQueue = processingQueue;
    }

    @JmsListener(destination = "processing", selector = "format = 'pdf'")
    @Override
    public void processReportRequests(List<ReportRequest> reportRequests) {
        List<ReportRequest> list = objectMapper.convertValue(reportRequests,TO_VALUE_TYPE_REF);
        for (ReportRequest reportRequest : list) {
            CompletableFuture.supplyAsync(() -> generateReport(reportRequest), executor)
                    .thenAccept(inputStream -> saveToFtp(inputStream, reportRequest))
                    .thenRun(() -> notifyFinished(reportRequest))
                    .whenComplete((u, ex) -> handleException(ex, reportRequest));
            log.info("Report {} is put into running queue", reportRequest);
        }
    }

    private void handleException(Throwable ex, ReportRequest reportRequest) {
        if (ex != null) {
            throw new RuntimeException("Error processing request " + reportRequest, ex);
        }
    }

    private void notifyFinished(ReportRequest reportRequest) {
        reportRequest.setReportStatus(ReportStatus.GENERATED);
        jmsTemplate.convertAndSend(processingQueue, reportRequest, REPLY_MESSAGE_POST_PROCESSOR);
    }

    private InputStream generateReport(ReportRequest reportRequest) {
        reportRequest.setReportStatus(ReportStatus.IN_PROGRESS);
        jmsTemplate.convertAndSend(processingQueue, reportRequest, REPLY_MESSAGE_POST_PROCESSOR);
        return reportGeneratorService.generateReport(reportRequest);
    }

    private void saveToFtp(InputStream inputStream, ReportRequest reportRequest) {
        String id = reportRequest.getId();
        log.info("Start saving report {} to ftp", id);
        ftpService.saveAndEnrichWithUrl(inputStream, reportRequest);
        log.info("Finish saving report {} to ftp. Link: {}", id, reportRequest.getFtpUrl());
    }

}
