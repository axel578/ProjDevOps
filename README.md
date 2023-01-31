# DOCKER TP 1 - ESCH AXEL

### **1-1 Document your database container essentials: commands and Dockerfile.**

#### 1-1-1. We specify the dockerfile for postgres:
```docker
FROM postgres:14.1-alpine

ENV POSTGRES_DB=db \
   POSTGRES_USER=usr \
   POSTGRES_PASSWORD=pwd

COPY CreateScheme.sql /docker-entrypoint-initdb.d
COPY InsertData.sql /docker-entrypoint-initdb.d
```

#### 1.1.2 Wecreate the netowrk and run the command for postgres in BDD folder:
##### note: we create a volume  /my/own/datadir -> /var/lib/postgresql/data to avoid data losss
```bash
docker network create app-network

docker build -t my-bdd-app . && docker run --name mybddcontainer -e POSTGRES_DB=db -e POSTGRES_USER=usr -e POSTGRES_PASSWORD=pwd  --network app-network -p 5433:5433  -v /my/own/datadir:/var/lib/postgresql/data -d my-bdd-app
```
#### 1.1.3 We run adminer in the same network as the BDD :
adminer:
```dockerfile
docker run \
    -p "8090:8080" \
    --net=app-network \
    --name=adminer \
    -d \
    adminer
```

#### 1.1.4 We connect to the database from http://localhost:8090/?pgsql=mybddcontainer&username=usr&db=db&ns=public



### **1.2 Why do we need a multistage build? And explain each step of this dockerfile.**

In this part, we create a default api on port 8080

#### 1.2.1 From Docker configuration :

```
FROM maven:3.8.6-amazoncorretto-17 AS myapp-build
ENV MYAPP_HOME /opt/myapp
WORKDIR $MYAPP_HOME
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Run
FROM amazoncorretto:17
ENV MYAPP_HOME /opt/myapp
WORKDIR $MYAPP_HOME
COPY --from=myapp-build $MYAPP_HOME/target/*.jar $MYAPP_HOME/myapp.jar

ENTRYPOINT java -jar myapp.jar
```

### Explaination :
With this configuration, we are describing the different steps necessary to :
1. Get the dependencies with maven
2. Build the package to a .jar and configuration for future usage (with docker run)

First we ask for the maven with java package (maven:3.8.6-amazoncorretto-17) for dependencies and build
Then we set the environment path MYAPP_HOME to /opt/myapp
We set the current working directory to the value of the env variable MYAPP_HOME
We copy pom.xml and src directory

We run maven

Then we install the java package for run, we set the current working directory like before
We copy to the folder that we will run the java executable, the jar

Once we have build we run : 
```
docker run -it --rm --name my-java-api -d -p "8080:8080"  my-java-app
```

#### 1-3 Document docker-compose most important commands :

The two commands I had to do to make everything work :

```
docker-compose build # build all the specified image in the docker-compose.yml
docker-compose up # run all the built images
```

#### 1-4 Document your docker-compose file :


```
version: '3.7'

#From here we declare the services we want to create
services:
   #the service backend is our Java Spring Boot API, we specify the folder, the specific container name for the proxy of the apache
   # and the network it belongs to ( so that it can contact the database and apache can contact the web API) with the ports,
   # also the db service must be started before the web api starts ( important since the web api communicate with the database )
    backend:
        build: ./API_2/simple-api-student-main
        container_name: my-java-api
        networks:
        - app-network
        ports:
        - 8080:8080
        depends_on:
        - db
   #we specify here the database service with the custom container_name for the backend api, and the environment variable and the
   #volume to ensure the database is not reset every time
    db:
        container_name: bdd_pg
        build: ./BDD
        ports:
        - 5432:5432
        networks:
        - app-network
        environment:
        - POSTGRES_DB=db
        - POSTGRES_USER=usr
        - POSTGRES_PASSWORD=pwd
        volumes:
        - /my/own/datadir:/var/lib/postgresql/data
   # Create apache confguration in the same networks at other so that the revere proxy is effective
    httpd:
        build: ./HTTP
        ports:
        - 80:80
        networks:
        - app-network
        depends_on:
        - backend

# Here we create the network app-network with brige to add an intermediate between each service ( no p2p communication )
networks:
    app-network:
      driver: bridge
```
#### 1-5 Document your publication commands and published images in dockerhub.

Then we tag :
docker tag web_db awel899/web_db:1.0
And we push :
docker push awel899/web_db:1.0

The same for the other application