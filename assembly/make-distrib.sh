#!/usr/bin/env bash

cd ./target/assembly
tar -cvzf ../snamp-1.1.0.tar.gz *
zip -9 -r ../snamp-1.1.0.zip *
