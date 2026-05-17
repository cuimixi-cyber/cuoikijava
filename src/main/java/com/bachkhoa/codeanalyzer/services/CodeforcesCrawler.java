package com.bachkhoa.codeanalyzer.services;

import com.bachkhoa.codeanalyzer.dao.BaiNopDAO;
import com.bachkhoa.codeanalyzer.models.BaiNop;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.swing.JOptionPane;
import java.io.File;
import java.time.Duration;
import java.util.List;

public class CodeforcesCrawler {

    private BaiNopDAO baiNopDAO = new BaiNopDAO();

    public void fetchAndSaveRecentSubmissions(int coderId, String handle) {
        System.out.println("-> Khởi động trình duyệt Edge để kết nối Codeforces cho nick: " + handle);

        // Khai báo thủ công driver nếu có trong thư mục dự án để bỏ qua tải tự động
        // (tránh lỗi tường lửa)
        String projectDir = System.getProperty("user.dir");
        String driverPath = projectDir + File.separator + "msedgedriver.exe";
        if (new File(driverPath).exists()) {
            System.setProperty("webdriver.edge.driver", driverPath);
            System.out.println("   [Thông tin] Đã phát hiện và sử dụng msedgedriver.exe nội bộ.");
        }

        String userHome = System.getProperty("user.home");
        String profilePath = userHome + File.separator + ".codeforces-crawler-profile";

        EdgeOptions options = new EdgeOptions();
        options.addArguments("user-data-dir=" + profilePath);
        options.addArguments("--disable-blink-features=AutomationControlled");

        WebDriver driver = null;
        try {
            driver = new EdgeDriver(options);
            driver.manage().window().maximize();

            // Điều hướng đến Codeforces trang chủ trước để người dùng đăng nhập / vượt
            // Cloudflare
            driver.get("https://codeforces.com/");

            // Hiển thị hộp thoại nhắc nhở
            JOptionPane.showMessageDialog(null,
                    "Trình duyệt Edge đã được mở.\n\n" +
                            "1. Vui lòng đăng nhập vào tài khoản Codeforces của bạn (nếu chưa đăng nhập).\n" +
                            "2. Xác thực Cloudflare (nếu có).\n\n" +
                            "Sau khi hoàn tất, nhấn OK tại đây để bắt đầu cào dữ liệu tự động.",
                    "Xác thực Codeforces",
                    JOptionPane.INFORMATION_MESSAGE);

            // Điều hướng thẳng tới trang danh sách submission cá nhân của handle
            String submissionsPageUrl = "https://codeforces.com/submissions/" + handle;
            System.out.println("-> Đang quét danh sách submissions từ: " + submissionsPageUrl);
            driver.get(submissionsPageUrl);

            // Chờ cho bảng danh sách xuất hiện
            WebDriverWait tableWait = new WebDriverWait(driver, Duration.ofSeconds(15));
            tableWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".status-frame-datatable")));

            // Lấy tất cả các dòng của bảng
            List<WebElement> rows = driver.findElements(By.cssSelector(".status-frame-datatable tr"));
            System.out.println("   -> Phát hiện " + rows.size() + " hàng trong bảng trạng thái.");

            // Lưu trữ thông tin các submission hợp lệ để cào sau (tránh
            // StaleElementReferenceException)
            java.util.List<SubmissionInfo> validSubmissions = new java.util.ArrayList<>();

            for (WebElement row : rows) {
                try {
                    List<WebElement> cols = row.findElements(By.tagName("td"));
                    if (cols.size() >= 4) {
                        WebElement idCell = cols.get(0);
                        WebElement problemCell = cols.get(3);

                        List<WebElement> aTags = idCell.findElements(By.tagName("a"));
                        if (!aTags.isEmpty()) {
                            WebElement idLink = aTags.get(0);
                            String submissionId = idLink.getText().trim();

                            // Phân tích contest ID hoặc gym ID từ link bài toán để tạo link xem code chuẩn
                            // xác
                            String submissionUrl = "";
                            List<WebElement> problemLinks = problemCell.findElements(By.tagName("a"));
                            if (!problemLinks.isEmpty()) {
                                String problemHref = problemLinks.get(0).getAttribute("href");
                                if (problemHref != null) {
                                    if (problemHref.contains("/contest/")) {
                                        String[] parts = problemHref.split("/contest/");
                                        if (parts.length > 1) {
                                            String contestId = parts[1].split("/")[0];
                                            submissionUrl = "https://codeforces.com/contest/" + contestId
                                                    + "/submission/" + submissionId;
                                        }
                                    } else if (problemHref.contains("/gym/")) {
                                        String[] parts = problemHref.split("/gym/");
                                        if (parts.length > 1) {
                                            String gymId = parts[1].split("/")[0];
                                            submissionUrl = "https://codeforces.com/gym/" + gymId + "/submission/"
                                                    + submissionId;
                                        }
                                    }
                                }
                            }

                            // Nếu tạo được link xem code hợp lệ, thêm vào danh sách cào
                            if (!submissionUrl.isEmpty()) {
                                String problemName = problemCell.getText().trim();
                                validSubmissions.add(new SubmissionInfo(submissionId, submissionUrl, problemName));
                            }
                        }
                    }
                } catch (Exception ex) {
                    // Bỏ qua dòng tiêu đề hoặc lỗi phần tử
                }
            }

            System.out.println("   -> Tìm thấy " + validSubmissions.size() + " bài nộp CÓ THỂ xem được mã nguồn.");

            // Cào giới hạn tối đa 20 bài nộp tìm thấy trên trang hiện tại
            int limit = Math.min(validSubmissions.size(), 10);
            for (int i = 0; i < limit; i++) {
                SubmissionInfo info = validSubmissions.get(i);
                System.out.println("   - Tiến hành cào bài: " + info.problemName + " (ID: " + info.id + ")");

                // Cào mã nguồn
                String sourceCode = scrapeSourceCode(driver, info.url);

                // Lưu vào Database
                BaiNop baiNop = new BaiNop(coderId, info.id, info.problemName, sourceCode);
                boolean isSaved = baiNopDAO.addBaiNop(baiNop);

                if (isSaved) {
                    System.out.println("     -> Lưu thành công code vào Database!");
                } else {
                    System.out.println("     -> Bài này đã tồn tại trong máy, bỏ qua.");
                }

                // Nghỉ 1.5 giây tránh spam request quá nhanh
                Thread.sleep(1500);
            }

        } catch (Exception e) {
            System.out.println("=> Lỗi khi kết nối hoặc cào dữ liệu Codeforces: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("-> Đã đóng trình duyệt Edge thành công.");
                } catch (Exception ex) {
                    System.out.println("     [Lỗi đóng driver] " + ex.getMessage());
                }
            }
        }
    }

    // Hàm phụ: Dùng EdgeDriver cào trực tiếp nội dung Source Code của submission từ
    // link truyền vào
    private String scrapeSourceCode(WebDriver driver, String url) {
        System.out.println("     -> Đang tải trang submission: " + url);

        try {
            driver.get(url);

            // Sử dụng WebDriverWait để đợi mã nguồn hiển thị hoàn toàn
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            WebElement codeElement = wait
                    .until(ExpectedConditions.presenceOfElementLocated(By.id("program-source-text")));

            if (codeElement != null) {
                return codeElement.getText();
            }
        } catch (Exception e) {
            System.out.println("     [Lỗi cào code bằng Selenium] " + e.getMessage());
        }

        return "// Không thể cào được source code do Cloudflare chặn gắt gao. Vui lòng kiểm tra lại.";
    }

    // Class phụ để lưu giữ thông tin tạm thời của submission
    private static class SubmissionInfo {
        String id;
        String url;
        String problemName;

        public SubmissionInfo(String id, String url, String problemName) {
            this.id = id;
            this.url = url;
            this.problemName = problemName;
        }
    }
}