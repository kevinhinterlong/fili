#!/bin/sh

export DRUID_HOST=druid #from docker-compose.yml

# Configure druid endpoints
export DRUID_COORD=http://$DRUID_HOST:8081/druid/coordinator/v1
export NON_UI_DRUID_BROKER=http://$DRUID_HOST:8082/druid/v2
export UI_DRUID_BROKER=http://$DRUID_HOST:8082/druid/v2

# Wait for indexer port to open
while ! nc -z $DRUID_HOST 8090; do
  sleep 2
done

# Wait for "wikiticker" to be ready
echo "Waiting for Druid to finish setting up"
while ! curl http://$DRUID_HOST:8081/druid/coordinator/v1/datasources | grep -q "wikiticker"; do
  sleep 5
done
echo "Druid finished setting up. Starting Fili"

mvn -pl fili-wikipedia-example exec:java \
-Dbard__druid_coord=$DRUID_COORD \
-Dbard__non_ui_druid_broker=$NON_UI_DRUID_BROKER \
-Dbard__ui_druid_broker=$UI_DRUID_BROKER
