package com.dzytsiuk.pdfreportservice.entity;

public enum ReportFormat {
    PDF("pdf");
    private String name;

    ReportFormat(String name) {
        this.name = name;
    }

    public static ReportFormat getReportFormatFromName(String name) {
        for (ReportFormat reportFormat : ReportFormat.values()) {
            if (name.equalsIgnoreCase(reportFormat.getName())) {
                return reportFormat;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
