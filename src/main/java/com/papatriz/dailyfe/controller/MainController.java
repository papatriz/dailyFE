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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
            Map.of("USER_DATA","/user","ACTIVITIES","/activity", "CHECK_ACTIVITY", "/activity/{id}/{date}");
    private LocalDate startDate;

    @PostConstruct
    private void init() {
        var url = restApiRoot + restEndPoints.get("USER_DATA");
        userData = restTemplate.getForObject(url, UserDto.class);
        startDate = LocalDate.now();
        logger.info("Get user data from remote server: "+userData);
        logger.info("Rest API root: "+restApiRoot);
    }

    public ActivityDto[] getActivities() {
        var url = restApiRoot + restEndPoints.get("ACTIVITIES");

        return restTemplate.getForObject(url, ActivityDto[].class);
    }

    public String getDateRepresent(int dateShift) {
       var targetDate = startDate.plusDays(dateShift);
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
       return targetDate.format(formatter);
    }

    public boolean isActivityComplete(long activityId, int dateShift) {
        var urlTemplate = restApiRoot + restEndPoints.get("CHECK_ACTIVITY");

        var targetDate = startDate.plusDays(dateShift);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        var url = urlTemplate.replace("{id}", String.valueOf(activityId))
                                     .replace("{date}", targetDate.format(formatter));

        logger.info("Check activity completion by URL: "+url);
        return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    }

    public boolean getRandomBool() { // for test purposes only
        var time = System.nanoTime();
        return time % 2 == 0;
    }
}
