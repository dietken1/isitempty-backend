#!/bin/bash

# Redis 터널링
echo "[ Redis 터널 연결 중... ]"
ssh -L 6379:172.20.0.4:6379 root@223.130.134.121