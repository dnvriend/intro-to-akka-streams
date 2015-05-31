#!/bin/bash
docker-compose up -d
sbt test
docker-compose stop
docker-compose rm -f
