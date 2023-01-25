package com.example.e_junk;

public class LocationHelperClass {

    String id, name, barangay;
    double latitude, longitude;

    public LocationHelperClass() {

    }

    public LocationHelperClass(String id, String name, String barangay, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.barangay = barangay;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarangay() {
        return barangay;
    }

    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
