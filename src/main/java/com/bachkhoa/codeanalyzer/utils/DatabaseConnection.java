package com.bachkhoa.codeanalyzer.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Dựa vào ảnh của bạn, server là .\SQLEXPRESS01 và dùng Windows Authentication
    private static final String DB_URL = "jdbc:sqlserver://localhost;databaseName=CodeAnalyzerDB;integratedSecurity=true;encrypt=true;trustServerCertificate=true;";
    
    private static Connection connection = null;

    // Chặn khởi tạo đối tượng từ bên ngoài (Singleton)
    private DatabaseConnection() {}

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // Tải driver
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                
                // Mở kết nối
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("✅ Kết nối Cơ sở dữ liệu thành công!");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Không tìm thấy Driver JDBC: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối CSDL: " + e.getMessage());
        }
        return connection;
    }
}
