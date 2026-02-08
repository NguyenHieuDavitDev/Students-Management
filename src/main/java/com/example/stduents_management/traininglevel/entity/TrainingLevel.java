package com.example.stduents_management.traininglevel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "training_levels",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "training_level_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainingLevel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uniqueidentifier")
    private UUID trainingLevelId;

    @Column(
            name = "training_level_name",
            nullable = false,
            columnDefinition = "NVARCHAR(100)"
    )
    private String trainingLevelName;
}
