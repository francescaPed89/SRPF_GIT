#!/bin/bash

SENDER=IDUGS:S-IM:CDMFEAS
RECEIVER=IDUGS:S-RPF:FE
BASKET=/opt/TEST/basket
MSGCLASS=PAW
MODE=oneway

if [ "$#" -ne 1 ]; then
    echo "Usage: sendPaw.sh <attachment_path> "
else
    java -jar cmSimSend.jar $SENDER $RECEIVER $BASKET $1 $MSGCLASS $MODE 1 250
fi

