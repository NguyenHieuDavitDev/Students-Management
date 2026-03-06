package com.example.stduents_management.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoScheduleResult {

    private int createdCount;
    private int skippedCount;
    private String message;
}
