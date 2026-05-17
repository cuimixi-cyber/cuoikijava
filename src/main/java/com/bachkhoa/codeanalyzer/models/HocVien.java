package com.bachkhoa.codeanalyzer.models;

import java.sql.Timestamp;

public class HocVien {
    private int id;
    private String handleName;
    private String platform;
    private Timestamp addedDate;
    private Timestamp lastCrawledAt;

    // Constructor
    public HocVien(String handleName, String platform) {
        this.handleName = handleName;
        this.platform = platform;
    }

    public HocVien(int id, String handleName, String platform, Timestamp addedDate) {
        this.id = id;
        this.handleName = handleName;
        this.platform = platform;
        this.addedDate = addedDate;
    }

    public HocVien(int id, String handleName, String platform, Timestamp addedDate, Timestamp lastCrawledAt) {
        this.id = id;
        this.handleName = handleName;
        this.platform = platform;
        this.addedDate = addedDate;
        this.lastCrawledAt = lastCrawledAt;
    }

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getHandleName() { return handleName; }
    public void setHandleName(String handleName) { this.handleName = handleName; }

    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }

    public Timestamp getAddedDate() { return addedDate; }
    public void setAddedDate(Timestamp addedDate) { this.addedDate = addedDate; }

    public Timestamp getLastCrawledAt() { return lastCrawledAt; }
    public void setLastCrawledAt(Timestamp lastCrawledAt) { this.lastCrawledAt = lastCrawledAt; }
}