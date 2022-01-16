package com.huce.mynotes.model;

public class Note {
    private String id;
    private String Title;
    private String Content;
    private String Time;
    private String Image;
    private String Url;

    public Note()
    {

    }

    public Note(String title, String content, String time, String image, String url)
    {
        this.Title = title;
        this.Content = content;
        this.Time = time;
        this.Image = image;
        this.Url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.Title = title;
    }

    public void setContent(String content) { this.Content = content; }

    public String getTitle() {
        return Title;
    }

    public String getContent() {
        return Content;
    }

    public void setTime(String time) { Time = time; }

    public String getTime() { return Time; }

    public void setImage(String image) {
        Image = image;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getImage() {
        return Image;
    }

    public String getUrl() {
        return Url;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id='" + id + '\'' +
                "Title='" + Title + '\'' +
                ", Content='" + Content + '\'' +
                ", Time='" + Time + '\'' +
                ", Image='" + Image + '\'' +
                ", Url='" + Url + '\'' +
                '}';
    }
}


