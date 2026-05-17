package com.bachkhoa.codeanalyzer.services;

import com.bachkhoa.codeanalyzer.dao.HocVienDAO;
import com.bachkhoa.codeanalyzer.models.HocVien;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CrawlScheduler {
    private ScheduledExecutorService scheduler;
    private HocVienDAO hocVienDAO = new HocVienDAO();
    private JTextArea consoleArea;
    private boolean isRunning = false;
    private int intervalHours = 24;

    public CrawlScheduler(JTextArea consoleArea) {
        this.consoleArea = consoleArea;
    }

    public void start(int intervalHours) {
        if (isRunning) return;
        this.intervalHours = intervalHours;
        scheduler = Executors.newSingleThreadScheduledExecutor();
        
        log("Bắt đầu lên lịch crawl tự động mỗi " + intervalHours + " giờ.");
        
        // Khởi chạy task ngay lập tức và lặp lại theo chu kỳ
        scheduler.scheduleAtFixedRate(() -> {
            log("--- Kích hoạt Crawl Tự Động ---");
            runAutoCrawl();
        }, 0, intervalHours, TimeUnit.HOURS);
        
        isRunning = true;
    }

    public void stop() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            log("Đã TẮT lịch crawl tự động.");
            isRunning = false;
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
    
    public int getIntervalHours() {
        return intervalHours;
    }

    private void runAutoCrawl() {
        List<HocVien> danhSach = hocVienDAO.getAllHocVien();
        if (danhSach.isEmpty()) {
            log("Không có nick nào trong hệ thống để crawl.");
            return;
        }

        CodeforcesCrawler cfCrawler = new CodeforcesCrawler();
        
        for (HocVien hv : danhSach) {
            log("Auto Crawl đang chạy cho: " + hv.getHandleName() + " (" + hv.getPlatform() + ")");
            if ("Codeforces".equalsIgnoreCase(hv.getPlatform())) {
                try {
                    // Chạy crawl
                    cfCrawler.fetchAndSaveRecentSubmissions(hv.getId(), hv.getHandleName());
                    // Cập nhật last crawled
                    hocVienDAO.updateLastCrawled(hv.getId());
                } catch (Exception e) {
                    log("Lỗi khi crawl " + hv.getHandleName() + ": " + e.getMessage());
                }
            } else {
                log("Platform " + hv.getPlatform() + " chưa được hỗ trợ crawl tự động.");
            }
        }
        log("--- Hoàn tất chu kỳ Crawl Tự Động ---");
    }

    private void log(String msg) {
        if (consoleArea != null) {
            SwingUtilities.invokeLater(() -> {
                consoleArea.append(">>> [Scheduler] " + msg + "\n");
                // Cuộn xuống cuối
                consoleArea.setCaretPosition(consoleArea.getDocument().getLength());
            });
        } else {
            System.out.println("[Scheduler] " + msg);
        }
    }
}
