##>>>>>>>>>>>>>>>>>>>>>>>file input>>>>>>>>>>>>>>>>>>>>>
#!/bin/bash

# 临时文件路径，保存jps信息
TMP_FILE_PATH=/tmp/superJps.tmp
jps -ml > $TMP_FILE_PATH

# java进程占用的内存总百分比
SUM_PMEM=0

# 展示第一行表头
echo -e "PID\tPORT\tPMEM\tJAR"

# 逐行读入jps内容
while read LINE
do
        # 首先转换为数组，第一列是PID，第二列是详细描述
        JPS_ARRAY=($LINE)
        # 占用端口
        PORT=`netstat -nlp | awk '{if($6=="LISTEN" && $7=="'"${JPS_ARRAY[0]}/java"'")print $4}' | awk -F ":" '{print $4}'`
        # 占用内存
        PMEM=`ps -e -o 'pid,pmem' | sed s/'^\s*'/''/ | egrep "^${JPS_ARRAY[0]}" | awk '{print $2}'`
        # 输出
        echo -e "${JPS_ARRAY[0]}\t$PORT\t$PMEM%\t${JPS_ARRAY[1]}"
        # 内存百分比求和
        if  [ ! -n "$PMEM" ] ;then
                SUM_PMEM=`echo $SUM_PMEM+0|bc`
        else
                SUM_PMEM=`echo $SUM_PMEM+$PMEM|bc`
        fi
done < $TMP_FILE_PATH

# 展示总的内存占用百分比
echo "PMEM_TOTAL: $SUM_PMEM%"
##>>>>>>>>>>>>>>>>>>>>>>>file input>>>>>>>>>>>>>>>>>>>>>
