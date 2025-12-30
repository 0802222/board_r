#!/bin/bash
# /home/ubuntu/scripts/switch-nginx.sh

set -e  # 에러 발생 시 즉시 종료

NEW_PORT=$1

if [ -z "$NEW_PORT" ]; then
  echo "Usage: $0 <port>"
  exit 1
fi

# ✅ 백업 생성
BACKUP_FILE="/etc/nginx/sites-available/board.conf.backup.$(date +%Y%m%d_%H%M%S)"
cp /etc/nginx/sites-available/board.conf "$BACKUP_FILE"
echo "Backup created: $BACKUP_FILE"

# ✅ Nginx 설정 업데이트
cat > /etc/nginx/sites-available/board.conf << EOF
upstream board_backend {
    server localhost:${NEW_PORT} max_fails=3 fail_timeout=30s;
}

server {
    listen 80;
    server_name _;

    # ✅ 개선: 타임아웃 설정
    proxy_connect_timeout 60s;
    proxy_send_timeout 60s;
    proxy_read_timeout 60s;

    # ✅ 개선: 버퍼 설정
    proxy_buffer_size 128k;
    proxy_buffers 4 256k;
    proxy_busy_buffers_size 256k;

    location / {
        proxy_pass http://board_backend;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;

        # ✅ 개선: WebSocket 지원 (향후 사용)
        proxy_http_version 1.1;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
    }

    # ✅ 추가: Health check 경로 (Nginx 레벨)
    location /nginx-health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }

    # ✅ 개선: Static 파일 캐싱
    location ~* \.(jpg|jpeg|png|gif|ico|css|js)$ {
        proxy_pass http://board_backend;
        expires 1d;
        add_header Cache-Control "public, immutable";
    }
}
EOF

echo "✅ Nginx configuration updated to port ${NEW_PORT}"

# ✅ 설정 테스트
if ! nginx -t; then
  echo "❌ Nginx config test failed, restoring backup"
  cp "$BACKUP_FILE" /etc/nginx/sites-available/board.conf
  exit 1
fi

echo "✅ Nginx configuration valid"
exit 0