package com.dzytsiuk.pdfreportservice.service.impl;

import com.dzytsiuk.pdfreportservice.entity.*;
import com.dzytsiuk.pdfreportservice.exception.UnsupportedReportTypeException;
import com.dzytsiuk.pdfreportservice.service.MovieService;
import com.dzytsiuk.pdfreportservice.service.ReportGeneratorService;
import com.dzytsiuk.pdfreportservice.service.UserService;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DefaultReportGeneratorService implements ReportGeneratorService {
    private static final int SMALL_FONT_SIZE = 12;
    private static final int LARGE_FONT_SIZE = 16;
    private static final String ARIALUNI_FONT = "arialuni.ttf";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MovieService movieService;
    private final UserService userService;
    private final DateTimeFormatter dateTimeFormatter;
    @Value("${fetch.size.movie}")
    private Integer moviesFetchSize;

    public DefaultReportGeneratorService(MovieService movieService, UserService userService, DateTimeFormatter dateTimeFormatter) {
        this.movieService = movieService;
        this.userService = userService;
        this.dateTimeFormatter = dateTimeFormatter;
    }


    @Override
    public InputStream generateReport(ReportRequest reportRequest) {
        reportRequest.setReportStatus(ReportStatus.IN_PROGRESS);
        log.info("Start generating report {}", reportRequest);
        ReportType reportType = reportRequest.getReportType();
        InputStream resultInputStream;
        String id = reportRequest.getId();
        if (reportType == ReportType.ADDED_DURING_PERIOD || reportType == ReportType.ALL_MOVIES) {
            ReportParameter reportParameter = reportRequest.getReportParameter();
            resultInputStream = generateMovieReport(reportParameter == null ? new ReportParameter() : reportParameter, id, reportType.getName());
        } else if (reportType == ReportType.TOP_ACTIVE_USERS) {
            resultInputStream = generateUserReport(id);
        } else {
            throw new UnsupportedReportTypeException("Report type " + reportType + " unsupported");
        }
        log.info("Finish generating report {}", reportRequest);
        return resultInputStream;
    }


    @SneakyThrows
    private InputStream generateUserReport(String reportId) {
        String reportTypeName = ReportType.TOP_ACTIVE_USERS.getName();
        List<User> topUsers = userService.getTopUsers();
        String fileName = getFileName(reportId);
        Document document = new Document(PageSize.A4);
        try (FileOutputStream stream = new FileOutputStream(fileName)) {
            try {
                Font font = getFontAndPreprocess(reportId, reportTypeName, document, stream);
                for (User topUser : topUsers) {
                    addUser(topUser, document, font);
                }
            } finally {
                document.close();
            }
        }
        return new FileInputStream(fileName);
    }

    private void addUser(User user, Document document, Font font) throws DocumentException {
        document.add(new Paragraph("User " + user.getId(), font));
        document.add(new Paragraph("Email  " + user.getEmail(), font));
        document.add(new Paragraph("Reviews Count  " + user.getReviewCount(), font));
        document.add(new Paragraph("Average rate  " + user.getAverageRating(), font));
    }

    @SneakyThrows
    private InputStream generateMovieReport(ReportParameter reportParameter, String reportId, String reportTypeName) {
        Document document = new Document(PageSize.A4);
        String fileName = getFileName(reportId);
        try (FileOutputStream stream = new FileOutputStream(fileName)) {
            try {
                Font font = getFontAndPreprocess(reportId, reportTypeName, document, stream);
                List<ReportMovie> movies;
                int page = 1;
                for (; (movies = movieService.getMovies(page, moviesFetchSize, reportParameter)).size() == moviesFetchSize; page++) {
                    addMovies(movies, document, font);
                }
                //process last portion of data
                addMovies(movies, document, font);

            } finally {
                document.close();
            }
        }
        return new FileInputStream(fileName);
    }

    private void addMovies(List<ReportMovie> movies, Document document, Font font) throws DocumentException {
        for (ReportMovie movie : movies) {
            addMovie(movie, document, font);
        }
    }

    private void addMovie(ReportMovie movie, Document document, Font font) throws DocumentException {
        document.add(new Paragraph("Movie " + movie.getId(), font));
        document.add(new Paragraph(movie.getNameNative() + " ( " + movie.getNameRussian() + " )", font));
        document.add(new Paragraph(movie.getDescription(), font));
        document.add(new Paragraph("Genres: " + movie.getGenres(), font));
        document.add(new Paragraph("Price: " + movie.getPrice() + " UAH", font));
        document.add(new Paragraph("Add Date: " + dateTimeFormatter.format(movie.getAddDate()), font));
        document.add(new Paragraph("Last Modified Date: " + dateTimeFormatter.format(movie.getLastModifiedDate()), font));
        document.add(new Paragraph("Rating: " + movie.getRating(), font));
        document.add(new Paragraph("Reviews Count: " + movie.getReviewCount(), font));
        document.add(Chunk.NEWLINE);
    }

    private Font getFontAndPreprocess(String reportId, String reportTypeName, Document document, FileOutputStream stream) throws DocumentException, IOException {
        Font font = getBaseFont();
        PdfWriter.getInstance(document, stream);
        document.open();
        Paragraph headerParagraph = getHeaderParagraph(reportId, reportTypeName, font);
        addHeader(document, font, headerParagraph);
        return font;
    }

    private void addHeader(Document document, Font font, Paragraph headerParagraph) throws DocumentException {
        document.add(headerParagraph);
        document.add(Chunk.NEWLINE);
        font.setSize(SMALL_FONT_SIZE);
    }

    private Paragraph getHeaderParagraph(String reportId, String reportTypeName, Font font) {
        font.setSize(LARGE_FONT_SIZE);
        Paragraph paragraph = new Paragraph("Report " + reportId + " " + reportTypeName + " ", font);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(10);
        return paragraph;
    }

    private Font getBaseFont() throws IOException, DocumentException {
        BaseFont baseFont = BaseFont.createFont(ARIALUNI_FONT, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        return new Font(baseFont, SMALL_FONT_SIZE);
    }

    private String getFileName(String reportId) {
        return "tmp/" + reportId + ".pdf";
    }

}
