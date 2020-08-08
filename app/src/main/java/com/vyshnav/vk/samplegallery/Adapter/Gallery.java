package com.vyshnav.vk.samplegallery.Adapter;


public class Gallery {
String imagePath;

    public Gallery(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
