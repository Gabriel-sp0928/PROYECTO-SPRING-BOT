try {
  $login = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body (@{username='client';password='client123'} | ConvertTo-Json) -ContentType 'application/json' -TimeoutSec 10
  if(-not $login.token){ Write-Output 'Login failed or no token'; exit 1 }
  Write-Output "TOKEN: $($login.token)"

  $headers = @{ Authorization = "Bearer $($login.token)" }

  Write-Output "\nGET /api/quotes/my"
  try {
    $quotes = Invoke-RestMethod -Uri 'http://localhost:8080/api/quotes/my' -Method Get -Headers $headers -TimeoutSec 10
    Write-Output ($quotes | ConvertTo-Json -Compress)
  } catch { Write-Output ('Quotes error: ' + $_.Exception.Message) }

  Write-Output "\nGET /api/dashboard (expect 403 for client)"
  try {
    $dash = Invoke-RestMethod -Uri 'http://localhost:8080/api/dashboard' -Method Get -Headers $headers -TimeoutSec 10
    Write-Output ($dash | ConvertTo-Json -Compress)
  } catch { Write-Output ('Dashboard error: ' + $_.Exception.Message) }

} catch {
  Write-Output ('Error: ' + $_.Exception.Message)
}
