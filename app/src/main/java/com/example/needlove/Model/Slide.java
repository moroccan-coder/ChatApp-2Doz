package com.example.needlove.Model;

public class Slide {

    private int image;
    private String imageLink;
    private String Title;

    public Slide(int image, String title) {
        this.image = image;
        Title = title;
    }

    public Slide(String imageLink) {
        this.imageLink = imageLink;
    }

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }
}
