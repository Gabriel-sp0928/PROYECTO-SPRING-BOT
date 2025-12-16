$login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body (@{username='admin';password='admin123'} | ConvertTo-Json) -ContentType 'application/json' -TimeoutSec 10
if(-not $login.token){ Write-Output 'Admin login failed'; exit 1 }
Write-Output "TOKEN: $($login.token)"
$h = @{ Authorization = "Bearer $($login.token)" }
try {
  $resp = Invoke-WebRequest -Uri 'http://localhost:8080/api/dashboard' -Method Get -Headers $h -UseBasicParsing -TimeoutSec 10
  Write-Output ("Status: " + $resp.StatusCode)
  Write-Output $resp.Content
} catch {
  Write-Output ('Error: ' + $_.Exception.Message)
}
