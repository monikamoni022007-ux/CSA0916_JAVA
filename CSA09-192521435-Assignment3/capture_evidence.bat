@echo off
echo ================================================================================
echo   JAVA ASSIGNMENT 3 (CO3) - TERMINAL EVIDENCE & SCREENSHOT RUNNER
echo ================================================================================
echo.
echo Choose an option:
echo   [1] Run Question 1 (ATM Simulation)
echo   [2] Run Question 2 (Online Exam Portal & StorageDemo)
echo   [3] Run Question 3 (Daily Sales Report & Suppressed Demo)
echo   [4] Run Question 4 (Parallel Scanner Stations & Experiments)
echo   [5] Run Question 5 (Seat Booking Race & 10k Benchmark)
echo   [6] Run Question 6 (Token Counter & Controlled Deadlock Demo)
echo   [7] Run Question 7 (10,000-File Processor & Pool Benchmarks)
echo   [8] Run ALL Questions Sequentially
echo   [9] Exit
echo.
set /p choice="Enter option (1-9): "

if "%choice%"=="1" (
    cd /d "%~dp0Q1"
    javac *.java
    if errorlevel 1 goto compile_error
    java AtmSimulation
    java CatchOrderCompilerErrorDemo
    pause
)
if "%choice%"=="2" (
    cd /d "%~dp0Q2"
    javac *.java
    if errorlevel 1 goto compile_error
    java StorageDemo
    java OnlineExamPortal
    pause
)
if "%choice%"=="3" (
    cd /d "%~dp0Q3"
    javac *.java
    if errorlevel 1 goto compile_error
    java SafeReportGenerator
    java SuppressedExceptionDemo
    pause
)
if "%choice%"=="4" (
    cd /d "%~dp0Q4"
    javac *.java
    if errorlevel 1 goto compile_error
    java ParallelScannerSimulation
    java RunVsStartDemo
    java StudentBExperiments
    pause
)
if "%choice%"=="5" (
    cd /d "%~dp0Q5"
    javac *.java
    if errorlevel 1 goto compile_error
    java SeatBooking
    java UnsynchronizedSeatBooking
    java CinemaSyncComparison
    pause
)
if "%choice%"=="6" (
    cd /d "%~dp0Q6"
    javac *.java
    if errorlevel 1 goto compile_error
    java TokenCounter
    java HostelWalletSafeTransfer
    java HostelWalletDeadlockDemo
    pause
)
if "%choice%"=="7" (
    cd /d "%~dp0Q7"
    javac *.java
    if errorlevel 1 goto compile_error
    java SemesterResultProcessor
    pause
)
if "%choice%"=="8" (
    echo Running All Tasks...
    cd /d "%~dp0Q1"
    javac *.java
    if errorlevel 1 goto compile_error
    java AtmSimulation
    cd /d "%~dp0Q2"
    javac *.java
    if errorlevel 1 goto compile_error
    java StorageDemo
    cd /d "%~dp0Q3"
    javac *.java
    if errorlevel 1 goto compile_error
    java SafeReportGenerator
    cd /d "%~dp0Q4"
    javac *.java
    if errorlevel 1 goto compile_error
    java ParallelScannerSimulation
    cd /d "%~dp0Q5"
    javac *.java
    if errorlevel 1 goto compile_error
    java SeatBooking
    cd /d "%~dp0Q6"
    javac *.java
    if errorlevel 1 goto compile_error
    java TokenCounter
    cd /d "%~dp0Q7"
    javac *.java
    if errorlevel 1 goto compile_error
    java SemesterResultProcessor
    pause
)
exit /b 0

:compile_error
echo Compilation failed. Check the Java compiler output above.
pause
exit /b 1
