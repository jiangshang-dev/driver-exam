FROM nginx:latest

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone

RUN mkdir -p /data/html

WORKDIR /data

ADD dist/ /data/html/
ADD config/ /etc/nginx/conf.d/

EXPOSE 80