Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$backendDirectory = Split-Path -Parent $MyInvocation.MyCommand.Path
$envFile = Join-Path $backendDirectory '.env'
$exampleFile = Join-Path $backendDirectory '.env.example'

if (Test-Path -LiteralPath $envFile) {
    Write-Host ".env already exists. It was not overwritten."
    exit 0
}

Copy-Item -LiteralPath $exampleFile -Destination $envFile
$password = Read-Host 'Choose the JWT keystore password' -AsSecureString
$passwordText = [System.Net.NetworkCredential]::new('', $password).Password

$content = Get-Content -LiteralPath $envFile -Raw
$content = $content.Replace('JWT_KEY_STORE_PASSWORD=change-this-keystore-password', "JWT_KEY_STORE_PASSWORD=$passwordText")
Set-Content -LiteralPath $envFile -Value $content -NoNewline

Write-Host '.env created. Start the stack with: docker compose up --build'
