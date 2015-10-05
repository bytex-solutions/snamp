#!/usr/bin/env bash

rm -r html
mkdir html
cp -avr ./images ./html/images
pandoc overview.md --latex-engine=xelatex -o result.pdf
