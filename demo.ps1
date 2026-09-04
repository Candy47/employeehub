# ============================================================================
#  EmployeeHub - end-to-end demo (works on any IntelliJ / no plugins needed).
#  Run AFTER infrastructure + all 4 services are up:
#     docker compose -f infrastructure/docker-compose.yml up -d
#     ./run.ps1
#  Usage:  ./demo.ps1
# ============================================================================

$ErrorActionPreference = "Stop"
$gw = "http://localhost:8090"

# A unique email each run so "register" never collides.
$email = "asha+$((Get-Date).ToString('HHmmss'))@example.com"

function Section($text) {
    Write-Host ""
    Write-Host ("=" * 62) -ForegroundColor DarkCyan
    Write-Host "  $text" -ForegroundColor White
    Write-Host ("=" * 62) -ForegroundColor DarkCyan
}

# --- 1) Register ------------------------------------------------------------
Section "1) Register user  ($email)"
$registerBody = @{ fullName = "Asha Rao"; email = $email; password = "Password123" } | ConvertTo-Json
$register = Invoke-RestMethod -Uri "$gw/api/v1/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
$register | Format-List

# --- 2) Login (captures the httpOnly jwt cookie into $session) ---------------
Section "2) Login (stores JWT cookie)"
$loginBody = @{ email = $email; password = "Password123" } | ConvertTo-Json
$login = Invoke-RestMethod -Uri "$gw/api/v1/auth/login" -Method Post -Body $loginBody -ContentType "application/json" -SessionVariable session
$login | Format-List

# Debezium (CDC) is asynchronous - give the event a moment to flow through
# Kafka so employee-service can auto-create the profile.
Write-Host "   waiting a few seconds for the event to propagate..." -ForegroundColor DarkGray
Start-Sleep -Seconds 5

# --- 3) List employees (protected - uses the cookie) ------------------------
Section "3) Employees (auto-created from the UserRegistered event)"
$employees = Invoke-RestMethod -Uri "$gw/api/v1/employees" -Method Get -WebSession $session
$employees | Format-Table id, userId, email, department, status -AutoSize

$mine = $employees | Where-Object { $_.email -eq $email } | Select-Object -First 1
if (-not $mine) { throw "Profile not found yet - Debezium/Kafka may need a moment. Re-run or increase the sleep." }

# --- 4) Complete / update the profile (emits EmployeeUpdated) ---------------
Section "4) Update profile id=$($mine.id)  (emits EmployeeUpdated)"
$updateBody = @{ department = "Engineering"; designation = "Backend Engineer"; status = "ACTIVE" } | ConvertTo-Json
$updated = Invoke-RestMethod -Uri "$gw/api/v1/employees/$($mine.id)" -Method Put -Body $updateBody -ContentType "application/json" -WebSession $session
$updated | Format-List

# --- 5) My own profile ------------------------------------------------------
Section "5) My profile (/api/v1/employees/me)"
Invoke-RestMethod -Uri "$gw/api/v1/employees/me" -Method Get -WebSession $session | Format-List

Start-Sleep -Seconds 3

# --- 6) Audit log (shows fan-out) -------------------------------------------
Section "6) Audit log"
$auditLog = Invoke-RestMethod -Uri "$gw/api/v1/audit" -Method Get -WebSession $session
$auditLog | Format-Table id, subject, eventType, sourceTopic -AutoSize

# --- 7) Unauthorized check --------------------------------------------------
Section "7) Protected route WITHOUT login should be 401"
try {
    Invoke-RestMethod -Uri "$gw/api/v1/employees" -Method Get | Out-Null
    Write-Host "   Unexpected: request succeeded without auth!" -ForegroundColor Red
} catch {
    Write-Host "   Got $($_.Exception.Response.StatusCode.value__) Unauthorized as expected." -ForegroundColor Green
}

Write-Host ""
Write-Host "Demo complete. Check each service window for the event logs." -ForegroundColor Green
Write-Host ""

