#!/bin/bash

# MySQL 터널링
echo "[ MySQL 터널 연결 중... ]"
ssh -L 3307:172.19.0.3:3306 root@223.130.134.121
