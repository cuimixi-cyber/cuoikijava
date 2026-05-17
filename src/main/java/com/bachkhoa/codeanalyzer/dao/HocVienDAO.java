package com.bachkhoa.codeanalyzer.dao;

import com.bachkhoa.codeanalyzer.models.HocVien;
import com.bachkhoa.codeanalyzer.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocVienDAO {

    /** Thêm nick mới vào CSDL */
    public boolean addHocVien(HocVien hocVien) {
        String sql = "INSERT INTO HocVien (HandleName, Platform) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hocVien.getHandleName());
            pstmt.setString(2, hocVien.getPlatform());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm học viên: " + e.getMessage());
            return false;
        }
    }

    /** Lấy tất cả nick trong hệ thống */
    public List<HocVien> getAllHocVien() {
        List<HocVien> list = new ArrayList<>();
        String sql = "SELECT Id, HandleName, Platform, AddedDate, LastCrawledAt FROM HocVien ORDER BY Id DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                HocVien hv = new HocVien(
                        rs.getInt("Id"),
                        rs.getString("HandleName"),
                        rs.getString("Platform"),
                        rs.getTimestamp("AddedDate"),
                        rs.getTimestamp("LastCrawledAt")
                );
                list.add(hv);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách học viên: " + e.getMessage());
        }
        return list;
    }

    /** Xóa nick theo ID */
    public boolean deleteHocVien(int id) {
        String sql = "DELETE FROM HocVien WHERE Id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa học viên: " + e.getMessage());
            return false;
        }
    }

    /** Cập nhật thời điểm crawl cuối cùng */
    public void updateLastCrawled(int id) {
        String sql = "UPDATE HocVien SET LastCrawledAt = GETDATE() WHERE Id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật LastCrawledAt: " + e.getMessage());
        }
    }

    /** Lấy hoặc tạo mới học viên, trả về ID */
    public int getOrCreateHocVien(String handleName, String platform) {
        String querySql = "SELECT Id FROM HocVien WHERE HandleName = ? AND Platform = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(querySql)) {
            pstmt.setString(1, handleName);
            pstmt.setString(2, platform);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("Id");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn học viên: " + e.getMessage());
        }

        String insertSql = "INSERT INTO HocVien (HandleName, Platform) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, handleName);
            pstmt.setString(2, platform);
            int affected = pstmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) return keys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi thêm học viên mới: " + e.getMessage());
        }
        return -1;
    }
}
