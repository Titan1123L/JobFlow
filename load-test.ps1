param(
    [int]$JobCount = 30,
    [string]$ApiKey = "X3PcDfop4cVliuCUNFgqdiJVF5fhjsApE0eNVQWmzlY="
)

$headers = @{ "X-API-Key" = $ApiKey }
$body = '{"jobType":"SEND_EMAIL","payload":{"to":"loadtest@example.com","template":"welcome"}}'

Write-Host "Submitting $JobCount jobs..."
$startSubmit = Get-Date
for ($i = 0; $i -lt $JobCount; $i++) {
    Invoke-RestMethod -Uri "http://localhost:8080/api/jobs" -Method POST -ContentType "application/json" -Headers $headers -Body $body | Out-Null
}
$submitDuration = (Get-Date) - $startSubmit
Write-Host "Submitted $JobCount jobs in $($submitDuration.TotalSeconds) seconds"

Write-Host "Waiting for all jobs to complete..."
$startWait = Get-Date
while ($true) {
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/stats" -Headers $headers
    $pending = $stats.queuedJobs + $stats.processingJobs
    if ($pending -eq 0) { break }
    Start-Sleep -Milliseconds 500
}
$processDuration = (Get-Date) - $startWait

Write-Host ""
Write-Host "=== RESULTS ==="
Write-Host "Jobs submitted: $JobCount"
Write-Host "Processing time: $($processDuration.TotalSeconds) seconds"
Write-Host "Throughput: $([math]::Round($JobCount / $processDuration.TotalSeconds, 2)) jobs/sec"