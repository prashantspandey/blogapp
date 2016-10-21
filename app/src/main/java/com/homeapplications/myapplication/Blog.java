package com.homeapplications.myapplication;



public class Blog {
    private String Description;
    private String ImageUrl;
    private String Title;
    private String UserName;
    private String displayPicture;



    private String uid;

    public Blog(String descripton, String imageUrl, String title) {
        Description = descripton;
        ImageUrl = imageUrl;
        Title = title;
        this.UserName = UserName;
        this.displayPicture = displayPicture;
        this.uid = uid;

    }

    public Blog() {
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }
    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        this.UserName = userName;
    }

    public String getDisplayPicture() {
        return displayPicture;
    }

    public void setDisplayPicture(String displayPicture) {
        this.displayPicture = displayPicture;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
