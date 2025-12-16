try {
    $r = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body (@{username='client';password='client123'} | ConvertTo-Json) -ContentType 'application/json' -TimeoutSec 10
    $r | ConvertTo-Json -Compress
} catch {
    Write-Output ('Error: ' + $_.Exception.Message)
}
