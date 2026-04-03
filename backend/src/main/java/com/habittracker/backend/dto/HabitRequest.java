package com.habittracker.backend.dto;

import lombok.Data;

@Data
public class HabitRequest {
    private String name;
    private String description;
}