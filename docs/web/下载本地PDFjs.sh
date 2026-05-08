#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p libs
echo "正在下载 PDF.js 最小本地版..."
curl -L 'https://cdn.jsdelivr.net/npm/pdfjs-dist@5.7.284/build/pdf.min.mjs' -o './libs/pdf.min.mjs'
curl -L 'https://cdn.jsdelivr.net/npm/pdfjs-dist@5.7.284/build/pdf.worker.min.mjs' -o './libs/pdf.worker.min.mjs'
echo "下载完成。重新打开 index.html 后，PDF 导入会优先使用本地 PDF.js。"
