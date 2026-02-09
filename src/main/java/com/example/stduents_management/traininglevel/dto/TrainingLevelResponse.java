package com.example.stduents_management.traininglevel.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TrainingLevelResponse {

    private UUID trainingLevelId;
    private String trainingLevelName;
    private String description;
}
