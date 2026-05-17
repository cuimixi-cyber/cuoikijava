@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ===================================================
echo   DANG KHOI DONG CODEFORCES ANALYZER...
echo ===================================================

:: Buoc 1: Kiem tra Java JDK
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [Loi] May tinh cua ban chua cai dat Java hoac chua cau hinh bien moi truong PATH.
    echo Vui long tai va cai dat JDK 21 truoc khi tiep tuc.
    pause
    exit /b
)

:: Buoc 2: Tim va xac dinh vi tri cua Maven
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    set "MVN_CMD=C:\Users\admin\.m2\wrapper\dists\apache-maven-3.9.6-bin\3311e1d4\apache-maven-3.9.6\bin\mvn.cmd"
)

if not exist "%MVN_CMD%" (
    echo [Loi] Khong tim thay Maven tren may tinh. Vui long cai dat Maven hoac chay qua IDE.
    pause
    exit /b
)

:: Buoc 3: Tai va dong bo tat ca cac library dependency vao target/dependency
echo [+] Dang tai va dong bo cac thu vien lien quan (bao gom Selenium)...
call "%MVN_CMD%" dependency:copy-dependencies -DoutputDirectory=target\dependency >nul 2>&1
if %errorlevel% neq 0 (
    echo [+] Thu tai dependency lai voi che do hien thi chi tiet...
    call "%MVN_CMD%" dependency:copy-dependencies -DoutputDirectory=target\dependency
    if %errorlevel% neq 0 (
        echo [Loi] Tai cac thu vien that bai!
        pause
        exit /b
    )
)

:: Buoc 4: Bien dich du an bang Maven
echo [+] Dang bien dich du an...
call "%MVN_CMD%" compile
if %errorlevel% neq 0 (
    echo [Loi] Bien dich du an that bai! Vui long kiem tra lai ma nguon.
    pause
    exit /b
)

echo [Thanh cong] Bien dich hoan tat!
echo ===================================================
echo [+] Dang chay ung dung...

:: Buoc 5: Chay chuong trinh voi library path ho tro Windows Auth va toan bo jar trong target/dependency
java -Djava.library.path=. -cp "target\classes;target\dependency\*" com.bachkhoa.codeanalyzer.Main

pause
