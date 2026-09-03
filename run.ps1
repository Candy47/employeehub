# ============================================================================
#  EmployeeHub - launch all microservices, each in its own window.
#  Docker infra is separate:
#    docker compose -f infrastructure/docker-compose.yml up -d
#  Usage:  ./run.ps1
# ============================================================================

Set-Location $PSScriptRoot

# --- Services ----------------------------------------------------------------
$services = @(
    [pscustomobject]@{ Name = "auth-service";         Port = 8080; Color = "Cyan"   }
    [pscustomobject]@{ Name = "notification-service"; Port = 8081; Color = "Magenta" }
)

# --- JDK (project targets Java 21) -------------------------------------------
$jdk21 = "D:\Essentials Softwares\jdk-21.0.12.1+1"
if (Test-Path $jdk21) { $env:JAVA_HOME = $jdk21 }
$javaVer = (Split-Path $env:JAVA_HOME -Leaf)

# --- Banner ------------------------------------------------------------------
Write-Host ""
Write-Host "  +==========================================================+" -ForegroundColor DarkCyan
Write-Host "  |" -ForegroundColor DarkCyan -NoNewline
Write-Host "                  E M P L O Y E E   H U B                 " -ForegroundColor White -NoNewline
Write-Host "|" -ForegroundColor DarkCyan
Write-Host "  |" -ForegroundColor DarkCyan -NoNewline
Write-Host "                   microservices launcher                 " -ForegroundColor Gray -NoNewline
Write-Host "|" -ForegroundColor DarkCyan
Write-Host "  +==========================================================+" -ForegroundColor DarkCyan
Write-Host ""
Write-Host "   JDK        : " -ForegroundColor DarkGray -NoNewline; Write-Host $javaVer -ForegroundColor Green
Write-Host "   Services   : " -ForegroundColor DarkGray -NoNewline; Write-Host $services.Count -ForegroundColor Green
Write-Host ""

# --- Launch each service in its own window -----------------------------------
foreach ($svc in $services) {
    $dir = Join-Path $PSScriptRoot $svc.Name

    # Pretty header shown inside the service's own window (single line, ';'-joined).
    $bar   = "  " + ("=" * 52)
    $line1 = "Write-Host '$bar' -ForegroundColor $($svc.Color)"
    $line2 = "Write-Host '   $($svc.Name)  (http://localhost:$($svc.Port))' -ForegroundColor $($svc.Color)"
    $line3 = "Write-Host '$bar' -ForegroundColor $($svc.Color)"
    $header = "$line1; $line2; $line3; Write-Host ''"

    $cmd = "`$env:JAVA_HOME='$env:JAVA_HOME'; `$host.UI.RawUI.WindowTitle='$($svc.Name)'; Set-Location '$dir'; $header; .\mvnw.cmd spring-boot:run"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd

    Write-Host "   [ok] " -ForegroundColor Green -NoNewline
    Write-Host ("{0,-22}" -f $svc.Name) -ForegroundColor $svc.Color -NoNewline
    Write-Host "-> http://localhost:$($svc.Port)" -ForegroundColor DarkGray
}

Write-Host ""
Write-Host "   All services launched in separate windows." -ForegroundColor Green
Write-Host "   Close a window (or Ctrl+C in it) to stop that service." -ForegroundColor DarkGray
Write-Host ""










