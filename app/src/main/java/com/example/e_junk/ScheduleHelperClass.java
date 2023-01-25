package com.example.e_junk;

public class ScheduleHelperClass {

    String id, barangay, zone, time, date;

    public ScheduleHelperClass() {
    }

    public ScheduleHelperClass(String id, String barangay, String zone, String time, String date) {
        this.id = id;
        this.barangay = barangay;
        this.zone = zone;
        this.time = time;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
