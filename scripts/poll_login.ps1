for ($i=1; $i -le 20; $i++) {
    try {
        $r = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -Body (@{username='client';password='client123'} | ConvertTo-Json) -ContentType 'application/json' -TimeoutSec 5
        Write-Output ($r | ConvertTo-Json -Compress)
        break
    } catch {
        Write-Output ("Attempt $i failed: " + $_.Exception.Message)
        Start-Sleep -Seconds 2
    }
}
