# Builds or runs Femtocraft against a chosen ItszuLib branch, tag or commit, without touching your ItszuLib checkout.
# Windows twin of tools/itszulib-ref.sh; same behaviour and environment variables.
#
#   tools\itszulib-ref.ps1 <ref> [gradle args...]    e.g. tools\itszulib-ref.ps1 main runClient
#   tools\itszulib-ref.ps1 --list
#   tools\itszulib-ref.ps1 --clean
#
# Each ref gets a detached git worktree of your ItszuLib checkout under .itszulib\<ref> (git-ignored), updated from
# ItszuLib's remote on every run, and Gradle runs with -Pitszulib_dir pointed at it. <ref> is looked up as a remote
# branch, then a tag, then any local ref or commit.
#
# Environment: ITSZULIB_DIR (default ..\ItszuLib), ITSZULIB_REMOTE (default origin), ITSZULIB_NO_FETCH=1 (skip fetch)
$ErrorActionPreference = 'Stop'

$root = Split-Path -Parent $PSScriptRoot
$sourceDir = if ($env:ITSZULIB_DIR) { $env:ITSZULIB_DIR } else { Join-Path $root '..\ItszuLib' }
$remote = if ($env:ITSZULIB_REMOTE) { $env:ITSZULIB_REMOTE } else { 'origin' }
$worktrees = Join-Path $root '.itszulib'

function Die($message) { Write-Error "itszulib-ref: $message"; exit 1 }

if ($args.Count -lt 1) { Die 'usage: tools\itszulib-ref.ps1 <branch|tag|commit> [gradle args...] | --list | --clean' }
git -C $sourceDir rev-parse --git-dir *> $null
if ($LASTEXITCODE -ne 0) { Die "no ItszuLib git checkout at $sourceDir (set ITSZULIB_DIR)" }

switch ($args[0]) {
    '--list' {
        git -C $sourceDir worktree list | Select-String -SimpleMatch $worktrees
        exit 0
    }
    '--clean' {
        if (Test-Path $worktrees) {
            Get-ChildItem -Directory $worktrees | ForEach-Object {
                git -C $sourceDir worktree remove --force $_.FullName
                Write-Host "removed $($_.FullName)"
            }
        }
        git -C $sourceDir worktree prune
        exit 0
    }
}

$ref = $args[0]
$gradleArgs = @($args | Select-Object -Skip 1)
$dir = Join-Path $worktrees ($ref -replace '[\\/]', '_')

if ($env:ITSZULIB_NO_FETCH -ne '1') {
    Write-Host "itszulib-ref: fetching $remote"
    git -C $sourceDir fetch --quiet --tags $remote
    if ($LASTEXITCODE -ne 0) { Die "fetch from $remote failed (ITSZULIB_NO_FETCH=1 to skip)" }
}

$commit = $null
foreach ($candidate in @("refs/remotes/$remote/$ref", "refs/tags/$ref", $ref)) {
    $found = git -C $sourceDir rev-parse --verify --quiet "$candidate^{commit}" 2> $null
    if ($LASTEXITCODE -eq 0 -and $found) { $commit = $found; break }
}
if (-not $commit) { Die "no branch, tag or commit '$ref' in $sourceDir (remote $remote)" }

if (Test-Path $dir) {
    if (git -C $dir status --porcelain) {
        Write-Host "itszulib-ref: $dir has local changes; building it as it is"
    } else {
        git -C $dir checkout --quiet --detach $commit
    }
} else {
    New-Item -ItemType Directory -Force $worktrees | Out-Null
    git -C $sourceDir worktree add --quiet --detach $dir $commit
}

Write-Host "itszulib-ref: ItszuLib $ref at $(git -C $dir log -1 --format='%h %s')"
Set-Location $root
& .\gradlew.bat "-Pitszulib_dir=$dir" @gradleArgs
exit $LASTEXITCODE
