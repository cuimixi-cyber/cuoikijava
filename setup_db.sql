-- Chạy các lệnh này trong SQL Server (SSMS) để tạo CSDL và Bảng
USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'CodeAnalyzerDB')
BEGIN
    CREATE DATABASE CodeAnalyzerDB;
END
GO

USE CodeAnalyzerDB;
GO

-- 1. Bảng Học Viên (Nick)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'HocVien')
BEGIN
    CREATE TABLE HocVien (
        Id INT PRIMARY KEY IDENTITY(1,1),
        HandleName NVARCHAR(100) NOT NULL,
        Platform NVARCHAR(50) DEFAULT 'Codeforces',
        AddedDate DATETIME DEFAULT GETDATE(),
        LastCrawledAt DATETIME NULL
    );
END
ELSE
BEGIN
    -- Thêm các cột nếu bảng đã tồn tại từ phiên bản cũ
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HocVien') AND name = 'Platform')
        ALTER TABLE HocVien ADD Platform NVARCHAR(50) DEFAULT 'Codeforces';
        
    IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('HocVien') AND name = 'LastCrawledAt')
        ALTER TABLE HocVien ADD LastCrawledAt DATETIME NULL;
END
GO

-- 2. Bảng Bài Nộp
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'BaiNop')
BEGIN
    CREATE TABLE BaiNop (
        Id INT PRIMARY KEY IDENTITY(1,1),
        CoderId INT NOT NULL,
        SubmissionId NVARCHAR(50) NOT NULL,
        ProblemName NVARCHAR(200) NOT NULL,
        CodeContent NVARCHAR(MAX),
        SubmitTime DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (CoderId) REFERENCES HocVien(Id) ON DELETE CASCADE
    );
END
GO

-- 3. Bảng Đánh Giá (AI)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'DanhGia')
BEGIN
    CREATE TABLE DanhGia (
        Id INT PRIMARY KEY IDENTITY(1,1),
        SubmissionId INT NOT NULL, -- Liên kết với Id của bảng BaiNop
        DsaTags NVARCHAR(500),
        AiGeneratedProbability FLOAT,
        AnalysisReport NVARCHAR(MAX),
        AnalysisTime DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (SubmissionId) REFERENCES BaiNop(Id) ON DELETE CASCADE
    );
END
GO

-- 4. Bảng Lịch Sử Crawl (Tùy chọn cho sau này nếu muốn track log chi tiết hơn)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CrawlLog')
BEGIN
    CREATE TABLE CrawlLog (
        Id INT PRIMARY KEY IDENTITY(1,1),
        CoderId INT,
        CrawlTime DATETIME DEFAULT GETDATE(),
        Status NVARCHAR(50),
        FOREIGN KEY (CoderId) REFERENCES HocVien(Id) ON DELETE CASCADE
    );
END
GO
