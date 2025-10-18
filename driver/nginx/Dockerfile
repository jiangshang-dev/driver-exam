FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY ssl/codevateai.cn.pem /etc/nginx/ssl/codevateai.cn.pem
COPY ssl/codevateai.cn.key /etc/nginx/ssl/codevateai.cn.key
COPY html/50x.html /usr/share/nginx/html/50x.html

# 挂载缓存目录（可选）
RUN mkdir -p /usr/local/proxy_cache

# 设置权限
RUN chmod 600 /etc/nginx/ssl/codevateai.cn.key

EXPOSE 443

CMD ["nginx", "-g", "daemon off;"]
