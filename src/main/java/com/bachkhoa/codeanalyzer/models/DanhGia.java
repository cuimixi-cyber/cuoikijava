package com.bachkhoa.codeanalyzer.models;

public class DanhGia {
    private int submissionId;
    private String dsaTags;
    private double aiGeneratedProbability;
    private String analysisReport;

    public DanhGia(int submissionId, String dsaTags, double aiGeneratedProbability, String analysisReport) {
        this.submissionId = submissionId;
        this.dsaTags = dsaTags;
        this.aiGeneratedProbability = aiGeneratedProbability;
        this.analysisReport = analysisReport;
    }

    public int getSubmissionId() { return submissionId; }
    public String getDsaTags() { return dsaTags; }
    public double getAiGeneratedProbability() { return aiGeneratedProbability; }
    public String getAnalysisReport() { return analysisReport; }
}