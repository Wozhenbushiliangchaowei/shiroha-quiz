<#
.SYNOPSIS
  PDF.js 5.7.284 完整离线包下载脚本
.DESCRIPTION
  从 npm 官方包下载 pdfjs-dist，并解包复制 Web 端 PDF 导入需要的完整运行资源：
  核心库、Worker、CMaps、标准字体、ICC 色彩配置、WASM 图片解码资源和沙箱文件。

  注意：这些资源增强的是文字层 PDF 和复杂编码 PDF 的解析稳定性，不等于 OCR。
#>

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$version = "5.7.284"
$packageUrl = "https://registry.npmjs.org/pdfjs-dist/-/pdfjs-dist-$version.tgz"
$tempDir = Join-Path $scriptDir ".pdfjs-dist-$version"
$tarPath = Join-Path $tempDir "pdfjs-dist-$version.tgz"
$packageDir = Join-Path $tempDir "package"

function Copy-PdfJsPath {
    param(
        [Parameter(Mandatory = $true)][string]$RelativePath,
        [Parameter(Mandatory = $true)][string]$DestinationName
    )

    $source = Join-Path $packageDir $RelativePath
    $destination = Join-Path $scriptDir $DestinationName

    if (!(Test-Path $source)) {
        throw "PDF.js 包中缺少资源：$RelativePath"
    }

    if (Test-Path $destination) {
        Remove-Item -Path $destination -Recurse -Force
    }

    Copy-Item -Path $source -Destination $destination -Recurse -Force
}

Write-Host "PDF.js $version 完整离线包下载" -ForegroundColor Cyan
Write-Host "目标位置：$scriptDir" -ForegroundColor Gray

if (Test-Path $tempDir) {
    Remove-Item -Path $tempDir -Recurse -Force
}
New-Item -ItemType Directory -Path $tempDir -Force | Out-Null

Write-Host "`n[下载 npm 包]" -ForegroundColor Yellow
Invoke-WebRequest -Uri $packageUrl -OutFile $tarPath -UseBasicParsing -ErrorAction Stop

Write-Host "[解包]" -ForegroundColor Yellow
tar -xf $tarPath -C $tempDir

Write-Host "[复制核心文件]" -ForegroundColor Yellow
Copy-PdfJsPath -RelativePath "build/pdf.min.mjs" -DestinationName "pdf.min.mjs"
Copy-PdfJsPath -RelativePath "build/pdf.worker.min.mjs" -DestinationName "pdf.worker.min.mjs"
Copy-PdfJsPath -RelativePath "build/pdf.sandbox.min.mjs" -DestinationName "pdf.sandbox.min.mjs"

Write-Host "[复制增强资源]" -ForegroundColor Yellow
Copy-PdfJsPath -RelativePath "cmaps" -DestinationName "cmaps"
Copy-PdfJsPath -RelativePath "standard_fonts" -DestinationName "standard_fonts"
Copy-PdfJsPath -RelativePath "iccs" -DestinationName "iccs"
Copy-PdfJsPath -RelativePath "wasm" -DestinationName "wasm"
Copy-PdfJsPath -RelativePath "image_decoders" -DestinationName "image_decoders"

Remove-Item -Path $tempDir -Recurse -Force

$cmapCount = (Get-ChildItem (Join-Path $scriptDir "cmaps") -File | Measure-Object).Count
$fontCount = (Get-ChildItem (Join-Path $scriptDir "standard_fonts") -File | Measure-Object).Count
$wasmCount = (Get-ChildItem (Join-Path $scriptDir "wasm") -File | Measure-Object).Count

Write-Host ""
Write-Host "完成：PDF.js $version 完整离线资源已更新。" -ForegroundColor Green
Write-Host "CMaps: $cmapCount 个文件；标准字体: $fontCount 个文件；WASM: $wasmCount 个文件。" -ForegroundColor Gray
Write-Host "Web 端仍只支持文字层 PDF；扫描件/图片型 PDF 需要 OCR，不属于 PDF.js 完整包能力。" -ForegroundColor Gray

