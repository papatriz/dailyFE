package com.papatriz.dailyfe.dto;

//public record ActivityDto(long id, String title, short duration, short weight) {
//}

import lombok.Data;

@Data
public class ActivityDto {
    private long id;
    private String title;
    private short duration;
    private short weight;
}

