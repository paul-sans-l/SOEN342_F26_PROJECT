param(
    [Parameter(Position = 0)]
    [string]$AvailabilityCsv
)

$ErrorActionPreference = 'Stop'
$compiler = Get-Command javac -CommandType Application -ErrorAction SilentlyContinue | Select-Object -First 1
if (-not $compiler) {
    throw 'Install JDK 17 or newer and make java and javac available on PATH.'
}
$runtime = Join-Path (Split-Path -Parent $compiler.Source) 'java.exe'
if (-not (Test-Path -LiteralPath $runtime -PathType Leaf)) {
    $runtime = (Get-Command java -CommandType Application -ErrorAction Stop | Select-Object -First 1).Source
}

$pocArguments = @()
if ($AvailabilityCsv) {
    $pocArguments += $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($AvailabilityCsv)
}

$sources = (Get-ChildItem -LiteralPath (Join-Path $PSScriptRoot 'src') -Filter '*.java' -File -Recurse).FullName
$tempRoot = (Get-Item -LiteralPath ([System.IO.Path]::GetTempPath())).FullName.TrimEnd('\', '/')
$classes = Join-Path $tempRoot ('soen342-poc-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $classes | Out-Null

Push-Location -LiteralPath $PSScriptRoot
try {
    & $compiler.Source --release 17 -encoding UTF-8 -d $classes @sources
    $code = $LASTEXITCODE
    if ($code -eq 0) {
        & $runtime -cp $classes soen342.Main @pocArguments
        $code = $LASTEXITCODE
    }
} finally {
    Pop-Location
    $resolved = (Resolve-Path -LiteralPath $classes -ErrorAction SilentlyContinue).ProviderPath
    if ($resolved -eq $classes -and (Split-Path -Parent $resolved) -eq $tempRoot) {
        Remove-Item -LiteralPath $resolved -Recurse -Force
    }
}

exit $code
