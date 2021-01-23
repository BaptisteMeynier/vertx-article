FROM mysql:8.0.22

ENV MYSQL_DATABASE fishs
ENV MYSQL_ALLOW_EMPTY_PASSWORD yes
COPY ./sql-scripts/ /docker-entrypoint-initdb.d/
