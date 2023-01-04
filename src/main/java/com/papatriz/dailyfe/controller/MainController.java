package com.papatriz.dailyfe.controller;

import com.papatriz.dailyfe.api.ApiAction;
import com.papatriz.dailyfe.api.RestApi;
import com.papatriz.dailyfe.dto.ActivityDto;
import com.papatriz.dailyfe.dto.UserDto;
import lombok.Getter;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
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
    @Autowired
    private RestApi api;
    private LocalDate startDate;

    @PostConstruct
    private void init() {
        var url = api.getUrlFor(ApiAction.GetUserData);
        userData = restTemplate.getForObject(url, UserDto.class);
        startDate = LocalDate.now();
        logger.info("Get user data from remote server: "+userData);
    }

    public ActivityDto[] getActivities() {
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

    public void onRowEdit(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowEdit");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Activity "+ event.getObject().id() +" edited", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    public void onRowCancel(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowCancel");
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Activity "+ event.getObject().id() +" edited", "");
        FacesContext.getCurrentInstance().addMessage(null, msg);

    }

    public boolean getRandomBool() { // for test purposes only
        var time = System.nanoTime();
        return time % 2 == 0;
    }
}
