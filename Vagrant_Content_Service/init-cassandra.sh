#!/bin/bash
#
# Fire up a cassandra cluster for testing with. It runs inside Docker, but is
# reachable at localhost.
#

if !hash vagrant 2>/dev/null; then
  echo "ERROR: VAGRANT is misconfigured"
else
  vagrant halt --force
  vagrant up --provision
fi
