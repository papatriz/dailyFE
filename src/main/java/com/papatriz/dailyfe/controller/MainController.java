package com.papatriz.dailyfe.controller;

import com.papatriz.dailyfe.api.ApiAction;
import com.papatriz.dailyfe.api.RestApi;
import com.papatriz.dailyfe.dto.ActivityDto;
import com.papatriz.dailyfe.dto.UserDto;
import lombok.Getter;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Scope(value = "session")
@Component(value = "mainController")
@ELBeanName(value = "mainController")
public class MainController {

    private UserDto userData;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private RestApi api;
    private LocalDate startDate;

    private List<ActivityDto> activityList;
    private ActivityDto newActivity = new ActivityDto();

    @PostConstruct
    private void init() {
        var url = api.getUrlFor(ApiAction.GetUserData);
        userData = restTemplate.getForObject(url, UserDto.class);
        startDate = LocalDate.now();
        activityList = Arrays.stream(getActivities()).toList();
        logger.info("Get user data from remote server: "+userData);
    }

    private ActivityDto[] getActivities() {
        var url = api.getUrlFor(ApiAction.GetActivityList);

        return restTemplate.getForObject(url, ActivityDto[].class);
    }

    public String getDateRepresent(int dateShift) {
       var targetDate = startDate.plusDays(dateShift);
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
       return targetDate.format(formatter);
    }

    public boolean isActivityComplete(long activityId, int dateShift) {
        var urlTemplate = api.getUrlFor(ApiAction.IsActivityComplete);

        var targetDate = startDate.plusDays(dateShift);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        var url = urlTemplate.replace("{id}", String.valueOf(activityId))
                                     .replace("{date}", targetDate.format(formatter));

        logger.info("Check activity completion by URL: "+url);
        return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    }

    public void addActivity() {
        logger.info("On Add Activity: %s".formatted(newActivity));

        PrimeFaces.current().ajax().update("checkForm");
        PrimeFaces.current().ajax().update("activitiesForm");

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Adding activity", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowEdit(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowEdit: %s".formatted(event.getObject()));
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(event.getObject(), headers);
        var url=api.getUrlFor(ApiAction.EditActivity);
        var response = restTemplate.postForObject(url, request, String.class);
        logger.info("Response: "+response);
      //  activityList = Arrays.stream(getActivities()).toList();

        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Activity "+ response +" edited", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowCancel");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Cancel edit", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public boolean getRandomBool() { // for test purposes only
        var time = System.nanoTime();
        return time % 2 == 0;
    }
}
