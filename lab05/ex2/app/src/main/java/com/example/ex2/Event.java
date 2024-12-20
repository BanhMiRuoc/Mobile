package com.example.ex2;

import java.io.Serializable;

public class Event implements Serializable {
    private String name;
    private String place;
    private String date;
    private String time;
    private boolean isEnabled;

    public Event(String name, String place, String date, String time, boolean isEnabled) {
        this.name = name;
        this.place = place;
        this.date = date;
        this.time = time;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Event event = (Event) obj;
        return name.equals(event.name) &&
                place.equals(event.place) &&
                date.equals(event.date) &&
                time.equals(event.time);
    }
}
