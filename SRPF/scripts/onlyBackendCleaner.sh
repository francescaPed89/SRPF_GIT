#!/bin/bash

#to be inseted on contrab on hosts running the only instance of the  Backend
OLD_LOG="/opt/SRPF/oldlogs"

find $OLD_LOG/* -ctime +10 | xargs rm -rf
