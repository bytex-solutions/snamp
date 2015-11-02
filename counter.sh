#!/usr/bin/env bash
everything=$(find | grep -v /docs)

for OUTPUT in $(git log --format='%aN' | sort -u)
do
echo "Author $OUTPUT has made:" $(git log --author="$OUTPUT" --pretty=tformat: --numstat | grep -v "docs/" | \
gawk '{  add += $1 ; subs += $2 ; loc += $1 - $2} END \
{ printf "added lines: %s removed lines: %s total lines: %s\n",add,subs,loc}' -)
echo "It's" 
done
