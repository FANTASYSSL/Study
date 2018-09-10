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
function delete_indices() {
    comp_date=`date -d "05 day ago" +"%Y-%m-%d"`
    date1="$1 00:00:00"
    date2="$comp_date 00:00:00"

    t1=`date -d "$date1" +%s`
    t2=`date -d "$date2" +%s`

    if [ $t1 -le $t2 ]; then
        echo "$1时间早于$comp_date，进行索引删除"
        #转换一下格式，将类似2018-05-01格式转化为2018.05.01
        format_date=`echo $1| sed 's/-/\./g'`
        curl -XDELETE http://localhost:9200/*$format_date
    fi
}

curl -XGET http://localhost:9200/_cat/indices | awk -F" " '{print $3}' | awk -F"-" '{print $NF}' | egrep "[0-9]*\.[0-9]*\.[0-9]*" | sort | uniq  | sed 's/\./-/g' | while read LINE
do
    #调用索引删除函数
    delete_indices $LINE
done
