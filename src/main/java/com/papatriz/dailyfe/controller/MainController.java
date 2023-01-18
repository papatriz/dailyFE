package com.papatriz.dailyfe.controller;

import com.papatriz.dailyfe.api.ApiAction;
import com.papatriz.dailyfe.api.RestApi;
import com.papatriz.dailyfe.dto.ActivityDto;
import com.papatriz.dailyfe.dto.UserDto;
import lombok.Getter;
import lombok.Setter;
import org.ocpsoft.rewrite.el.ELBeanName;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

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
    @Setter
    private ActivityDto newActivity = new ActivityDto();
    private ActivityDto selectedActivity;

    @PostConstruct
    private void init() {
        var url = api.getUrlFor(ApiAction.GetUserData);
        userData = restTemplate.getForObject(url, UserDto.class);
        startDate = LocalDate.now();
        activityList = Arrays.stream(getActivities()).toList();
        newActivity.setWeight((short) 1);
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

        return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    }

    public void addActivity() {
        logger.info("On Add Activity: %s".formatted(newActivity));
        String response;

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        var request = new HttpEntity<>(newActivity, headers);
        var url=api.getUrlFor(ApiAction.SaveNewActivity);

        try {
            response = restTemplate.postForObject(url, request, String.class);
        }
        catch (HttpStatusCodeException e) {
            logger.info("Catch exception: "+e.getStatusCode()+" "+e.getResponseBodyAsString());
            showMessage("Error: \n"+e.getResponseBodyAsString().replace("\n","; "), true);
            return;
        }

        PrimeFaces.current().executeScript("PF('addDialog').hide();");
        newActivity = new ActivityDto();
        newActivity.setWeight((short) 1);

        activityList = Arrays.stream(getActivities()).toList();
        PrimeFaces.current().ajax().update("checkForm", "activitiesForm", "addForm");

        showMessage("Activity added");
        logger.info("Add activity response: "+response);
    }

    public void onRowEdit(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowEdit: %s".formatted(event.getObject()));
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var request = new HttpEntity<>(event.getObject(), headers);
        var url=api.getUrlFor(ApiAction.EditActivity);
        restTemplate.put(url, request);

        showMessage("Activity "+ event.getObject().getTitle() +" edited");
    }

    public void deleteSelectedActivity() {
        var urlTemplate = api.getUrlFor(ApiAction.DeleteActivity);
        var url = urlTemplate.replace("{id}", String.valueOf(selectedActivity.getId()));
        restTemplate.delete(url);
        activityList = Arrays.stream(getActivities()).toList();
        PrimeFaces.current().ajax().update("checkForm", "activitiesForm");
    }
    public void selectActivity(ActivityDto activity) {
        selectedActivity = activity;
    }

    public void onRowCancel(RowEditEvent<ActivityDto> event) {
        logger.info("OnRowCancel");
        showMessage("Editing canceled");
    }

    private void showMessage(String message, boolean... isError) {
        var isInfo = isError.length == 0;
        if (!isInfo) isInfo = !isError[0];
        FacesMessage.Severity severity = isInfo? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, ""));
    }

    public boolean getRandomBool() { // for test purposes only
        var time = System.nanoTime();
        return time % 2 == 0;
    }
}
