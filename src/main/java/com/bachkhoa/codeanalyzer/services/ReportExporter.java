package com.bachkhoa.codeanalyzer.services;

import com.bachkhoa.codeanalyzer.models.ThongKeNick;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportExporter {

    public static String exportHtmlReport(List<ThongKeNick> danhSach) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "BaoCao_DanhGia_" + timestamp + ".html";
        File file = new File(fileName);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang='vi'>\n<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>Báo Cáo Đánh Giá Năng Lực Lập Trình</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f5f7fa; color: #333; margin: 0; padding: 20px; }\n");
        html.append("        .container { max-width: 1000px; margin: 0 auto; background: #fff; padding: 30px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }\n");
        html.append("        h1 { color: #2c3e50; text-align: center; border-bottom: 2px solid #3498db; padding-bottom: 10px; }\n");
        html.append("        h2 { color: #34495e; margin-top: 30px; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        html.append("        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #3498db; color: white; }\n");
        html.append("        tr:hover { background-color: #f1f1f1; }\n");
        html.append("        .risk-low { color: #27ae60; font-weight: bold; }\n");
        html.append("        .risk-medium { color: #f39c12; font-weight: bold; }\n");
        html.append("        .risk-high { color: #c0392b; font-weight: bold; }\n");
        html.append("        .footer { text-align: center; margin-top: 40px; color: #7f8c8d; font-size: 0.9em; }\n");
        html.append("    </style>\n");
        html.append("</head>\n<body>\n");
        
        html.append("<div class='container'>\n");
        html.append("    <h1>BÁO CÁO ĐÁNH GIÁ NĂNG LỰC LẬP TRÌNH TỔNG HỢP</h1>\n");
        html.append("    <p>Ngày tạo: ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date())).append("</p>\n");
        html.append("    <p>Tổng số sinh viên / nicks được phân tích: <strong>").append(danhSach.size()).append("</strong></p>\n");

        html.append("    <h2>Kết Quả Chi Tiết</h2>\n");
        html.append("    <table>\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th>Handle</th>\n");
        html.append("                <th>Platform</th>\n");
        html.append("                <th>Số bài nộp</th>\n");
        html.append("                <th>Đã AI Phân Tích</th>\n");
        html.append("                <th>Tỷ lệ AI trung bình</th>\n");
        html.append("                <th>Thuật toán sử dụng nhiều nhất</th>\n");
        html.append("                <th>Mức Rủi Ro AI</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");

        for (ThongKeNick tk : danhSach) {
            html.append("            <tr>\n");
            html.append("                <td><strong>").append(tk.getHandleName()).append("</strong></td>\n");
            html.append("                <td>").append(tk.getPlatform()).append("</td>\n");
            html.append("                <td>").append(tk.getTotalSubmissions()).append("</td>\n");
            html.append("                <td>").append(tk.getAnalyzedCount()).append("</td>\n");
            
            double aiPercent = tk.getAvgAiProbability() * 100;
            html.append("                <td>").append(String.format("%.1f%%", aiPercent)).append("</td>\n");
            
            html.append("                <td>").append(tk.getTopAlgorithms()).append("</td>\n");
            
            String risk = tk.getAiRiskLevel();
            String riskClass = "risk-low";
            if (risk.equals("Trung bình")) riskClass = "risk-medium";
            else if (risk.equals("Cao")) riskClass = "risk-high";
            
            html.append("                <td class='").append(riskClass).append("'>").append(risk).append("</td>\n");
            html.append("            </tr>\n");
        }

        html.append("        </tbody>\n");
        html.append("    </table>\n");
        
        html.append("    <div class='footer'>\n");
        html.append("        <p>Báo cáo được tạo tự động bởi Hệ Thống Đánh Giá Năng Lực Lập Trình.</p>\n");
        html.append("    </div>\n");
        html.append("</div>\n");

        html.append("</body>\n</html>");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(html.toString());
            return file.getAbsolutePath();
        } catch (IOException e) {
            System.err.println("Lỗi ghi file báo cáo: " + e.getMessage());
            return null;
        }
    }
}
