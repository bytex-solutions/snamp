#!/usr/bin/env bash

cd ./target/assembly
tar -cvzf ../snamp-1.2.0.tar.gz *
zip -9 -r ../snamp-1.2.0.zip *
