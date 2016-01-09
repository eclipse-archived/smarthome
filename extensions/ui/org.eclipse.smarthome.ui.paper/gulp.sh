#!/bin/bash

# This script calls gulp using the node and gulp versions installed
# by the maven frontend plugin instead of using the globally installed ones

node/node node_modules/gulp/bin/gulp.js "$@"