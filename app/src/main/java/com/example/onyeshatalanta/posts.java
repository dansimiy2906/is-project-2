package com.example.onyeshatalanta;

public class posts {
    private String uid,time,date,postImage,caption,profileimage,Fullname;
    public posts(){

    }

    public posts(String uid, String time, String date, String postImage, String caption, String profileimage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postImage = postImage;
        this.caption = caption;
        this.profileimage = profileimage;
        this.Fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }


    public String getFullname() {
        return Fullname;
    }

    public void setFullname(String fullname) {
        this.Fullname = fullname;
    }
}
