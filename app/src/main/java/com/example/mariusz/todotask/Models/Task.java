package com.example.mariusz.todotask.Models;

import java.util.Date;

/**
 * Created by mariusz on 24/05/16.
 */
public class Task {
    private long id;
    private String name;
    private Date endDate;
    private Date createdAt;
    private String description;
    private boolean isOpen;
    private String facebookId;
    private String taskId;

    public Task() {}
    public Task(String name, Date endDate, Date createdAt, String description, String facebookId, String taskId) {
        super();
        this.name = name;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.description = description;
        this.isOpen = false;
        this.facebookId = facebookId;
        this.taskId = taskId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public String getFacebookId() {
        return facebookId;
    }

    public void setFacebookId(String facebookId) {
        this.facebookId = facebookId;
    }
}
