package com.example.ex1.Model;

import com.example.ex1.R;

public class File {
    private String name;
    private double capacity;
    private int status;
    private int progress;

    public File() {
    }

    public File(String name, double capacity, int status, int progress) {
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.progress = progress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getImageResourceId(){
        int resId = R.drawable.icon_other;

        String ext = name.substring(name.lastIndexOf(".") + 1);
        switch (ext){
            case "docx":
            case "pdf":
                resId = R.drawable.icon_office;
                break;
            case "jpg":
            case "png":
                resId = R.drawable.icon_img;
                break;
            case "mp3":
            case "mp4":
                resId = R.drawable.icon_music;
                break;
            case "txt":
                resId = R.drawable.icon_text;
                break;
            case "zip":
            case "rar":
                resId = R.drawable.icon_archive;
                break;
        }

        return resId;
    }
    public double getCapacityPerFile(String fileName){
        double resId = R.drawable.icon_other;

        String ext = name.substring(name.lastIndexOf(".") + 1);
        switch (ext){
            case "docx":
            case "pdf":
                capacity = 1.5;
                break;
            case "jpg":
            case "png":
                capacity = 2.3;
                break;
            case "mp3":
            case "mp4":
                capacity = 20;
                break;
            case "txt":
                capacity = 0.5;
                break;
            case "zip":
            case "rar":
                capacity = 30;
                break;
        }

        return resId;
    }


    @Override
    public String toString() {
        return "File{" +
                "name='" + name + '\'' +
                ", capacity=" + capacity +
                ", status=" + status +
                ", progress=" + progress +
                '}';
    }
}
