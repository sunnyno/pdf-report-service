package com.dzytsiuk.pdfreportservice.service;

import com.dzytsiuk.pdfreportservice.entity.ReportRequest;
import lombok.SneakyThrows;

import java.io.InputStream;

public interface FtpService {
    @SneakyThrows
    void saveAndEnrichWithUrl(InputStream inputStream, ReportRequest reportRequest);
}
