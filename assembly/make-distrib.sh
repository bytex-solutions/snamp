#!/usr/bin/env bash

cd ./target/assembly
tar -cvzf ../snamp-2.0.0.tar.gz *
zip -9 -r ../snamp-2.0.0.zip *
