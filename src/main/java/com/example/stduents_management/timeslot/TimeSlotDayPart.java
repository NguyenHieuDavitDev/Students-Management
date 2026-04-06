package com.example.stduents_management.timeslot;

import com.example.stduents_management.timeslot.entity.TimeSlot;

/**
 * Phân buổi trong ngày (dùng lưới lịch sáng / chiều / tối).
 * Giờ bắt đầu: &lt; 12h → sáng; &lt; 18h → chiều; còn lại → tối. Nếu không có giờ → suy từ số tiết (1–5 / 6–10 / 11+).
 */
public enum TimeSlotDayPart {
    MORNING("MORNING", "Buổi sáng"),
    AFTERNOON("AFTERNOON", "Buổi chiều"),
    EVENING("EVENING", "Buổi tối");

    private final String apiValue;
    private final String labelVi;

    TimeSlotDayPart(String apiValue, String labelVi) {
        this.apiValue = apiValue;
        this.labelVi = labelVi;
    }

    public String getApiValue() {
        return apiValue;
    }

    public String getLabelVi() {
        return labelVi;
    }

    public static TimeSlotDayPart resolve(TimeSlot ts) {
        if (ts == null) {
            return AFTERNOON;
        }
        if (ts.getStartTime() != null) {
            int h = ts.getStartTime().getHour();
            if (h < 12) {
                return MORNING;
            }
            if (h < 18) {
                return AFTERNOON;
            }
            return EVENING;
        }
        int ps = ts.getPeriodStart() != null ? ts.getPeriodStart() : 1;
        if (ps <= 5) {
            return MORNING;
        }
        if (ps <= 10) {
            return AFTERNOON;
        }
        return EVENING;
    }
}
