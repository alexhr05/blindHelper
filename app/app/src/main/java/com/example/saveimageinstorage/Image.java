package com.example.saveimageinstorage;

public class Image {
    String title;
    String path;
    Long size;

    public Image(String title,String path, Long size){
        this.title = title;
        this.path = path;
        this.size = size;
    }

    public String getTitle(){
        return title;
    }

    public String getPath(){
        return path;
    }

    public Long getSize(){
        return size;
    }

}
