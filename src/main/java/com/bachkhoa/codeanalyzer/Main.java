package com.bachkhoa.codeanalyzer;

import com.bachkhoa.codeanalyzer.ui.MainForm;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        // Chạy giao diện trên luồng an toàn của Java (Event Dispatch Thread)
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Khởi tạo và hiển thị form
                MainForm form = new MainForm();
                form.setVisible(true);
            }
        });
    }
}