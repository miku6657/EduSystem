#!/usr/bin/env bash
# ============================================================
# 一键同步数据库（建表 + 演示数据）—— macOS / Linux 版本
#
# 用法：cd edumanage/springboot && ./sync-db.sh
#
# 说明：
#   - schema.sql 可重复执行（每张表前会先 DROP TABLE IF EXISTS）；
#   - data.sql 全部使用 INSERT IGNORE，并把 seed 账号密码统一更新为 BCrypt；
#   - 每次 git pull 之后跑一次即可，避免「表不存在 / 字段不存在 / 登录失败」；
#   - 显式指定 --default-character-set=utf8mb4，保证中文不被客户端字符集破坏。
#
# 账号密码默认与 application.yml 一致，可用环境变量覆盖：
#   MYSQL_USER=root MYSQL_PWD=你的密码 MYSQL_HOST=127.0.0.1 MYSQL_PORT=3306 ./sync-db.sh
# ============================================================
set -euo pipefail
cd "$(dirname "$0")"

MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PWD="${MYSQL_PWD:-123456}"
MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
export MYSQL_PWD

if ! command -v mysql >/dev/null 2>&1; then
  echo "[错误] 找不到 mysql 命令，请把 MySQL 的 bin 目录加入 PATH"
  exit 1
fi

MYSQL_ARGS=(--default-character-set=utf8mb4 --user="$MYSQL_USER" --host="$MYSQL_HOST" --port="$MYSQL_PORT")

echo "[1/2] 建表：schema.sql（会先删除同名表）"
mysql "${MYSQL_ARGS[@]}" < schema.sql

echo "[2/2] 演示数据：data.sql"
mysql "${MYSQL_ARGS[@]}" eduSYSTEM < data.sql

echo
echo "完成：数据库 eduSYSTEM 已同步。"
echo "登录账号（密码均为 123456）：admin（管理员）、T001（教师）、2023005001（学生）"
