#!/bin/bash

# MySQL 터널링
echo "[ MySQL 터널 연결 중... ]"
ssh -L 3307:172.20.0.2:3306 root@223.130.134.121