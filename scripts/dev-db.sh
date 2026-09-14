#!/usr/bin/env bash
# ==========================================================
# Dung mot instance PostgreSQL tam cho moi truong phat trien, chay o cong 55432
# va luu du lieu trong .tmp-pg/ nen khong anh huong den PostgreSQL dang co tren may.
#
# Chi dung khi KHONG chay bang Docker. Neu da co Docker thi dung:
#   docker compose up -d postgres redis minio
#
# Cach dung:
#   bash scripts/dev-db.sh start     # khoi tao (neu chua co) va bat
#   bash scripts/dev-db.sh stop      # tat
#   bash scripts/dev-db.sh reset     # xoa sach va tao lai tu dau
#   bash scripts/dev-db.sh status    # xem trang thai
# ==========================================================
set -uo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DATA_DIR="$PROJECT_DIR/.tmp-pg"
PORT=55432
DB_NAME=delivery_db
DB_USER=delivery_user
DB_PASSWORD=delivery_pass_2026

# Tim bo cong cu PostgreSQL tren may
PG_BIN=""
for candidate in /opt/homebrew/opt/postgresql@16/bin /usr/local/opt/postgresql@16/bin /opt/homebrew/bin /usr/local/bin; do
  if [ -x "$candidate/pg_ctl" ]; then
    PG_BIN="$candidate"
    break
  fi
done
if [ -z "$PG_BIN" ]; then
  echo "Khong tim thay pg_ctl. Cai PostgreSQL truoc: brew install postgresql@16"
  exit 1
fi

init_cluster() {
  echo "Khoi tao cluster moi tai $DATA_DIR ..."
  "$PG_BIN/initdb" -D "$DATA_DIR" -U postgres --auth=trust -E UTF8 > /dev/null
}

start_cluster() {
  [ -d "$DATA_DIR" ] || init_cluster
  "$PG_BIN/pg_ctl" -D "$DATA_DIR" -o "-p $PORT -k /tmp" -l "$DATA_DIR/server.log" start
  sleep 2
  # Tao role va database neu chua co
  "$PG_BIN/psql" -h 127.0.0.1 -p $PORT -U postgres -d postgres -tc \
    "SELECT 1 FROM pg_roles WHERE rolname='$DB_USER'" | grep -q 1 || \
    "$PG_BIN/psql" -h 127.0.0.1 -p $PORT -U postgres -d postgres -c \
      "CREATE ROLE $DB_USER LOGIN PASSWORD '$DB_PASSWORD' SUPERUSER;" > /dev/null
  "$PG_BIN/psql" -h 127.0.0.1 -p $PORT -U postgres -d postgres -tc \
    "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" | grep -q 1 || \
    "$PG_BIN/psql" -h 127.0.0.1 -p $PORT -U postgres -d postgres -c \
      "CREATE DATABASE $DB_NAME OWNER $DB_USER;" > /dev/null

  cat <<EOF

PostgreSQL da san sang tai cong $PORT.
Chay backend bang lenh:

  cd backend && \\
  DB_URL="jdbc:postgresql://127.0.0.1:$PORT/$DB_NAME" \\
  DB_USERNAME=$DB_USER DB_PASSWORD=$DB_PASSWORD \\
  SPRING_CACHE_TYPE=none mvn spring-boot:run

EOF
}

case "${1:-start}" in
  start)
    start_cluster
    ;;
  stop)
    if [ ! -f "$DATA_DIR/postmaster.pid" ]; then
      echo "Postgres tam (.tmp-pg, cong $PORT) khong dang chay. Khong can tat."
      exit 0
    fi
    "$PG_BIN/pg_ctl" -D "$DATA_DIR" stop
    ;;
  reset)
    "$PG_BIN/pg_ctl" -D "$DATA_DIR" stop 2>/dev/null || true
    rm -rf "$DATA_DIR"
    start_cluster
    ;;
  status)
    "$PG_BIN/pg_ctl" -D "$DATA_DIR" status
    ;;
  *)
    echo "Cach dung: bash scripts/dev-db.sh [start|stop|reset|status]"
    exit 1
    ;;
esac
