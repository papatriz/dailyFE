package com.papatriz.dailyfe.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ActivityDto {
    private long id;
    private String title;
    private short duration;
    private short weight;
    private LocalDate startDate;
}

