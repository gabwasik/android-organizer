package com.example.organizer;

import androidx.annotation.NonNull;

public class Task {
    private final String name;
    private String url;

    public Task(String name) {
        this.name = name;
    }
    public Task(String name, String url) {
        this.name = name;
        if (url != null) {
            if (!(url.startsWith("http://") && url.startsWith("https://"))){
                if (url.startsWith("//")) url = "https:" + url;
                else url = "https://" + url;
            }

            this.url = url;
        }
    }

    public String getName() {
        return name;
    }
    public String getUrl() {
        return url;
    }
    @NonNull @Override public String toString() {
        return name;
    }
}
