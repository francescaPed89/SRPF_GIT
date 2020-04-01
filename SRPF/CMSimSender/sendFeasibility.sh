#!/bin/bash

SENDER=IDUGS:S-IM:CDMFEAS
RECEIVER=IDUGS:S-RPF:FE
BASKET=/opt/TEST/basket
MSGCLASS=FeasibilityAnalysis
MODE=twoway

if [ "$#" -ne 1 ]; then
    echo "Usage: sendFeasybility.sh <attachment_path> "
else
    java -jar cmSimSend.jar $SENDER $RECEIVER $BASKET $1 $MSGCLASS $MODE 1 250
fi
