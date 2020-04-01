#!/bin/bash

if [ "$#" -ne 3 ]; then
    echo "Usage: cmSimSend.sh <attachment_path> <msg_class> oneway|twoway"
else
    java -jar cmSimSend.jar IDUGS:S-IM:CDMFEAS IDUGS:S-RPF:FE /opt/TEST/attachment $1 $2 $3 250
fi
