# ================================================================================
# Coffee & More - PowerShell Launcher for Java Backend
# ================================================================================
Write-Host "================================================================================" -ForegroundColor DarkYellow
Write-Host "  COFFEE & MORE — JAVA BACKEND SERVER LAUNCHER" -ForegroundColor Yellow
Write-Host "================================================================================" -ForegroundColor DarkYellow

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "`n[1/2] Compiling Java classes..." -ForegroundColor Cyan
if (-not (Test-Path "backend\bin")) {
    New-Item -ItemType Directory -Force -Path "backend\bin" | Out-Null
}

javac -encoding UTF-8 -cp "backend/lib/*" -d backend/bin backend/src/com/coffeeandmore/*.java
if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Compilation failed." -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "[2/2] Starting server at http://localhost:8080/ ..." -ForegroundColor Green
Write-Host "Press Ctrl+C to stop the server.`n" -ForegroundColor DarkGray

java -cp "backend/bin;backend/lib/*" com.coffeeandmore.CoffeeAndMoreServer

