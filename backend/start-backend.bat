@echo off
echo Starting ChronoSync Backend...
echo.

REM Try different ways to start the backend

REM Option 1: Check if mvn is in PATH
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Found Maven in PATH, starting...
    mvn spring-boot:run
    goto :end
)

REM Option 2: Check common Maven installation paths
if exist "C:\Program Files\Maven\apache-maven-3.9.9\bin\mvn.cmd" (
    echo Found Maven at C:\Program Files\Maven\apache-maven-3.9.9\bin
    "C:\Program Files\Maven\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
    goto :end
)

if exist "C:\apache-maven-3.9.9\bin\mvn.cmd" (
    echo Found Maven at C:\apache-maven-3.9.9\bin
    "C:\apache-maven-3.9.9\bin\mvn.cmd" spring-boot:run
    goto :end
)

REM Option 3: Try to run the compiled JAR if it exists
if exist "target\meeting-scheduler-backend-1.0.0.jar" (
    echo Running compiled JAR...
    java -jar target\meeting-scheduler-backend-1.0.0.jar
    goto :end
)

REM Option 4: Run from classes
if exist "target\classes" (
    echo Running from compiled classes...
    java -cp "target\classes;target\dependency\*" com.meetingscheduler.MeetingSchedulerApplication
    goto :end
)

echo ERROR: Cannot start backend. Maven not found and no compiled JAR exists.
echo.
echo Please install Maven or extract apache-maven-3.9.9-bin.zip to C:\apache-maven-3.9.9
echo Then add C:\apache-maven-3.9.9\bin to your PATH
echo.
pause

:end



