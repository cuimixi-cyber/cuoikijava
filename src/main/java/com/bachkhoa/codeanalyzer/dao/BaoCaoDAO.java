package com.bachkhoa.codeanalyzer.dao;

import com.bachkhoa.codeanalyzer.utils.DatabaseConnection;
import javax.swing.table.DefaultTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Vector;

public class BaoCaoDAO {

    public DefaultTableModel getBaoCaoModel() {
        Vector<String> columnNames = new Vector<>();
        columnNames.add("ID Bài Nộp");
        columnNames.add("Người Nộp");       // <-- thêm cột tên người nộp
        columnNames.add("Tên Bài");
        columnNames.add("Thuật Toán (AI)");
        columnNames.add("Tỷ lệ AI viết");
        columnNames.add("__CodeContent__"); // cột ẩn chứa source code

        Vector<Vector<Object>> data = new Vector<>();

        // JOIN thêm bảng HocVien để lấy tên (handle), và lấy CodeContent để xem code
        String sql = "SELECT b.SubmissionId, h.HandleName, b.ProblemName, " +
                     "d.DsaTags, d.AiGeneratedProbability, b.CodeContent " +
                     "FROM BaiNop b " +
                     "LEFT JOIN HocVien h ON b.CoderId = h.Id " +
                     "LEFT JOIN DanhGia d ON b.Id = d.SubmissionId " +
                     "ORDER BY b.Id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("SubmissionId"));
                
                String handle = rs.getString("HandleName");
                row.add(handle != null ? handle : "N/A");

                row.add(rs.getString("ProblemName"));

                String tags = rs.getString("DsaTags");
                row.add(tags != null ? tags : "Chưa phân tích");

                double prob = rs.getDouble("AiGeneratedProbability");
                if (rs.wasNull()) {
                    row.add("N/A");
                } else {
                    row.add(String.format("%.1f%%", prob * 100));
                }

                // Cột ẩn: source code
                String code = rs.getString("CodeContent");
                row.add(code != null ? code : "// Không có source code");

                data.add(row);
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy báo cáo từ DB: " + e.getMessage());
        }

        return new DefaultTableModel(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }
}