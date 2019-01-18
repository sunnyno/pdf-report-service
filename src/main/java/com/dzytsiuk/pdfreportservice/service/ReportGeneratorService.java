package com.dzytsiuk.pdfreportservice.service;

import com.dzytsiuk.pdfreportservice.entity.ReportRequest;

import java.io.InputStream;

public interface ReportGeneratorService {
    InputStream generateReport(ReportRequest reportRequest);
}
