# Hệ Thống Đánh Giá Năng Lực Lập Trình (Codeforces AI Analyzer)

Đây là hệ thống quản lý, thu thập tự động mã nguồn từ Codeforces và sử dụng Trí Tuệ Nhân Tạo (Google Gemini) để phân tích năng lực lập trình, thuật toán và mức độ phụ thuộc vào AI của sinh viên.

---

## 🌟 Các Tính Năng Chính
1. **Quản Lý Nick**: Thêm, xóa và theo dõi danh sách sinh viên / nick Codeforces.
2. **Auto Crawl (Cào dữ liệu tự động)**: Lên lịch cào mã nguồn bài tập tự động theo chu kỳ (6h, 12h, 24h).
3. **Phân Tích AI**: Quét mã nguồn và yêu cầu Gemini phân tích thuật toán, độ tin cậy tự code (chống gian lận AI).
4. **Đánh Giá Tổng Hợp**: Thống kê mức độ rủi ro, số bài tập, và thuật toán chủ đạo cho từng nick.
5. **Xuất Báo Cáo HTML**: Kết xuất báo cáo đánh giá năng lực lập trình ra định dạng HTML trực quan.

---

## 🛠 Yêu Cầu Hệ Thống
- **Java**: JDK 21 (hoặc tương đương)
- **Cơ Sở Dữ Liệu**: SQL Server (Bật chế độ TCP/IP và Windows Authentication)
- **Trình Duyệt**: Microsoft Edge (Để tự động vượt qua Cloudflare của Codeforces)
- **Web Driver**: `msedgedriver.exe` (Phải cùng phiên bản với Microsoft Edge đang cài trên máy)
- **Build Tool**: Maven

---

## 🚀 Hướng Dẫn Cài Đặt Chi Tiết

### Bước 1: Cấu Hình Cơ Sở Dữ Liệu SQL Server
Hệ thống sử dụng SQL Server làm nơi lưu trữ dữ liệu. Hãy đảm bảo bạn đã cài đặt SQL Server và SQL Server Management Studio (SSMS).
1. Mở **SQL Server Configuration Manager**, chọn **SQL Server Network Configuration** > **Protocols for MSSQLSERVER**. Đảm bảo **TCP/IP** đã được **Enabled** (Bật).
2. Khởi động lại service SQL Server nếu bạn vừa bật TCP/IP.
3. Mở **SSMS**, kết nối bằng **Windows Authentication**.
4. Mở một New Query và chạy lệnh tạo Database:
   ```sql
   CREATE DATABASE CodeAnalyzerDB;
   ```
5. Mở file `setup_db.sql` được đính kèm trong thư mục gốc của project bằng SSMS, chọn database `CodeAnalyzerDB` và nhấn **Execute (F5)** để tạo toàn bộ bảng dữ liệu cần thiết.
*Lưu ý: Nếu bạn sử dụng tài khoản `sa` thay vì Windows Authentication, hãy mở file `src/main/java/com/bachkhoa/codeanalyzer/dao/DatabaseConnection.java` và cấu hình lại Connection String với `user` và `password` tương ứng.*

