#!/bin/bash

exec >> /root/output/output.txt 2>&1
echo "********************************************************************************************************************"
HOST="10.200.166.252"
USER="ftp-user"
PASSWORD="pacsbftp"
SOURCE="input"

File1=m_promotion.csv
File2=m_reward.csv
File3=m_condition.csv

lftp -u $USER,$PASSWORD $HOST <<-EOF
set ftp:passive-mode off
echo $(date -u) "FTP connection established"
cd $SOURCE

get $File1
rm -f $File1
get $File2
rm -f $File2
get $File3
rm -f $File3

#ConsolidatedCsv.sh
javac ConsolidatedCsv.java
echo "Compiled Successfully"
java ConsolidatedCsv
echo "Running Successfully"

#Script to transfer the files from EC2 to S3
File1=m_consolidated.csv
#File2=m_condition.csv
today=$(date +"%Y%m%d")
echo $(date -u) "Starting to copy the files from EC2 to S3"
cd /root

if test -f "$File1";then
echo "m_consolidated.csv file exists"
cp $File1 /root/backup/m_consolidated_${today}.csv
echo "Successfully copied the m_consolidated.csv file to /root/backup directory"
aws s3 cp /root/m_consolidated.csv s3://cz-s3-pac-snb/pac-batch/update/
echo "m_consolidated.csv file got successfully copied from EC2 to S3"
rm -f $File1
else
echo "m_consolidated.csv file does not exists"
fi

#if test -f "$File2";then
#echo "m_condition.csv file exists"
#cp $File2 /root/backup/m_reward_${today}.csv
#echo "Successfully copied the m_condition.csv file to /root/backup directory"
#aws s3 cp /root/m_condition.csv s3://cz-s3-pac-snb/pac-batch/update/
#echo "m_condition.csv file got successfully copied from EC2 to S3"
#rm -f $File2
#else
#echo "m_condition.csv file does not exists"
#fi
