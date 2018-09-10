#!/bin/bash
#
#############################################
# author:WCH
# describes:Delete elasticsearch history index.
# version:v1.0
# updated:20180907
#############################################
#
# Configuration information
logfile=/del_index.log
tmpfile=/index.txt
host=localhost
port=9200
deldate=`date -d '-3days' +'%Y.%m.%d'`
# Check executed user
if [ `whoami` != "root" ];then
echo "Please use root run script!!!"
exit 1
fi
# Delete elasticsearch index
curl -s "$host:$port/_cat/indices?v"|grep -v health|awk {'print $3'}|grep "$deldate" > $tmpfile
if [ ! -s $tmpfile ];then
echo "[`date +'%F %T'`] [WARNING] $tmpfile is a empty file."|tee -a $logfile
exit 1
fi
for i in `cat /index.txt`
do
curl -XDELETE http://$host:$port/$i
if [ $? -eq 0 ];then
echo "[`date +'%F %T'`] [  INFO  ] Elasticsearch index $i delete successful."|tee -a $logfile
else
echo "[`date +'%F %T'`] [CRITICAL] Elasticsearch index $i delete failed!!!"|tee -a $logfile
/usr/bin/cagent_tools alarm "Elasticsearch index $i delete failed!!!"
exit 6
fi
done