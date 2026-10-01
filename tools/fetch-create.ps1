param([string]$ModsDirectory = "mods")
$ErrorActionPreference = "Stop"
$Name = "create-1.21.1-6.0.10.jar"
$Url = "https://cdn.modrinth.com/data/LNytGWDc/versions/UjX6dr61/$Name"
$Expected = "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a"
New-Item -ItemType Directory -Force -Path $ModsDirectory | Out-Null
$Target = Join-Path $ModsDirectory $Name
if (Test-Path $Target) {
    if ((Get-FileHash -Algorithm SHA256 $Target).Hash.ToLower() -eq $Expected) { Write-Host "Already verified: $Target"; exit 0 }
    throw "Refusing to overwrite unexpected existing file: $Target"
}
$Temp = "$Target.part"
Invoke-WebRequest -Uri $Url -OutFile $Temp
if ((Get-FileHash -Algorithm SHA256 $Temp).Hash.ToLower() -ne $Expected) { Remove-Item $Temp; throw "Checksum mismatch; nothing installed" }
Move-Item $Temp $Target
Write-Host "Installed and verified: $Target"
