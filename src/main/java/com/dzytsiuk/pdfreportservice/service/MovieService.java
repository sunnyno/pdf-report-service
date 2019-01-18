package com.dzytsiuk.pdfreportservice.service;

import com.dzytsiuk.pdfreportservice.entity.ReportMovie;
import com.dzytsiuk.pdfreportservice.entity.ReportParameter;

import java.util.List;

public interface MovieService {
    List<ReportMovie> getMovies(Integer page, Integer count, ReportParameter reportParameter);
}
