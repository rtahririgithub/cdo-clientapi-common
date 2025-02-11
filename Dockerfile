#FROM maven:3.8.1-openjdk-8-slim
FROM adoptopenjdk/openjdk11:alpine-jre

#Copy the maven repo from the working directory back to the desired location within the Docker image
ADD .m2 /root/.m2

ENTRYPOINT ["mvn"]