#!/usr/bin/env bash

thrift -gen java -v -out ./src/test/java/ ./src/test/resources/thrift-defs/mda.thrift