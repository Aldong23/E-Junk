package com.example.e_junk;

public class UserHelperClass {

    String name, username, contact, barangay, zone, birthday, usertype;

    public UserHelperClass() {

    }

    public UserHelperClass(String name, String username, String contact, String barangay, String zone, String birthday, String usertype) {
        this.name = name;
        this.username = username;
        this.contact = contact;
        this.barangay = barangay;
        this.zone = zone;
        this.birthday = birthday;
        this.usertype = usertype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
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

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getUsertype() {
        return usertype;
    }

    public void setUsertype(String usertype) {
        this.usertype = usertype;
    }
}
