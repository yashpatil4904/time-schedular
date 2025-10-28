Write-Host "Starting ChronoSync Backend..." -ForegroundColor Green
Write-Host ""

# Check if Maven is installed
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue

if ($mvnCmd) {
    Write-Host "Found Maven, starting Spring Boot..." -ForegroundColor Cyan
    mvn spring-boot:run
    exit
}

# Check common Maven paths
$mavenPaths = @(
    "C:\Program Files\Maven\apache-maven-3.9.9\bin\mvn.cmd",
    "C:\apache-maven-3.9.9\bin\mvn.cmd",
    "$env:MAVEN_HOME\bin\mvn.cmd"
)

foreach ($path in $mavenPaths) {
    if (Test-Path $path) {
        Write-Host "Found Maven at: $path" -ForegroundColor Cyan
        & $path spring-boot:run
        exit
    }
}

# Try to run the JAR directly
if (Test-Path "target\meeting-scheduler-backend-1.0.0.jar") {
    Write-Host "Running compiled JAR..." -ForegroundColor Cyan
    java -jar target\meeting-scheduler-backend-1.0.0.jar
    exit
}

Write-Host "ERROR: Maven not found!" -ForegroundColor Red
Write-Host ""
Write-Host "Please do ONE of the following:" -ForegroundColor Yellow
Write-Host "1. Install Maven from https://maven.apache.org/download.cgi" -ForegroundColor White
Write-Host "2. Extract apache-maven-3.9.9-bin.zip to C:\apache-maven-3.9.9" -ForegroundColor White
Write-Host "3. Add Maven bin directory to your PATH" -ForegroundColor White
Write-Host ""
Write-Host "OR open this project in IntelliJ IDEA and click the Run button" -ForegroundColor Cyan



