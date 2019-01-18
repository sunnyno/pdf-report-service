package com.dzytsiuk.pdfreportservice.service.impl;

import com.dzytsiuk.pdfreportservice.entity.User;
import com.dzytsiuk.pdfreportservice.service.UserService;
import com.dzytsiuk.pdfreportservice.service.client.MovielandClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DefaultUserService implements UserService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MovielandClient movielandClient;

    public DefaultUserService(MovielandClient movielandClient) {
        this.movielandClient = movielandClient;
    }

    public List<User> fallback() {
        return new ArrayList<>();
    }

   // @HystrixCommand(fallbackMethod = "fallback")
    @Override
    public List<User> getTopUsers() {
        log.info("Sending request to get top users");
        List<User> users = movielandClient.getUsers();
        log.info("Users {} received", users);
        return users;
    }

}
