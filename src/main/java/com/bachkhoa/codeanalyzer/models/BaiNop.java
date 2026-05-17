package com.bachkhoa.codeanalyzer.models;

public class BaiNop {
    private int id; // Khóa chính tự tăng trong Database
    private int coderId;
    private String submissionId; // ID lấy từ trang Codeforces
    private String problemName;
    private String codeContent;

    // Constructor dùng khi lấy dữ liệu TỪ Database lên
    public BaiNop(int id, int coderId, String submissionId, String problemName, String codeContent) {
        this.id = id;
        this.coderId = coderId;
        this.submissionId = submissionId;
        this.problemName = problemName;
        this.codeContent = codeContent;
    }

    // Constructor dùng khi tạo mới để lưu XUỐNG Database
    public BaiNop(int coderId, String submissionId, String problemName, String codeContent) {
        this.coderId = coderId;
        this.submissionId = submissionId;
        this.problemName = problemName;
        this.codeContent = codeContent;
    }

    public int getId() { return id; }
    public int getCoderId() { return coderId; }
    public String getSubmissionId() { return submissionId; }
    public String getProblemName() { return problemName; }
    public String getCodeContent() { return codeContent; }
}