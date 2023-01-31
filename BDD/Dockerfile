FROM postgres:14.1-alpine

ENV POSTGRES_DB=db \
   POSTGRES_USER=usr \
   POSTGRES_PASSWORD=pwd

COPY CreateScheme.sql /docker-entrypoint-initdb.d
COPY InsertData.sql /docker-entrypoint-initdb.d

#docker build -t my-bdd-app .
#docker run --name mybddcontainer -e POSTGRES_DB=db -e POSTGRES_USER=usr -e POSTGRES_PASSWORD=pwd -p 5432:5432 -d my-bdd-app
#docker network create app-network
#docker run \
#    -p "8090:8080" \
#    --net=app-network \
#    --name=adminer \
#    -d \
#    adminer

#docker cp httpd.conf my-front:/usr/local/apache2/conf/httpd.conf
#docker build -t my-bdd-app . && docker run --name mybddcontainer -e POSTGRES_DB=db -e POSTGRES_USER=usr -e POSTGRES_PASSWORD=pwd  --network app-network -p 5432:5432  -v /my/own/datadir:/var/lib/postgresql/data -d my-bdd-app