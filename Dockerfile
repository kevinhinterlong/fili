FROM openjdk:alpine
RUN apk update
RUN apk add maven git

RUN mkdir -p /opt/fili
COPY . /opt/fili
WORKDIR /opt/fili

RUN mvn install -DskipTests -Dcheckstyle.skip=true -Dmaven.javadoc.skip=true

ENV FILI_PORT=9998
ENV DRUID_COORD=http://localhost:8081/druid/coordinator/v1
ENV NON_UI_DRUID_BROKER=http://localhost:8082/druid/v2
ENV UI_DRUID_BROKER=http://localhost:8082/druid/v2

EXPOSE $FILI_PORT

CMD mvn -pl fili-generic-example exec:java -Dbard__fili_port=$FILI_PORT \
-Dbard__druid_coord=$DRUID_COORD \
-Dbard__non_ui_druid_broker=$NON_UI_DRUID_BROKER \
-Dbard__ui_druid_broker=$UI_DRUID_BROKER


