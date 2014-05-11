#!/bin/bash
protoc --java_out=../src/main/java/panda/db/nosql/rethinkdb/proto/ ql2.proto