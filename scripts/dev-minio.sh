#!/usr/bin/env bash
# ==========================================================
# Chay MinIO local (khong can Docker) de upload anh giao hang.
# Du lieu nam trong .tmp-minio/ (da gitignore).
#
# Cach dung:
#   bash scripts/dev-minio.sh start
#   bash scripts/dev-minio.sh stop
#   bash scripts/dev-minio.sh status
# ==========================================================
set -uo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DATA_DIR="$PROJECT_DIR/.tmp-minio"
PID_FILE="$DATA_DIR/minio.pid"
LOG_FILE="$DATA_DIR/minio.log"
PORT=9000
CONSOLE_PORT=9001
ACCESS_KEY=minioadmin
SECRET_KEY=minioadmin123
BUCKET=delivery-files

MINIO_BIN="$(command -v minio || true)"
MC_BIN="$(command -v mc || true)"

if [ -z "$MINIO_BIN" ]; then
  echo "Khong tim thay minio. Cai bang: brew install minio minio-mc"
  exit 1
fi

is_running() {
  if [ -f "$PID_FILE" ]; then
    local pid
    pid="$(cat "$PID_FILE")"
    if kill -0 "$pid" 2>/dev/null; then
      return 0
    fi
  fi
  lsof -nP -iTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1
}

start_minio() {
  mkdir -p "$DATA_DIR"
  if is_running; then
    echo "MinIO da chay tai http://127.0.0.1:$PORT"
  else
    MINIO_ROOT_USER="$ACCESS_KEY" MINIO_ROOT_PASSWORD="$SECRET_KEY" \
      "$MINIO_BIN" server "$DATA_DIR/data" \
      --address ":$PORT" \
      --console-address ":$CONSOLE_PORT" \
      >"$LOG_FILE" 2>&1 &
    echo $! >"$PID_FILE"
    for _ in 1 2 3 4 5 6 7 8 9 10; do
      if curl -sf -o /dev/null "http://127.0.0.1:$PORT/minio/health/live"; then
        break
      fi
      sleep 0.5
    done
    echo "MinIO da san sang:"
  fi

  if [ -n "$MC_BIN" ]; then
    "$MC_BIN" alias set delivery-local "http://127.0.0.1:$PORT" "$ACCESS_KEY" "$SECRET_KEY" >/dev/null
    "$MC_BIN" mb --ignore-existing "delivery-local/$BUCKET" >/dev/null
    "$MC_BIN" anonymous set download "delivery-local/$BUCKET" >/dev/null
  fi

  cat <<EOF
  API:     http://127.0.0.1:$PORT
  Console: http://127.0.0.1:$CONSOLE_PORT  ($ACCESS_KEY / $SECRET_KEY)
  Bucket:  $BUCKET

Neu backend dang chay truoc khi bat MinIO, khoi dong lai backend de no tao bucket
(neu script nay chua tao duoc). Upload anh khong can Docker.
EOF
}

stop_minio() {
  if [ -f "$PID_FILE" ]; then
    kill "$(cat "$PID_FILE")" 2>/dev/null || true
    rm -f "$PID_FILE"
  fi
  local pid
  pid="$(lsof -nP -t -iTCP:"$PORT" -sTCP:LISTEN 2>/dev/null || true)"
  if [ -n "$pid" ]; then
    kill "$pid" 2>/dev/null || true
  fi
  echo "Da tat MinIO"
}

case "${1:-start}" in
  start)
    start_minio
    ;;
  stop)
    stop_minio
    ;;
  status)
    if is_running; then
      echo "MinIO dang chay tai http://127.0.0.1:$PORT"
    else
      echo "MinIO chua chay. Bat bang: bash scripts/dev-minio.sh start"
    fi
    ;;
  *)
    echo "Cach dung: bash scripts/dev-minio.sh [start|stop|status]"
    exit 1
    ;;
esac
