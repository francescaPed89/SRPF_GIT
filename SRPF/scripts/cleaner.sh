#!/bin/bash

#to be inseted on contrab on hosts running the Frontend

#PATHS holding elaborated files
FEAS="/opt/SRPF/working_dir/FEAS"
REFINE="/opt/SRPF/working_dir/REFINE"
ALLPLAN="/opt/SRPF/working_dir/ALLPLAN"
ODMTP="/opt/SRPF/working_dir/ODMTP"
ODNOM="/opt/SRPF/working_dir/ODNOM"
ODREF="/opt/SRPF/working_dir/ODREF"
ODSTP="/opt/SRPF/working_dir/ODSTP"
PAW="/opt/SRPF/working_dir/PAW"
SOE="/opt/SRPF/working_dir/SOE"

#CM BASKET
CMBASKET="/opt/SRPF/FrontEnd/basket"

#OLD LOG
OLD_LOG="/opt/SRPF/oldlogs"

find $FEAS/* -ctime +10 | xargs rm -rf
find $REFINE/* -ctime +10 | xargs rm -rf
find $ALLPLAN/* -ctime +10 | xargs rm -rf
find $ODMTP/* -ctime +10 | xargs rm -rf
find $ODNOM/* -ctime +10 | xargs rm -rf
#find $ODREF/* -ctime +180 | xargs rm -rf
find $ODSTP/* -ctime +10 | xargs rm -rf
find $PAW/* -ctime +10 | xargs rm -rf
find $SOE/* -ctime +10 | xargs rm -rf
find $OLD_LOG/* -ctime +10 | xargs rm -rf

#the CM BASKET is emptied by Frontend
find $CMBASKET/* -ctime +10 | xargs rm -rf



