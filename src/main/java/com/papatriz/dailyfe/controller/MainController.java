package com.papatriz.dailyfe.controller;

import com.papatriz.dailyfe.api.ApiAction;
import com.papatriz.dailyfe.api.RestApi;
import com.papatriz.dailyfe.dto.ActivityDto;
import com.papatriz.dailyfe.dto.UserDto;
import com.papatriz.dailyfe.model.EState;
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

    public void completeActivity(ActivityDto activity) {
        var urlTemplate = api.getUrlFor(ApiAction.CompleteActivity);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        var url = urlTemplate.replace("{id}", String.valueOf(activity.getId()))
                .replace("{date}", LocalDate.now().format(formatter));
        String response = restTemplate.postForObject(url, null, String.class);

        logger.info("Complete activity, remote server response: "+response);
    }

    public String getDateRepresent(int dateShift) {
       var targetDate = startDate.plusDays(dateShift);
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
       return targetDate.format(formatter);
    }

    public EState getActivityState(ActivityDto activity, int dateShift) {
        var checkedDate = startDate.plusDays(dateShift);
        if (checkedDate.isAfter(LocalDate.now())) return EState.AWAITED;
        if(activity.getStartDate().isAfter(checkedDate)) return EState.NOT_STARTED;

        var complete = isActivityComplete(activity,checkedDate);

        if (complete) return EState.COMPLETED;
        if (checkedDate.isEqual(LocalDate.now())) return EState.AWAITED;

        return EState.FAILED;
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

    public void onRowCancel(RowEditEvent<ActivityDto> event) { //toDo: remove, this is not necessary
        showMessage("Editing canceled");
    }

    // ================ PRIVATE AND UTIL METHODS ================
    private ActivityDto[] getActivities() {
        var url = api.getUrlFor(ApiAction.GetActivityList);

        return restTemplate.getForObject(url, ActivityDto[].class);
    }
    private boolean isActivityComplete(ActivityDto activity, LocalDate checkedDate) {

        var urlTemplate = api.getUrlFor(ApiAction.IsActivityComplete);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        var url = urlTemplate.replace("{id}", String.valueOf(activity.getId()))
                .replace("{date}", checkedDate.format(formatter));

        return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    }

    private void showMessage(String message, boolean... isError) {
        var isInfo = isError.length == 0;
        if (!isInfo) isInfo = !isError[0];
        FacesMessage.Severity severity = isInfo? FacesMessage.SEVERITY_INFO : FacesMessage.SEVERITY_ERROR;
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, message, ""));
    }

    public boolean getRandomBool() { //toDo: for test purposes only, remove
        var time = System.nanoTime();
        return time % 2 == 0;
    }
}
