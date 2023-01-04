package com.papatriz.dailyfe.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RestApi {
    @Value("#{${restapi.root}}")
    private String restApiRoot;

    @Value("#{${restapi.endpoints}}")
    private Map<String, String> restEndPoints;

    public String getUrlFor(ApiAction action) {
        return restApiRoot + restEndPoints.get(action.toString());
    }
}
