package com.example.allan.booklistingapp;

import java.util.ArrayList;

public class Books {
    private String title;
    private ArrayList<String> authors;

    public Books(String title, ArrayList<String> authors) {
        this.title = title;
        this.authors = authors;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<String> getAuthors() {
        return authors;
    }

    public void setAuthors(ArrayList<String> authors) {
        this.authors = authors;
    }
}