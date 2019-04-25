{{#project}}
FROM openjdk:13-alpine
MAINTAINER {{#ownerName}}{{ownerName}}{{/ownerName}} <{{owner}}>
VOLUME /tmp
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-cp","app:app/lib/*","{{namespace}}.{{name}}.{{#capitalize}}{{name}}{{/capitalize}}App"]
{{/project}}
