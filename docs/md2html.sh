#!/usr/bin/env bash

rm -r html
mkdir html
cp -avr ./images ./html/images
for file in $(ls *.md); do pandoc -f markdown_github -t html --html-q-tags "${file}" > "./html/${file}.html"; done;
