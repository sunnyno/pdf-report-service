package com.dzytsiuk.pdfreportservice.service;

import com.dzytsiuk.pdfreportservice.entity.ReportRequest;

import java.util.List;

public interface RequestProcessor {
    void processReportRequests(List<ReportRequest> reportRequests);
}
