@echo off
chcp 65001 >nul
cd /d "%~dp0"
if not exist libs mkdir libs
echo 正在下载 PDF.js 最小本地版...
powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://cdn.jsdelivr.net/npm/pdfjs-dist@5.7.284/build/pdf.min.mjs' -OutFile '.\libs\pdf.min.mjs'; Invoke-WebRequest -Uri 'https://cdn.jsdelivr.net/npm/pdfjs-dist@5.7.284/build/pdf.worker.min.mjs' -OutFile '.\libs\pdf.worker.min.mjs'"
echo.
echo 下载完成。重新打开 index.html 后，PDF 导入会优先使用本地 PDF.js。
pause
