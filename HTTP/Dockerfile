FROM httpd:2.4
COPY ./public-html/ /usr/local/apache2/htdocs/
COPY httpd.conf /usr/local/apache2/conf/httpd.conf
EXPOSE 80
#newgrp docker
#docker build -t my-apache2 .
#docker run -dit --name my-front -p 80:80 my-apache2
#docker rm -f my-front & docker rm -f my-java-api