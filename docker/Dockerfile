FROM postgres:13.4

ADD init.sql /docker-entrypoint-initdb.d/

ENV TZ="Asia/Tbilisi"
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
