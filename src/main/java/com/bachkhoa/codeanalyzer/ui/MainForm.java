package com.bachkhoa.codeanalyzer.ui;

import com.bachkhoa.codeanalyzer.dao.BaiNopDAO;
import com.bachkhoa.codeanalyzer.dao.BaoCaoDAO;
import com.bachkhoa.codeanalyzer.dao.HocVienDAO;
import com.bachkhoa.codeanalyzer.dao.ThongKeDAO;
import com.bachkhoa.codeanalyzer.models.BaiNop;
import com.bachkhoa.codeanalyzer.models.HocVien;
import com.bachkhoa.codeanalyzer.models.ThongKeNick;
import com.bachkhoa.codeanalyzer.services.CodeforcesCrawler;
import com.bachkhoa.codeanalyzer.services.CrawlScheduler;
import com.bachkhoa.codeanalyzer.services.GeminiAnalyzer;
import com.bachkhoa.codeanalyzer.services.ReportExporter;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Vector;

public class MainForm extends JFrame {

    private JTabbedPane tabbedPane;
    private JTextArea txtConsole;

    // --- Tab 1: Quản lý Nick ---
    private JTable tbHocVien;
    private DefaultTableModel modelHocVien;
    private HocVienDAO hocVienDAO = new HocVienDAO();

    // --- Tab 2: Danh sách Bài nộp ---
    private JTable tbReport;
    private BaoCaoDAO baoCaoDAO = new BaoCaoDAO();
    private static final int CODE_COLUMN_INDEX = 5;

    // --- Tab 3: Đánh giá Tổng hợp ---
    private JTable tbThongKe;
    private DefaultTableModel modelThongKe;
    private ThongKeDAO thongKeDAO = new ThongKeDAO();

    // --- Tab 4: Lịch Crawl Tự Động ---
    private CrawlScheduler crawlScheduler;
    private JLabel lblSchedulerStatus;
    private JButton btnToggleScheduler;
    private JComboBox<String> cbInterval;

