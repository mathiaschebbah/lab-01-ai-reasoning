#!/usr/bin/env bash
# Builds the submission ZIP for Lab 1.
# It contains the PDF report, the full source code, the presentation, the build
# files and the project statement, but not target/, .git/ or LaTeX build files.
set -euo pipefail

cd "$(dirname "$0")"
NAME="RubiksCube_Astar_CHEBBAH_BETRAOUI_SALL_BARBIER"
OUT="${NAME}.zip"

# Make sure the report and presentation PDFs are up to date.
( cd report && pdflatex -interaction=nonstopmode report.tex >/dev/null && pdflatex -interaction=nonstopmode report.tex >/dev/null )
( cd presentation && pdflatex -interaction=nonstopmode presentation.tex >/dev/null && pdflatex -interaction=nonstopmode presentation.tex >/dev/null )

rm -f "$OUT"

zip -r "$OUT" \
    pom.xml \
    README.md \
    src \
    report/report.tex report/report.pdf report/figures report/*.sty \
    presentation/presentation.tex presentation/presentation.pdf \
    "Lab 1 - Solveur Rubik's Cube (Astar).pdf" \
    -x '*.DS_Store' \
    >/dev/null

echo "Created $OUT"
unzip -l "$OUT" | tail -n +2 | head -n 60
