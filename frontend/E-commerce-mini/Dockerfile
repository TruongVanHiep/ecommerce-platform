# --- Build stage ---
FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm run build

# --- Runtime stage ---
FROM nginx:alpine

# File nằm trong /etc/nginx/templates sẽ được entrypoint của image nginx chạy
# qua envsubst rồi ghi ra /etc/nginx/conf.d/ trước khi nginx khởi động.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Địa chỉ backend. Mặc định là tên service trong docker-compose.yml nên
# `docker compose up` chạy y như trước, không cần khai báo gì thêm.
# Khi deploy lên PaaS thì ghi đè bằng địa chỉ nội bộ nền tảng cấp.
ENV BACKEND_URL=http://backend:8080

# CHỈ thay thế các biến bắt đầu bằng BACKEND_.
# envsubst chỉ đụng tới tên biến CÓ THẬT trong môi trường, nên $host, $scheme,
# $uri... của nginx thường vẫn an toàn kể cả khi thiếu dòng này. Nhưng nếu nền
# tảng tình cờ inject một biến trùng tên thì config hỏng âm thầm — đã test:
# đặt biến `host` thì `proxy_set_header Host $host` bị thay bằng giá trị của
# nó, nginx vẫn khởi động bình thường, không lỗi, không log, chỉ sai.
ENV NGINX_ENVSUBST_FILTER=^BACKEND_

COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
