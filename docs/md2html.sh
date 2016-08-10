#!/usr/bin/env bash

rm -r html
mkdir html
mkdir html/adapters
mkdir html/images
mkdir html/connectors
mkdir html/webconsole
mkdir html/css
mkdir html/examples

cp -avr ./images/*.png ./html/images
cp -avr ./adapters/*.png ./html/adapters
cp -avr ./connectors/*.png ./html/connectors
cp -avr ./webconsole/*.png ./html/webconsole
cp -avr ./examples/*.png ./html/examples
cp -avr ./*.css ./html/css
for file in $(ls *.md); do pandoc -f markdown_github -t html -c css/pandoc.css --html-q-tags "${file}" > "./html/${file}.html"; done;
cd adapters
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/gateway/${file}.html"; done;
cd ..
cd connectors
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/connector/${file}.html"; done;
cd ..
cd webconsole
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/webconsole/${file}.html"; done;
cd ..
cd examples
for file in $(ls *.md); do pandoc -f markdown_github -t html -c ../css/pandoc.css --html-q-tags "${file}" > "../html/examples/${file}.html"; done;
cd ..
cd html
find . -type f  -name '*.html' -print0 | xargs -0 sed -i 's/md/md.html/g'
cd ..
