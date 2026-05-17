package com.bachkhoa.codeanalyzer.models;

public class ThongKeNick {
    private int coderId;
    private String handleName;
    private String platform;
    private int totalSubmissions;
    private int analyzedCount;
    private double avgAiProbability;   // 0.0 – 1.0
    private String topAlgorithms;      // Các thuật toán xuất hiện nhiều nhất

    public ThongKeNick(int coderId, String handleName, String platform,
                       int totalSubmissions, int analyzedCount,
                       double avgAiProbability, String topAlgorithms) {
        this.coderId = coderId;
        this.handleName = handleName;
        this.platform = platform;
        this.totalSubmissions = totalSubmissions;
        this.analyzedCount = analyzedCount;
        this.avgAiProbability = avgAiProbability;
        this.topAlgorithms = topAlgorithms;
    }

    /** Mức rủi ro AI dựa trên avgAiProbability */
    public String getAiRiskLevel() {
        if (avgAiProbability < 0.30) return "Thấp";
        if (avgAiProbability < 0.60) return "Trung bình";
        return "Cao";
    }

    public int getCoderId()             { return coderId; }
    public String getHandleName()       { return handleName; }
    public String getPlatform()         { return platform; }
    public int getTotalSubmissions()    { return totalSubmissions; }
    public int getAnalyzedCount()       { return analyzedCount; }
    public double getAvgAiProbability() { return avgAiProbability; }
    public String getTopAlgorithms()    { return topAlgorithms; }
}
