#!/bin/bash

SENDER=IDUGS:S-IM:CDMFEAS
RECEIVER=IDUGS:S-RPF:FE
BASKET=/opt/TEST/basket
MSGCLASS=FeasibilityAnalysis
MODE=twoway

if [ "$#" -ne 2 ]; then
    echo "Usage: sendFeasybilityWithNumber.sh <attachment_path> number of concurrent thread"
else
    java -jar cmSimSend.jar $SENDER $RECEIVER $BASKET $1 $MSGCLASS $MODE $2 3000
fi
