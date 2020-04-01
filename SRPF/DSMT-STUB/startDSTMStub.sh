#!/bin/bash

if [ "$#" -ne 2 ]; then
    echo "Usage: startDSTMStub.sh CM_ADDRESS EVENT_REPORT_CM_MSG_CLASS"
else
    java -jar DSMT-STUB.jar -a $1 -m $2
fi
