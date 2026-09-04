# ============================================================================
#  EmployeeHub - launch all microservices, each in its own window.
#  Docker infra is separate:
#    docker compose -f infrastructure/docker-compose.yml up -d
#  Usage:  ./run.ps1
# ============================================================================

Set-Location $PSScriptRoot

# --- Services ----------------------------------------------------------------
$services = @(
    [pscustomobject]@{ Name = "api-gateway";          Port = 8090; Color = "Green"   }
    [pscustomobject]@{ Name = "auth-service";         Port = 8080; Color = "Cyan"    }
    [pscustomobject]@{ Name = "employee-service";      Port = 8082; Color = "Yellow"  }
    [pscustomobject]@{ Name = "audit-service";        Port = 8081; Color = "Magenta" }
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

# --- Register Debezium outbox connectors (idempotent) ------------------------
#  Done here (not in docker-compose) because a connector can only be created
#  AFTER the owning service has created its outbox_events table on startup.
$connectUrl = "http://localhost:8083"
$connectors = @(
    [pscustomobject]@{ Name = "employeehub-outbox-connector";          File = "infrastructure\debezium-postgres-connector.json";  ReadyUrl = "http://localhost:8080/actuator/health" }
    [pscustomobject]@{ Name = "employeehub-employee-outbox-connector"; File = "infrastructure\debezium-employee-connector.json";  ReadyUrl = "http://localhost:8082/actuator/health" }
)

function Wait-Url {
    param([string]$Url, [int]$TimeoutSec = 180)
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        try { Invoke-RestMethod -Uri $Url -TimeoutSec 3 -ErrorAction Stop | Out-Null; return $true }
        catch { Start-Sleep -Seconds 3 }
    }
    return $false
}

Write-Host "   Waiting for Kafka Connect ($connectUrl)..." -ForegroundColor DarkGray
if (Wait-Url "$connectUrl/connectors") {

    $existing = @()
    try { $existing = Invoke-RestMethod -Uri "$connectUrl/connectors" -ErrorAction Stop } catch {}

    foreach ($c in $connectors) {
        if ($existing -contains $c.Name) {
            Write-Host "   [skip] $($c.Name) already registered" -ForegroundColor DarkGray
            continue
        }

        # Ensure the owning service is up so its outbox_events table exists.
        Write-Host "   Waiting for $($c.ReadyUrl) ..." -ForegroundColor DarkGray
        if (-not (Wait-Url $c.ReadyUrl)) {
            Write-Host "   [warn] service for $($c.Name) not healthy yet - skipping (re-run ./run.ps1 later)" -ForegroundColor Yellow
            continue
        }

        try {
            Invoke-RestMethod -Uri "$connectUrl/connectors" -Method Post -ContentType "application/json" `
                -InFile (Join-Path $PSScriptRoot $c.File) -ErrorAction Stop | Out-Null
            Write-Host "   [ok]   registered $($c.Name)" -ForegroundColor Green
        } catch {
            Write-Host "   [warn] could not register $($c.Name): $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "   [warn] Kafka Connect not reachable. Is 'docker compose up -d' running?" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "   Ready. Gateway entry point: http://localhost:8090" -ForegroundColor Green
Write-Host ""










