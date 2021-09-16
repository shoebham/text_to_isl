#! /bin/csh

set HERE = `/usr/bin/dirname $0`

# Call node script
node $HERE/htog.js $*

exit
