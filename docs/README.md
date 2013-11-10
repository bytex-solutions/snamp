To compile SNAMP online documentation:
* Install NodeJS and DocPad
* Moves to docs/ directory
* Run `docpad deploy-ghpages --env static` to compile the online documentation
* Copy files from /out directory into gh-pages branch
* Run `docpad run` and go to `http://localhost:9778/` to see the result locally
* Commit fresh gh-pages to the GitHub pages.
