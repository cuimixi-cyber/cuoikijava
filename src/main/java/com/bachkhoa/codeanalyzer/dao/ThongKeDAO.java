package com.bachkhoa.codeanalyzer.dao;

import com.bachkhoa.codeanalyzer.models.ThongKeNick;
import com.bachkhoa.codeanalyzer.utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ThongKeDAO {

    public List<ThongKeNick> getThongKeAllNicks() {
        List<ThongKeNick> resultList = new ArrayList<>();
        
        // Truy vấn tổng hợp: lấy thông tin nick, tổng số bài, số bài đã phân tích, tỉ lệ AI trung bình,
        // và gộp các thuật toán (DsaTags) thành một chuỗi (sử dụng STRING_AGG trong SQL Server 2017+)
        // Nếu dùng SQL Server cũ hơn, có thể xử lý chuỗi ở tầng Java, nhưng STRING_AGG là cách tốt nhất hiện tại.
        String sql = "SELECT " +
                     "    h.Id AS CoderId, " +
                     "    h.HandleName, " +
                     "    h.Platform, " +
                     "    COUNT(b.Id) AS TotalSubmissions, " +
                     "    COUNT(d.SubmissionId) AS AnalyzedCount, " +
                     "    AVG(d.AiGeneratedProbability) AS AvgAiProbability, " +
                     "    STRING_AGG(CAST(d.DsaTags AS NVARCHAR(MAX)), ', ') AS AllTags " +
                     "FROM HocVien h " +
                     "LEFT JOIN BaiNop b ON h.Id = b.CoderId " +
                     "LEFT JOIN DanhGia d ON b.Id = d.SubmissionId " +
                     "GROUP BY h.Id, h.HandleName, h.Platform " +
                     "ORDER BY h.Id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int coderId = rs.getInt("CoderId");
                String handleName = rs.getString("HandleName");
                String platform = rs.getString("Platform");
                int totalSubmissions = rs.getInt("TotalSubmissions");
                int analyzedCount = rs.getInt("AnalyzedCount");
                
                double avgAiProb = rs.getDouble("AvgAiProbability");
                if (rs.wasNull()) {
                    avgAiProb = 0.0;
                }

                String allTags = rs.getString("AllTags");
                String topAlgorithms = extractTopAlgorithms(allTags);

                ThongKeNick tk = new ThongKeNick(coderId, handleName, platform, totalSubmissions, analyzedCount, avgAiProb, topAlgorithms);
                resultList.add(tk);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi truy vấn thống kê: " + e.getMessage());
        }

        return resultList;
    }

    /**
     * Xử lý chuỗi tag gộp lại để tìm ra top thuật toán xuất hiện nhiều nhất.
     */
    private String extractTopAlgorithms(String allTags) {
        if (allTags == null || allTags.trim().isEmpty()) {
            return "Chưa có dữ liệu";
        }

        // Tách các tag (giả sử các tag cách nhau bởi dấu phẩy)
        String[] tags = allTags.split(",");
        java.util.Map<String, Integer> tagCount = new java.util.HashMap<>();
        
        for (String tag : tags) {
            tag = tag.trim();
            if (!tag.isEmpty() && !tag.equalsIgnoreCase("Chưa phân tích") && !tag.equalsIgnoreCase("Không xác định")) {
                tagCount.put(tag, tagCount.getOrDefault(tag, 0) + 1);
            }
        }

        if (tagCount.isEmpty()) return "Chưa có dữ liệu";

        // Sắp xếp giảm dần theo số lượng
        List<java.util.Map.Entry<String, Integer>> list = new ArrayList<>(tagCount.entrySet());
        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        // Lấy top 3
        StringBuilder topTags = new StringBuilder();
        int count = 0;
        for (java.util.Map.Entry<String, Integer> entry : list) {
            if (count > 0) topTags.append(", ");
            topTags.append(entry.getKey()).append(" (").append(entry.getValue()).append(")");
            count++;
            if (count >= 3) break;
        }

        return topTags.toString();
    }
}
