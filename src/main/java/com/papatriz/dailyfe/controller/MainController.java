package com.papatriz.dailyfe.controller;

import com.papatriz.dailyfe.dto.ActivityDto;
import com.papatriz.dailyfe.dto.UserDto;
import lombok.Getter;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Map;

@Getter
@Scope(value = "session")
@Component(value = "mainController")
@ELBeanName(value = "mainController")
public class MainController {

    private UserDto userData;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${restapi.root}")
    private String restApiRoot;
    private final Map<String, String> restEndPoints =
            Map.of("USER_DATA","/user","ACTIVITIES","/activity");

    @PostConstruct
    private void init() {
        var url = restApiRoot + restEndPoints.get("USER_DATA");
        userData = restTemplate.getForObject(url, UserDto.class);
        logger.info("Get user data from remote server: "+userData);
        logger.info("Rest API root: "+restApiRoot);
    }

    public ActivityDto[] getActivities() {
        var url = restApiRoot + restEndPoints.get("ACTIVITIES");

        return restTemplate.getForObject(url, ActivityDto[].class);
    }
}
