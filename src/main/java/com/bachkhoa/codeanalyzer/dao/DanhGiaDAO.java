package com.bachkhoa.codeanalyzer.dao;

import com.bachkhoa.codeanalyzer.models.DanhGia;
import com.bachkhoa.codeanalyzer.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DanhGiaDAO {
    
    public boolean addDanhGia(DanhGia danhGia) {
        String sql = "INSERT INTO DanhGia (SubmissionId, DsaTags, AiGeneratedProbability, AnalysisReport) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, danhGia.getSubmissionId());
            pstmt.setString(2, danhGia.getDsaTags());
            pstmt.setDouble(3, danhGia.getAiGeneratedProbability());
            pstmt.setString(4, danhGia.getAnalysisReport());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Loi luu ket qua danh gia: " + e.getMessage());
            return false;
        }
    }
}