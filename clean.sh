#!/bin/bash
read -p "此操作将删除 MySQL 和 Redis 的所有数据，确认继续？[y/N] " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
  echo "已取消"
  exit 0
fi

docker compose -f script/docker/docker-compose.yml down -v mysql redis
docker volume rm yudao-system_mysql yudao-system_redis 2>/dev/null || true
