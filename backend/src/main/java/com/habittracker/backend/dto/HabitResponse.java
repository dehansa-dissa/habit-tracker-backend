package com.habittracker.backend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class HabitResponse {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private double completionPercentage;
    private int streak;
    private boolean completedToday;
}