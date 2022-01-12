FROM gradle:jdk14 as builder

WORKDIR /app

COPY ./ /app

RUN gradle bootWar -x test -x googleJavaFormat

FROM tomcat:jdk14-openjdk-slim  as runner

COPY --from=builder /app/build/libs/oqcp-backend.war /usr/local/tomcat/webapps/ROOT.war

CMD ["catalina.sh", "run"]