#!/usr/bin/env bash

rm -r html
mkdir html
mkdir html/gateways
mkdir html/images
mkdir html/connectors
mkdir html/webconsole
mkdir html/css
mkdir html/supervisors
mkdir html/instrumentation

cp -avr ./images/*.png ./html/images
cp -avr ./gateways/*.png ./html/gateways
cp -avr ./connectors/*.png ./html/connectors
cp -avr ./webconsole/*.png ./html/webconsole
cp -avr ./*.css ./html/css
cp -avr ./supervisors/*.png ./html/supervisors
cp -avr ./instrumentation/*.png ./html/instrumentation
for file in $(ls *.md); do pandoc -f markdown_github -t html -c css/pandoc.css --html-q-tags "${file}" > "./html/${file}.html"; done;
cd gateways
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/gateways/${file}.html"; done;
cd ..
cd connectors
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/connectors/${file}.html"; done;
cd ..
cd webconsole
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/webconsole/${file}.html"; done;
cd ..
cd supervisors
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/supervisors/${file}.html"; done;
cd ..
cd instrumentation
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/instrumentation/${file}.html"; done;
cd ..
cd html
find . -type f  -name '*.html' -print0 | xargs -0 sed -i 's/md/md.html/g'
cd ..
