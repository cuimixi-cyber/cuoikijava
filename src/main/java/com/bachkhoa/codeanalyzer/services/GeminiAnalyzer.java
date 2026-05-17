package com.bachkhoa.codeanalyzer.services;

import com.bachkhoa.codeanalyzer.dao.DanhGiaDAO;
import com.bachkhoa.codeanalyzer.models.DanhGia;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeminiAnalyzer {

    // API Key của bạn
    private static final String API_KEY = "AIzaSyB4SZC-iTxwkJe77y7CpUbG-xxvfQ1Nrd0";

    // Model gemini-2.5-flash
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
            + API_KEY;

    // Rate limiting: free tier cho phép tối đa 20 req/phút
    // Delay 4 giây mỗi request => ~15 req/phút => an toàn
    private static final long DELAY_BETWEEN_REQUESTS_MS = 4000;

    // Số lần retry khi bị quota exceeded (429)
    private static final int MAX_RETRIES = 3;

    // Thời gian chờ trước khi retry (60 giây) khi bị quota exceeded
    private static final long RETRY_WAIT_MS = 65000;

    private DanhGiaDAO danhGiaDAO = new DanhGiaDAO();

    public void analyzeAndSave(int submissionId, String sourceCode) {
        System.out.println("-> Dang gui ma nguon len Google Gemini phan tich (ID: " + submissionId + ")...");

        // --- Rate Limiting: delay trước mỗi lần gọi API ---
        try {
            System.out.println("   [Rate Limit] Cho " + (DELAY_BETWEEN_REQUESTS_MS / 1000) + " giay truoc khi gui request...");
            Thread.sleep(DELAY_BETWEEN_REQUESTS_MS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        // Prompt hướng dẫn AI trả về JSON chuẩn
        String promptText = "Bạn là chuyên gia phân tích mã nguồn. Hãy đọc đoạn code sau và trả về KẾT QUẢ DUY NHẤT LÀ MỘT CHUỖI JSON, không giải thích gì thêm. "
                + "Cấu trúc JSON bắt buộc: {\"dsaTags\": \"các thuật toán/cấu trúc dữ liệu được sử dụng\", "
                + "\"aiProbability\": (số thập phân 0.0 đến 1.0 đánh giá tỷ lệ code do AI viết), "
                + "\"report\": \"Nhận xét ngắn gọn về logic (tối đa 2 câu)\"}. "
                + "Mã nguồn: \n" + sourceCode;

        // Xây dựng JSON request body bằng Gson
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptText);

        JsonArray partsArray = new JsonArray();
        partsArray.add(textPart);

        JsonObject contentObj = new JsonObject();
        contentObj.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObj);

        JsonObject requestBodyObj = new JsonObject();
        requestBodyObj.add("contents", contentsArray);

        String requestBody = requestBodyObj.toString();

        // --- Retry loop khi bị 429 quota exceeded ---
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(API_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();
                String responseBody = response.body();

                // Xử lý lỗi 429 quota exceeded
                if (statusCode == 429) {
                    if (attempt < MAX_RETRIES) {
                        System.out.println("   [Quota Exceeded] Vuot gioi han API! Cho " 
                            + (RETRY_WAIT_MS / 1000) + " giay roi thu lai (lan " + attempt + "/" + MAX_RETRIES + ")...");
                        Thread.sleep(RETRY_WAIT_MS);
                        continue; // thử lại
                    } else {
                        System.out.println("   [Quota Exceeded] Da thu lai " + MAX_RETRIES + " lan nhung van bi chặn. Bo qua bai nay.");
                        return;
                    }
                }

                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                // Kiểm tra lỗi từ API
                if (jsonResponse.has("error")) {
                    String errorMsg = jsonResponse.getAsJsonObject("error").get("message").getAsString();
                    int errorCode = jsonResponse.getAsJsonObject("error").get("code").getAsInt();

                    if (errorCode == 429 && attempt < MAX_RETRIES) {
                        System.out.println("   [Quota Exceeded] " + errorMsg);
                        System.out.println("   [Quota Exceeded] Cho " + (RETRY_WAIT_MS / 1000) + " giay roi thu lai (lan " + attempt + "/" + MAX_RETRIES + ")...");
                        Thread.sleep(RETRY_WAIT_MS);
                        continue; // thử lại
                    }

                    System.out.println("   [Loi tu Google API] " + errorMsg);
                    return;
                }

                // Trích xuất nội dung text từ AI
                String aiRawText = jsonResponse
                        .getAsJsonArray("candidates").get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts").get(0).getAsJsonObject()
                        .get("text").getAsString();

                // Dọn dẹp ký tự Markdown nếu AI lỡ thêm vào
                String cleanJson = aiRawText.replace("```json", "").replace("```", "").trim();

                // Phân tích kết quả JSON
                JsonObject aiResult = JsonParser.parseString(cleanJson).getAsJsonObject();
                String dsaTags = aiResult.get("dsaTags").getAsString();
                double aiProbability = aiResult.get("aiProbability").getAsDouble();
                String report = aiResult.get("report").getAsString();

                // Hiển thị kết quả ra Terminal
                System.out.println("   [AI Response] Thuat toan: " + dsaTags);
                System.out.println("   [AI Response] Ty le AI viet: " + (aiProbability * 100) + "%");
                System.out.println("   [AI Response] Nhan xet: " + report);

                // Lưu kết quả vào SQL Server
                DanhGia danhGia = new DanhGia(submissionId, dsaTags, aiProbability, report);
                boolean isSaved = danhGiaDAO.addDanhGia(danhGia);

                if (isSaved) {
                    System.out.println("   [Thanh cong] Da luu nhan xet AI vao Database!");
                } else {
                    System.out.println("   [That bai] Loi khong the luu vao CSDL.");
                }

                return; // thành công, thoát vòng lặp retry

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.out.println("=> Bi ngat khi cho delay.");
                return;
            } catch (Exception e) {
                System.out.println("=> Loi he thong phan tich: " + e.getMessage());
                return;
            }
        }
    }
}