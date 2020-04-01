#!/bin/bash

SENDER=IDUGS:S-IM:CDMFEAS
RECEIVER=IDUGS:S-RPF:FE
BASKET=/opt/TEST/basket
MODE=oneway

if [ "$#" -ne 2 ]; then
    echo "Usage: sendOBData.sh <attachment_path> <msg class>"
else
    java -jar cmSimSend.jar $SENDER $RECEIVER $BASKET $1 $2 $MODE 1 250
fi
