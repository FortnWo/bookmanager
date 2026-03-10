package com.fortn.bookmanager.model;

public class Persona {
    private String id;
    private String name;
    private String content;
    private String description;

    public Persona() {
    }

    public Persona(String id, String name, String content, String description) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}