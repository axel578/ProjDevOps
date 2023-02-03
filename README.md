# TP DOCKER - 1/2/3 - ESCH AXEL

## Table of contents

- [TP DOCKER - 1/2/3 - ESCH AXEL](#tp-docker---123---esch-axel)
  - [Table of contents](#table-of-contents)
- [PART 1 (TP1)](#part-1-tp1)
    - [**1-1 Document your database container essentials: commands and Dockerfile.**](#1-1-document-your-database-container-essentials-commands-and-dockerfile)
      - [1-1-1. We specify the dockerfile for postgres:](#1-1-1-we-specify-the-dockerfile-for-postgres)
      - [1.1.2 Wecreate the netowrk and run the command for postgres in BDD folder:](#112-wecreate-the-netowrk-and-run-the-command-for-postgres-in-bdd-folder)
        - [note: we create a volume  /my/own/datadir -\> /var/lib/postgresql/data to avoid data losss](#note-we-create-a-volume--myowndatadir---varlibpostgresqldata-to-avoid-data-losss)
      - [1.1.3 We run adminer in the same network as the BDD :](#113-we-run-adminer-in-the-same-network-as-the-bdd-)
      - [1.1.4 We connect to the database from http://localhost:8090/?pgsql=mybddcontainer\&username=usr\&db=db\&ns=public](#114-we-connect-to-the-database-from-httplocalhost8090pgsqlmybddcontainerusernameusrdbdbnspublic)
    - [**1.2 Why do we need a multistage build? And explain each step of this dockerfile.**](#12-why-do-we-need-a-multistage-build-and-explain-each-step-of-this-dockerfile)
      - [1.2.1 From Docker configuration :](#121-from-docker-configuration-)
    - [Explaination :](#explaination-)
      - [1-3 Document docker-compose most important commands :](#1-3-document-docker-compose-most-important-commands-)
      - [1-4 Document your docker-compose file :](#1-4-document-your-docker-compose-file-)
      - [1-5 Document your publication commands and published images in dockerhub.](#1-5-document-your-publication-commands-and-published-images-in-dockerhub)
- [PART 2 (TP2)](#part-2-tp2)
      - [2-1 What are testcontainers?](#2-1-what-are-testcontainers)
      - [2-2 Document your Github Actions configurations.](#2-2-document-your-github-actions-configurations)
      - [Document your quality gate configuration.](#document-your-quality-gate-configuration)
- [Part 3 (TP3)](#part-3-tp3)
      - [3-1 Document your inventory and base commands](#3-1-document-your-inventory-and-base-commands)
      - [3-2 Document your playbook](#3-2-document-your-playbook)
      - [Document your docker\_container tasks configuration.](#document-your-docker_container-tasks-configuration)
          - [For the main playbook file:](#for-the-main-playbook-file)
          - [for docker:](#for-docker)
          - [for network:](#for-network)
          - [for BDD :](#for-bdd-)
          - [for APP :](#for-app-)
          - [for apache (proxy):](#for-apache-proxy)
        - [for front :](#for-front-)
        - [The command to run :](#the-command-to-run-)
        - [The Front](#the-front)
  - [Extra](#extra)


# PART 1 (TP1)

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
   # Create apache confguration in the same networks at other so that the revesre proxy is effective
   # The reverse proxy permit overload managment ..
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

docker tag web_backend awel899/web_backend:1.0 && docker push awel899/web_backend:1.0
docker tag web_httpd awel899/web_httpd:1.0 && docker push awel899/web_httpd:1.0

The same for the other application

# PART 2 (TP2)

#### 2-1 What are testcontainers?

testcontainers are JUnit tests that can be runned in docker.

#### 2-2 Document your Github Actions configurations.

```yaml
name: CI devops 2023
on:
  push:
    branches: [ "master" ]
  pull_request:
    branches: [ "master" ]

jobs:
  test-backend: 
    runs-on: ubuntu-22.04
    steps:
     #checkout your github code using actions/checkout@v2.5.0
      - uses: actions/checkout@v3

     #do the same with another action (actions/setup-java@v3) that enable to setup jdk 17
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with: 
          java-version: 17
          distribution: 'temurin'
          cache: maven
     #finally build your app with the latest command
      - name: Build and test with Maven
        run: mvn --file API_2/simple-api-student-main/pom.xml clean test
```

I have only one branch master, we fist install the open jdk 17 from temurin with maven cache.
Then we build and test with maven with the specified pom.xml


#### Document your quality gate configuration.

Here is first the full configuration github action :
```yaml
name: CI devops 2023
on:
  push:
    branches: [ "master" ]
  pull_request:
    branches: [ "master" ]

jobs:
  test-backend: 
    runs-on: ubuntu-22.04
    steps:
     #checkout your github code using actions/checkout@v2.5.0
      - uses: actions/checkout@v3

     #do the same with another action (actions/setup-java@v3) that enable to setup jdk 17 -
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with: 
          java-version: 17
          distribution: 'temurin'
          cache: maven
     #finally build your app with the latest command
      - name: Build and test with Maven
        run: mvn -B verify sonar:sonar -Dsonar.projectKey=axel578_ProjDevOps -Dsonar.organization=axel578 -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=${{ secrets.SONAR_TOKEN }}  --file API_2/simple-api-student-main/pom.xml
  # define job to build and publish docker image
  build-and-push-docker-image:
    needs: test-backend
     # run only when code is compiling and tests are passing
    runs-on: ubuntu-22.04

     # steps to perform in job
    steps:
      - name: Checkout code
        uses: actions/checkout@v3

      - name: Login to DockerHub
        run: docker login -u ${{ secrets.DOCKERHUB_USERNAME }} -p ${{ secrets.DOCKERHUB_TOKEN }}

      - name: Build image and push backend
        uses: docker/build-push-action@v3
        with:
          # relative path to the place where source code with Dockerfile is located
          context: ./API_TP2/simple-api-student-main
           # Note: tags has to be all lower-case
          tags:  ${{secrets.DOCKERHUB_USERNAME}}/web_backend
          push: ${{ github.ref == 'refs/heads/main' }}

      - name: Build image and push database
           # DO the same for database
        uses: docker/build-push-action@v3
        with:
          context: ./BDD
          tags: ${{secrets.DOCKERHUB_USERNAME}}/web_db
          push: ${{ github.ref == 'refs/heads/main' }}
      - name: Build image and push httpd
         # DO the same for httpd
        uses: docker/build-push-action@v3
        with:
          context: ./HTTP
          tags: ${{secrets.DOCKERHUB_USERNAME}}/web_httpd
          push: ${{ github.ref == 'refs/heads/main' }}
```


But the quality gate configuration is :

```
run: mvn -B verify sonar:sonar -Dsonar.projectKey=axel578_ProjDevOps -Dsonar.organization=axel578 -Dsonar.host.url=https://sonarcloud.io -Dsonar.login=${{ secrets.SONAR_TOKEN }}  --file API_2/simple-api-student-main/pom.xml
```

# Part 3 (TP3)

I have successfully build the front, configured the docker for load balancer on apache and the front.

#### 3-1 Document your inventory and base commands

here is the ```project/ansible/inventories/setup.yml``` file :
```
all:
 vars:
   ansible_user: centos
   ansible_ssh_private_key_file: ~/id_rsa
 children:
   prod:
     hosts: axel.esch.takima.cloud
```

#### 3-2 Document your playbook

I have the docker installation in the docker role task folder
and playbook looks like this :
```
- name: all
  roles:
    - docker
```
#### Document your docker_container tasks configuration.

###### For the main playbook file:
```
- name: all
  roles:
    - docker
    - network
    - database
    - app
    - proxy
```

###### for docker:
```
- hosts: all
  gather_facts: false
  become: yes

# Install Docker
  tasks:
  - name: Clean packages
    command:
      cmd: dnf clean -y packages

  - name: Install device-mapper-persistent-data
    dnf:
      name: device-mapper-persistent-data
      state: latest

  - name: Install lvm2
    dnf:
      name: lvm2
      state: latest

  - name: add repo docker
    command:
      cmd: sudo dnf config-manager --add-repo=https://download.docker.com/linux/centos/docker-ce.repo

  - name: Install Docker
    dnf:
      name: docker-ce
      state: present

  - name: install python3
    dnf:
      name: python3

  - name: Pip install
    pip:
      name: docker

  - name: Make sure Docker is running
    service: name=docker state=started
    tags: docker

```

###### for network:
```
  # here I create a configuration for the network which will belong the front/bdd/api/proxy
  - name: Create Docker network
    docker_network:
        name: app-network
        driver: bridge
        state: present

```

###### for BDD :
```
  # Here I create and start the BDD on my image (web_db)
  # We use a specific volume to have a persistant bdd
  # We create environment variables for bdd connection
  # We specify the external and internal port 5432 that postgre sql uses
  - name: Start BDD
    docker_container:
        name: bdd_pg
        image: awel899/web_db:1.0
        env:
          POSTGRES_DB: db
          POSTGRES_USER: usr
          POSTGRES_PASSWORD: pwd
        ports:
        - 5432:5432
        networks:
        - name: app-network
        state: started
        volumes:
        - /bdd/dir:/var/lib/postgresql/data
```

###### for APP :
```
    # Here we create the api (that requires db) on port 8080
    docker_container:
        name: my-java-api
        image: awel899/web_backend:1.0
        ports:
        - 8080:8080
        networks:
        - name: app-network
        state: started
        links:
        - db:db
```

###### for apache (proxy):
```
  # The apache that behaves like a proxy between the front and the api
  # We now can use in the future load balancing to ensure stability and availability
  - name: http_front run
    docker_container:
        name: http_front
        image: awel899/web_httpd:4.0
        ports:
        - 80:80
        networks:
        - name: app-network
        state: started
        links:
        - my-java-api:my-java-api
```

##### for front :
```
  # The node JS front
  - name: devops-front run
    docker_container:
        name: devops-front
        image: awel899/front:4.0
        ports:
        - 82:82
        networks:
        - name: app-network
        state: started
        links:
        - http_front:http_front
```

##### The command to run :

Installation with playbook
```
ansible-playbook -i inventories/setup.yml playbook.yml
```
The playbook contains all the roles to be called and run



##### The Front
The front is working and available on axel.esch.takima.io

## Extra

Extra is working for load balancing and has been checked.