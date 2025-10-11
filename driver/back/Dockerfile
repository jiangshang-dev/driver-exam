FROM openjdk:21

RUN mkdir -p /data
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime
RUN echo "Asia/Shanghai" > /etc/timezone

WORKDIR /data

EXPOSE 9090

ADD ./*.jar /data/api.jar

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=dev

#ENTRYPOINT ["java","-jar","/data/api.jar","-Xms500m","-Xmn500m","--Spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
ENTRYPOINT ["java","-jar","/data/api.jar","-Xms500m","-Xmn500m","--server.port=9090","--Spring.profiles.active=${SPRING_PROFILES_ACTIVE}"]
