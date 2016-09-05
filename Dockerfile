FROM java:8
RUN mkdir /data
COPY target/glasto-checker-*.jar /jar/wsplosher.jar

EXPOSE 8080
VOLUME /data

WORKDIR /data
CMD ["java","-jar","/jar/wsplosher.jar"]