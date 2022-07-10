#!/usr/bin/env sh

source ./source.sh
deno run --inspect-brk --allow-net --allow-env ./polling.ts