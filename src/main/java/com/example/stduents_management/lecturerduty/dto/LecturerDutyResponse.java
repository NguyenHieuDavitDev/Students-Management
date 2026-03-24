package com.example.stduents_management.lecturerduty.dto;

import java.util.UUID;

public record LecturerDutyResponse(
        UUID lecturerDutyId,
        String dutyCode,
        String dutyName,
        String description
) {
}