    public MainForm() {
        setTitle("Hệ Thống Đánh Giá Năng Lực Lập Trình");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Khởi tạo console dùng chung
        txtConsole = new JTextArea(8, 50);
        txtConsole.setEditable(false);
        txtConsole.setFont(new Font("Monospaced", Font.PLAIN, 13));
        txtConsole.setBackground(Color.BLACK);
        txtConsole.setForeground(Color.GREEN);
        
        crawlScheduler = new CrawlScheduler(txtConsole);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 14));

        tabbedPane.addTab("Quản Lý Nick", createTabQuanLyNick());
        tabbedPane.addTab("Danh Sách Bài Nộp", createTabDanhSachBaiNop());
        tabbedPane.addTab("Đánh Giá Tổng Hợp", createTabDanhGiaTongHop());
        tabbedPane.addTab("Lịch Crawl Tự Động", createTabLichCrawl());

        // Khi chuyển tab thì tự động reload data
        tabbedPane.addChangeListener(e -> {
            int idx = tabbedPane.getSelectedIndex();
            if (idx == 0) loadDataToTabHocVien();
            else if (idx == 1) loadDataToTabDanhSachBai();
            else if (idx == 2) loadDataToTabThongKe();
        });

        add(tabbedPane, BorderLayout.CENTER);

        JScrollPane scrollConsole = new JScrollPane(txtConsole);
        scrollConsole.setBorder(BorderFactory.createTitledBorder("Terminal Tiến Trình"));
        add(scrollConsole, BorderLayout.SOUTH);
    }

    // =====================================================================================
    // TAB 1: QUẢN LÝ NICK
    // =====================================================================================
    private JPanel createTabQuanLyNick() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Nút điều khiển
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Thêm Nick Mới");
        JButton btnDelete = new JButton("Xóa Nick Chọn");
        JButton btnCrawlNow = new JButton("Crawl Ngay Nick Chọn");
        
        topPanel.add(btnAdd);
        topPanel.add(btnDelete);
        topPanel.add(btnCrawlNow);
        panel.add(topPanel, BorderLayout.NORTH);

        // Bảng dữ liệu
        tbHocVien = new JTable();
        tbHocVien.setRowHeight(25);
        tbHocVien.setFont(new Font("Arial", Font.PLAIN, 14));
        loadDataToTabHocVien();
        panel.add(new JScrollPane(tbHocVien), BorderLayout.CENTER);

        // Sự kiện
        btnAdd.addActionListener(e -> {
            JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
            p.add(new JLabel("Handle Codeforces:"));
            JTextField txtHandle = new JTextField();
            p.add(txtHandle);

            int result = JOptionPane.showConfirmDialog(this, p, "Thêm Nick Mới", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION && !txtHandle.getText().trim().isEmpty()) {
                String handle = txtHandle.getText().trim();
                HocVien hv = new HocVien(handle, "Codeforces");
                if (hocVienDAO.addHocVien(hv)) {
                    loadDataToTabHocVien();
                    txtConsole.append(">>> Đã thêm nick Codeforces: " + handle + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi khi thêm nick!");
                }
            }
        });

        btnDelete.addActionListener(e -> {
            int row = tbHocVien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nick cần xóa.");
                return;
            }
            int id = (int) tbHocVien.getValueAt(row, 0);
            String handle = (String) tbHocVien.getValueAt(row, 1);
            
            int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa nick " + handle + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (hocVienDAO.deleteHocVien(id)) {
                    loadDataToTabHocVien();
                    txtConsole.append(">>> Đã xóa nick: " + handle + "\n");
                } else {
                    JOptionPane.showMessageDialog(this, "Không thể xóa nick do có dữ liệu liên quan. Vui lòng xóa dữ liệu bài nộp trước.");
                }
            }
        });

        btnCrawlNow.addActionListener(e -> {
            int row = tbHocVien.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn nick để crawl.");
                return;
            }
            int coderId = (int) tbHocVien.getValueAt(row, 0);
            String handle = (String) tbHocVien.getValueAt(row, 1);
            String platform = (String) tbHocVien.getValueAt(row, 2);

            if ("Codeforces".equals(platform)) {
                txtConsole.append(">>> Bắt đầu crawl thủ công cho: " + handle + "...\n");
                new Thread(() -> {
                    CodeforcesCrawler crawler = new CodeforcesCrawler();
                    crawler.fetchAndSaveRecentSubmissions(coderId, handle);
                    hocVienDAO.updateLastCrawled(coderId);
                    SwingUtilities.invokeLater(() -> {
                        txtConsole.append(">>> Crawl hoàn tất cho: " + handle + "\n");
                        loadDataToTabHocVien();
                    });
                }).start();
            } else {
                JOptionPane.showMessageDialog(this, "Chỉ hỗ trợ crawl Codeforces.");
            }
        });

        return panel;
    }

    private void loadDataToTabHocVien() {
        List<HocVien> list = hocVienDAO.getAllHocVien();
        Vector<String> columns = new Vector<>(List.of("ID", "Handle", "Nền Tảng", "Ngày Thêm", "Lần Cuối Crawl"));
        Vector<Vector<Object>> data = new Vector<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        for (HocVien hv : list) {
            Vector<Object> row = new Vector<>();
            row.add(hv.getId());
            row.add(hv.getHandleName());
            row.add(hv.getPlatform());
            row.add(hv.getAddedDate() != null ? sdf.format(hv.getAddedDate()) : "");
            row.add(hv.getLastCrawledAt() != null ? sdf.format(hv.getLastCrawledAt()) : "Chưa từng crawl");
            data.add(row);
        }

        modelHocVien = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbHocVien.setModel(modelHocVien);
    }

    // =====================================================================================
    // TAB 2: DANH SÁCH BÀI NỘP
    // =====================================================================================
    private JPanel createTabDanhSachBaiNop() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAnalyze = new JButton("Gửi AI Phân Tích Các Bài Mới");
        topPanel.add(btnAnalyze);
        
        JLabel hint = new JLabel("  (Double-click vào dòng để xem Source Code)");
        hint.setFont(new Font("Arial", Font.ITALIC, 12));
        hint.setForeground(Color.GRAY);
        topPanel.add(hint);
        panel.add(topPanel, BorderLayout.NORTH);

        tbReport = new JTable();
        tbReport.setRowHeight(25);
        tbReport.setFont(new Font("Arial", Font.PLAIN, 13));
        tbReport.setSelectionBackground(new Color(173, 216, 230));
        loadDataToTabDanhSachBai();
        
        panel.add(new JScrollPane(tbReport), BorderLayout.CENTER);

        // Sự kiện Double-click: Xem Source Code
        tbReport.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tbReport.getSelectedRow();
                    if (row < 0) return;
                    String handle = (String) tbReport.getValueAt(row, 1);
                    String problemName = (String) tbReport.getValueAt(row, 2);
                    String code = (String) tbReport.getValueAt(row, CODE_COLUMN_INDEX);
                    showCodeDialog(handle, problemName, code);
                }
            }
        });

        // Nút phân tích AI
        btnAnalyze.addActionListener(e -> {
            txtConsole.append(">>> Đang quét CSDL tìm các bài chưa phân tích...\n");
            new Thread(() -> {
                BaiNopDAO bnDao = new BaiNopDAO();
                List<BaiNop> dsChuaPhanTich = bnDao.getUnanalyzedSubmissions();

                if (dsChuaPhanTich.isEmpty()) {
                    SwingUtilities.invokeLater(() -> txtConsole.append(">>> Không có bài nộp nào mới cần phân tích!\n"));
                    return;
                }

                GeminiAnalyzer analyzer = new GeminiAnalyzer();
                for (BaiNop bn : dsChuaPhanTich) {
                    SwingUtilities.invokeLater(() -> txtConsole.append(">>> Đang gọi AI đọc bài: " + bn.getProblemName() + "...\n"));
                    try {
                        analyzer.analyzeAndSave(bn.getId(), bn.getCodeContent());
                        Thread.sleep(5000); // Tránh quota limit
                    } catch (Exception ex) {
                        System.out.println("Lỗi xử lý AI: " + ex.getMessage());
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    txtConsole.append(">>> Phân tích xong toàn bộ!\n");
                    loadDataToTabDanhSachBai();
                });
            }).start();
        });

        return panel;
    }

    private void loadDataToTabDanhSachBai() {
        DefaultTableModel model = baoCaoDAO.getBaoCaoModel();
        tbReport.setModel(model);

        if (tbReport.getColumnCount() > CODE_COLUMN_INDEX) {
            TableColumn codeCol = tbReport.getColumnModel().getColumn(CODE_COLUMN_INDEX);
            codeCol.setMinWidth(0);
            codeCol.setMaxWidth(0);
            codeCol.setWidth(0);
            codeCol.setPreferredWidth(0);
        }
    }

    private void showCodeDialog(String handle, String problemName, String code) {
        JDialog dialog = new JDialog(this, "Source Code — " + handle + " — " + problemName, true);
        dialog.setSize(850, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("  " + handle + "  |  " + problemName, JLabel.LEFT);
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setOpaque(true);
        title.setBackground(new Color(40, 44, 52));
        title.setForeground(new Color(171, 178, 191));
        title.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
        dialog.add(title, BorderLayout.NORTH);

        JTextArea codeArea = new JTextArea(code);
        codeArea.setEditable(false);
        codeArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        codeArea.setBackground(new Color(40, 44, 52));
        codeArea.setForeground(new Color(171, 178, 191));
        codeArea.setMargin(new Insets(8, 12, 8, 8));
        dialog.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        dialog.setVisible(true);
    }

    // =====================================================================================
    // TAB 3: ĐÁNH GIÁ TỔNG HỢP
    // =====================================================================================
    private JPanel createTabDanhGiaTongHop() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnExport = new JButton("Xuất Báo Cáo HTML");
        btnExport.setBackground(new Color(46, 204, 113));
        btnExport.setForeground(Color.WHITE);
        topPanel.add(btnExport);
        panel.add(topPanel, BorderLayout.NORTH);

        tbThongKe = new JTable();
        tbThongKe.setRowHeight(30);
        tbThongKe.setFont(new Font("Arial", Font.PLAIN, 14));
        loadDataToTabThongKe();
        panel.add(new JScrollPane(tbThongKe), BorderLayout.CENTER);

        btnExport.addActionListener(e -> {
            List<ThongKeNick> ds = thongKeDAO.getThongKeAllNicks();
            if (ds.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không có dữ liệu để xuất.");
                return;
            }
            String path = ReportExporter.exportHtmlReport(ds);
            if (path != null) {
                txtConsole.append(">>> Đã xuất báo cáo tại: " + path + "\n");
                try {
                    Desktop.getDesktop().open(new File(path));
                } catch (Exception ex) {
                    txtConsole.append("Không thể tự mở file HTML. Hãy mở thủ công.\n");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi xuất báo cáo.");
            }
        });

        return panel;
    }

    private void loadDataToTabThongKe() {
        List<ThongKeNick> list = thongKeDAO.getThongKeAllNicks();
        Vector<String> columns = new Vector<>(List.of("Handle", "Platform", "Tổng Bài", "Đã Phân Tích", "TB AI (%)", "Thuật Toán Chính", "Mức Rủi Ro"));
        Vector<Vector<Object>> data = new Vector<>();
        
        for (ThongKeNick tk : list) {
            Vector<Object> row = new Vector<>();
            row.add(tk.getHandleName());
            row.add(tk.getPlatform());
            row.add(tk.getTotalSubmissions());
            row.add(tk.getAnalyzedCount());
            row.add(String.format("%.1f%%", tk.getAvgAiProbability() * 100));
            row.add(tk.getTopAlgorithms());
            row.add(tk.getAiRiskLevel());
            data.add(row);
        }

        modelThongKe = new DefaultTableModel(data, columns) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tbThongKe.setModel(modelThongKe);

        // Highlight màu sắc cho mức rủi ro
        tbThongKe.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 6) { // Cột Mức Rủi Ro
                    String risk = (String) value;
                    if ("Thấp".equals(risk)) c.setForeground(new Color(39, 174, 96));
                    else if ("Trung bình".equals(risk)) c.setForeground(new Color(243, 156, 18));
                    else if ("Cao".equals(risk)) c.setForeground(new Color(192, 57, 43));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        });
    }

    // =====================================================================================
    // TAB 4: LỊCH CRAWL TỰ ĐỘNG
    // =====================================================================================
    private JPanel createTabLichCrawl() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Cài Đặt Scheduler Tự Động Cào Code");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        lblSchedulerStatus = new JLabel("Trạng thái: ĐANG TẮT");
        lblSchedulerStatus.setForeground(Color.RED);
        lblSchedulerStatus.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(lblSchedulerStatus, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panel.add(new JLabel("Chu kỳ (Giờ):"), gbc);

        cbInterval = new JComboBox<>(new String[]{"6", "12", "24"});
        cbInterval.setSelectedItem("24");
        gbc.gridx = 1; gbc.gridy = 2;
        panel.add(cbInterval, gbc);

        btnToggleScheduler = new JButton("BẬT Auto Crawl");
        btnToggleScheduler.setBackground(new Color(46, 204, 113));
        btnToggleScheduler.setForeground(Color.WHITE);
        btnToggleScheduler.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.ipady = 10;
        panel.add(btnToggleScheduler, gbc);

        JLabel lblInfo = new JLabel("<html>Lưu ý: Hệ thống sẽ tự quét tất cả các nick Codeforces<br>trong tab 'Quản Lý Nick' theo chu kỳ để tìm bài nộp mới.</html>");
        lblInfo.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(lblInfo, gbc);

        btnToggleScheduler.addActionListener(e -> {
            if (crawlScheduler.isRunning()) {
                crawlScheduler.stop();
                lblSchedulerStatus.setText("Trạng thái: ĐANG TẮT");
                lblSchedulerStatus.setForeground(Color.RED);
                btnToggleScheduler.setText("BẬT Auto Crawl");
                btnToggleScheduler.setBackground(new Color(46, 204, 113));
                cbInterval.setEnabled(true);
            } else {
                int hours = Integer.parseInt((String) cbInterval.getSelectedItem());
                crawlScheduler.start(hours);
                lblSchedulerStatus.setText("Trạng thái: ĐANG CHẠY (Mỗi " + hours + "h)");
                lblSchedulerStatus.setForeground(new Color(39, 174, 96));
                btnToggleScheduler.setText("TẮT Auto Crawl");
                btnToggleScheduler.setBackground(new Color(231, 76, 60));
                cbInterval.setEnabled(false);
            }
        });

        // Đẩy lên trên cùng
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(panel, BorderLayout.NORTH);
        return wrapper;
    }
}