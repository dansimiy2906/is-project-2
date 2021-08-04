package com.example.onyeshatalanta;

public class Setup {
    private String username,fullname,countryname;

    public Setup (){

    }

    public Setup(String username, String fullname, String countryname) {
        this.username = username;
        this.fullname = fullname;
        this.countryname = countryname;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getCountryname() {
        return countryname;
    }
}
