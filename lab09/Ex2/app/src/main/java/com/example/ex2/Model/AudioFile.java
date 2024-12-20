package com.example.ex2.Model;

public class AudioFile {
    private String fileName;
    private String filePath;
    private int duration;
    public AudioFile(String fileName, String filePath, int duration){
        this.fileName = fileName;
        this.filePath = filePath;
        this.duration = duration;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        return "AudioFile{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", duration=" + duration +
                '}';
    }

    public String getDurationString(){
        double fDuration = this.duration/1000.0/60.0;
        String strDouble = String.format("%.2f", fDuration).replace('.', ':');

        return strDouble;
    }
}
