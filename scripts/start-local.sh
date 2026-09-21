#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

for command in docker mvn node npm; do
  if ! command -v "$command" >/dev/null 2>&1; then
    printf 'Missing required command: %s\n' "$command" >&2
    exit 1
  fi
done

docker compose up -d

if [ ! -d frontend/node_modules ]; then
  npm --prefix frontend ci
fi

cleanup() {
  if [ -n "${BACKEND_PID:-}" ]; then
    kill "$BACKEND_PID" 2>/dev/null || true
  fi
  if [ -n "${FRONTEND_PID:-}" ]; then
    kill "$FRONTEND_PID" 2>/dev/null || true
  fi
}

trap cleanup EXIT INT TERM

mvn spring-boot:run -Dspring-boot.run.profiles=dev &
BACKEND_PID=$!

npm --prefix frontend start &
FRONTEND_PID=$!

printf 'Backend: http://localhost:8080\n'
printf 'Frontend: http://localhost:4200\n'
printf 'Press Ctrl+C to stop the application processes. PostgreSQL remains running.\n'

while kill -0 "$BACKEND_PID" 2>/dev/null && kill -0 "$FRONTEND_PID" 2>/dev/null; do
  sleep 1
done
