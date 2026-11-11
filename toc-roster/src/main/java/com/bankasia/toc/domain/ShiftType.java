package com.bankasia.toc.domain;

import java.time.LocalTime;

public enum ShiftType {
    MORNING("Morning Duty", LocalTime.of(10, 0), LocalTime.of(17, 0)),
    EVENING("Evening Duty", LocalTime.of(14, 30), LocalTime.of(21, 30)),
    NIGHT("Night Duty", LocalTime.of(21, 30), LocalTime.of(10, 0)); // ends next day

    private final String label;
    private final LocalTime start;
    private final LocalTime end;

    ShiftType(String label, LocalTime start, LocalTime end) {
        this.label = label;
        this.start = start;
        this.end = end;
    }

    public String getLabel() { return label; }
    public LocalTime getStart() { return start; }
    public LocalTime getEnd() { return end; }

    public String getTimeRange() { return fmt(start) + " - " + fmt(end); }

    private static String fmt(LocalTime t) {
        int h = t.getHour() % 12 == 0 ? 12 : t.getHour() % 12;
        return String.format("%02d:%02d %s", h, t.getMinute(), t.getHour() < 12 ? "am" : "pm");
    }
}
