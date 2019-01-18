package com.dzytsiuk.pdfreportservice.service.impl;

import com.dzytsiuk.pdfreportservice.entity.ReportMovie;
import com.dzytsiuk.pdfreportservice.entity.ReportParameter;
import com.dzytsiuk.pdfreportservice.service.MovieService;
import com.dzytsiuk.pdfreportservice.service.client.MovielandClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultMovieService implements MovieService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MovielandClient movielandClient;

    public DefaultMovieService(MovielandClient movielandClient) {
        this.movielandClient = movielandClient;
    }

    public List<ReportMovie> fallback(){
        return new ArrayList<>();
    }

    //@HystrixCommand(fallbackMethod = "fallback")
    @Override
    public List<ReportMovie>  getMovies(Integer page, Integer count, ReportParameter reportParameter) {
        log.info("Start sending request to get {} movies on page {} with parameters {}",
                count, page, reportParameter);
        List<ReportMovie> movies = movielandClient.getMovies(page, count, reportParameter.getFromDate(), reportParameter.getToDate());
        log.info("Movies on page {} with parameters {} received {}", page, reportParameter, movies);
        return movies;
    }
}
