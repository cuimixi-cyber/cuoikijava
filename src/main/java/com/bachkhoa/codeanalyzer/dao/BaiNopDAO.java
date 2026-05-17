package com.bachkhoa.codeanalyzer.dao;

import com.bachkhoa.codeanalyzer.models.BaiNop;
import com.bachkhoa.codeanalyzer.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BaiNopDAO {
    
    public boolean addBaiNop(BaiNop baiNop) {
        String sql = "INSERT INTO BaiNop (CoderId, SubmissionId, ProblemName, CodeContent, SubmitTime) VALUES (?, ?, ?, ?, GETDATE())";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, baiNop.getCoderId());
            pstmt.setString(2, baiNop.getSubmissionId());
            pstmt.setString(3, baiNop.getProblemName());
            pstmt.setString(4, baiNop.getCodeContent());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi lưu bài nộp: " + e.getMessage());
            return false;
        }
    }

    public List<BaiNop> getUnanalyzedSubmissions() {
        List<BaiNop> list = new ArrayList<>();
        // Đã sửa: Lọc theo Id tự tăng của Database
        String sql = "SELECT * FROM BaiNop WHERE Id NOT IN (SELECT SubmissionId FROM DanhGia)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                BaiNop bn = new BaiNop(
                        rs.getInt("Id"), // Lấy khóa chính Id
                        rs.getInt("CoderId"),
                        rs.getString("SubmissionId"),
                        rs.getString("ProblemName"),
                        rs.getString("CodeContent")
                );
                list.add(bn);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách chưa phân tích: " + e.getMessage());
        }
        return list;
    }
}