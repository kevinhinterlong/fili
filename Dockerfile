FROM openjdk:alpine

# Copy the fili directory into /opt/fili inside docker
RUN mkdir -p /opt/fili
COPY . /opt/fili
WORKDIR /opt/fili

# Install maven and git to build fili, expose 9998
RUN apk update && apk add maven git
RUN mvn install
EXPOSE 9998

# Configure druid endpoints
ENV DRUID_COORD=http://localhost:8081/druid/coordinator/v1
ENV NON_UI_DRUID_BROKER=http://localhost:8082/druid/v2
ENV UI_DRUID_BROKER=http://localhost:8082/druid/v2

# Run the fili module [fili-wikipedia-example, fili-generic-example]
CMD mvn -pl fili-generic-example exec:java \
-Dbard__druid_coord=$DRUID_COORD \
-Dbard__non_ui_druid_broker=$NON_UI_DRUID_BROKER \
-Dbard__ui_druid_broker=$UI_DRUID_BROKER
