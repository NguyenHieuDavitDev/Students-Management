package com.example.stduents_management.schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AutoScheduleResult {

    private int createdCount;
    private int skippedCount;
    private String message;
    private List<String> skippedDetails = new ArrayList<>();
}