### Bước 2: Cài Đặt Web Driver (Dùng để giả lập trình duyệt)
Hệ thống cần EdgeDriver để tự động bật trình duyệt Edge và vượt qua tường lửa Cloudflare của Codeforces.
1. Mở trình duyệt Microsoft Edge trên máy bạn, gõ lên thanh địa chỉ `edge://settings/help` để xem phiên bản Edge hiện tại (Ví dụ: `125.0.2535.51`).
2. Truy cập trang web: [Microsoft Edge Developer](https://developer.microsoft.com/en-us/microsoft-edge/tools/webdriver/)
3. Tải xuống file `msedgedriver` có **phiên bản khớp** (hoặc gần nhất) với phiên bản trình duyệt Edge của bạn.
4. Giải nén file vừa tải, lấy file `msedgedriver.exe`.
5. Đặt file `msedgedriver.exe` vào **ngay trong thư mục gốc** của project này (Nằm cùng vị trí với file `run.bat`).

### Bước 3: Đăng ký và Cấu Hình API Key AI (Google Gemini)
Để hệ thống có thể dùng AI phân tích thuật toán, bạn cần cấp cho nó một chiếc "chìa khóa" (API Key) từ Google.
1. Truy cập [Google AI Studio](https://aistudio.google.com/) và đăng nhập bằng tài khoản Google của bạn.
2. Nhấn vào **Get API key** ở menu bên trái -> chọn **Create API key**.
3. Copy đoạn mã API Key vừa được tạo.
4. Mở file mã nguồn tại đường dẫn: `src/main/java/com/bachkhoa/codeanalyzer/services/GeminiAnalyzer.java`.
5. Tìm dòng khai báo `API_KEY` (khoảng dòng 17) và dán API Key của bạn thay thế cho dòng hiện tại:
   ```java
   private static final String API_KEY = "DÁN_API_KEY_CỦA_BẠN_VÀO_ĐÂY";
   ```
6. Lưu file lại. Đến đây, công tác cài đặt đã hoàn tất!

---

## ▶️ Hướng Dẫn Khởi Chạy Chương Trình

1. Mở thư mục chứa toàn bộ dự án.
2. Nhấn đúp chuột (Double-click) vào file **`run.bat`**.
3. Một cửa sổ dòng lệnh đen (Command Prompt) sẽ hiện lên. Script tự động hóa này sẽ làm các việc sau:
   - Kiểm tra xem máy bạn đã cài đủ Java JDK và Maven chưa.
   - Tự động gọi Maven tải toàn bộ các thư viện (Selenium, JDBC, Gson,...) về máy (lần đầu sẽ hơi lâu).
   - Tiến hành dịch mã nguồn và khởi chạy Giao diện Windows Form của phần mềm.
   
   *Lưu ý: Không tắt cửa sổ dòng lệnh đen trong suốt quá trình sử dụng phần mềm, nó chính là Terminal để ghi lại các log hoạt động và lịch sử phân tích của AI.*

---

## 📖 Hướng Dẫn Sử Dụng Trọn Bộ (Quy Trình 4 Bước)

Hệ thống được thiết kế theo luồng làm việc tuyến tính với **4 Tab chức năng**. Dưới đây là quy trình chuẩn để bạn sử dụng:

### Bước 1 (Tab 1): Quản Lý & Nhập Nick
- Vào mục **"Thêm Nick Mới"** để gõ các tài khoản Codeforces của sinh viên bạn muốn theo dõi.
- **Lần đầu tiên cào dữ liệu**: Hãy chọn một nick và nhấn **Crawl Ngay Nick Chọn**. 
  - Trình duyệt Edge sẽ tự động nhảy lên và truy cập Codeforces.
  - **RẤT QUAN TRỌNG**: Nếu Codeforces hiện màn hình "Verify you are human" (Kiểm tra bảo mật Cloudflare), bạn hãy **thao tác bằng tay** (click vào ô tick) trên trình duyệt đó. Sau khi vào được trang web bình thường, quay lại phần mềm và nhấn nút **OK** trên hộp thoại thông báo để hệ thống tự động cào mã nguồn.

### Bước 2 (Tab 4): Đặt Lịch Auto Crawl (Tùy chọn)
- Nếu bạn không muốn ngày nào cũng phải bấm bằng tay, hãy sang Tab 4 **Lịch Crawl Tự Động**.
- Chọn chu kỳ quét mã (ví dụ: Mỗi 24 giờ).
- Nhấn **BẬT Auto Crawl**. Thu nhỏ phần mềm xuống Taskbar và để máy tính bật, cứ đúng giờ đó hệ thống sẽ tự động lên Codeforces kéo các bài tập mới nhất của TẤT CẢ sinh viên về máy mà bạn không cần phải làm gì thêm.

### Bước 3 (Tab 2): Kích Hoạt AI Phân Tích Mã Nguồn
- Mở **Danh Sách Bài Nộp**. Tại đây bạn sẽ thấy tất cả các file code C++, Java... mà hệ thống đã kéo về.
- Bạn có thể **Double-click (Click đúp)** vào một dòng bất kỳ để tận mắt xem đoạn code mà sinh viên đó viết.
- Nhấn nút **"Gửi AI Phân Tích Các Bài Mới"**. 
  - Phần mềm sẽ đóng gói từng file code gửi lên Google Gemini.
  - Để tránh bị Google phạt khóa tài khoản (Quota Exceeded) do dùng miễn phí, phần mềm đã lập trình sẵn cơ chế tự nghỉ 4-5 giây sau mỗi bài. Bạn chỉ cần treo máy để AI tự đọc hàng trăm file code.

### Bước 4 (Tab 3): Xem Đánh Giá Tổng Hợp & Xuất Báo Cáo
- Sau khi AI phân tích xong, hãy mở **Đánh Giá Tổng Hợp**.
- Bảng này sẽ tổng hợp lại thành tích của từng sinh viên:
  - **Thuật toán nổi bật**: Giúp giảng viên/người quản lý biết sinh viên này mạnh về mảng nào (Ví dụ: Quy hoạch động, Đồ thị, hay chỉ biết duyệt mảng cơ bản).
  - **Mức Rủi Ro**:
    - 🟢 **Thấp**: Chúc mừng, code mang phong cách cá nhân, khả năng cao là tự code 100%.
    - 🟡 **Trung Bình**: Có thể tham khảo ý tưởng trên mạng hoặc dùng công cụ AI hỗ trợ gõ một vài cấu trúc nhỏ.
    - 🔴 **Cao**: Logic code không nhất quán, cấu trúc phức tạp bất thường, hoặc phong cách lập trình giống y hệt văn phong của ChatGPT/Gemini tạo ra. Rất có thể sinh viên đã chép giải trọn vẹn.
- Nhấn **Xuất Báo Cáo HTML**. Phần mềm sẽ xuất ra một file Web (`BaoCao_DanhGia_...html`) ngay trong thư mục dự án. File báo cáo này có giao diện rất trực quan, có thể mang đi in, gửi email, hoặc báo cáo lên cấp trên cực kỳ chuyên nghiệp.

---

## 📝 Xử Lý Lỗi Thường Gặp
- **Lỗi không kết nối được Database**: Kiểm tra lại Connection String trong `DatabaseConnection.java`, đảm bảo SQL Server đang chạy và bật TCP/IP.
- **Lỗi Cloudflare Access Denied**: Hãy mở trình duyệt, đăng nhập Codeforces trước, giữ nguyên phiên làm việc và thử Crawl lại.
- **Lỗi Quota Exceeded (Gemini API)**: Đã vượt quá giới hạn API miễn phí của Google (15 requests/phút). Hãy đợi 1 phút và thử lại.
