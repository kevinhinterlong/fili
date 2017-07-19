#!/bin/sh

./zookeeper-${ZOOKEEPER_VERSION}/bin/zkServer.sh start
echo "Starting coordinator"
nohup java `cat conf-quickstart/druid/coordinator/jvm.config | xargs` \
    -cp conf-quickstart/druid/_common:conf-quickstart/druid/coordinator:lib/* io.druid.cli.Main server coordinator > c.log &
echo "Starting broker"
nohup java `cat conf-quickstart/druid/broker/jvm.config | xargs` -cp conf-quickstart/druid/_common:conf-quickstart/druid/broker:lib/* io.druid.cli.Main server broker > b.log &
echo "Starting historical"
nohup java `cat conf-quickstart/druid/historical/jvm.config | xargs` -cp conf-quickstart/druid/_common:conf-quickstart/druid/historical:lib/* io.druid.cli.Main server historical > h.log &
echo "Starting middleManager"
nohup java `cat conf-quickstart/druid/middleManager/jvm.config | xargs` -cp conf-quickstart/druid/_common:conf-quickstart/druid/middleManager:lib/* io.druid.cli.Main server middleManager > m.log &
echo "Starting overlord"
nohup java `cat conf-quickstart/druid/overlord/jvm.config | xargs` -cp conf-quickstart/druid/_common:conf-quickstart/druid/overlord:lib/* io.druid.cli.Main server overlord > o.log &


while ! nc -z localhost 8090; do
  sleep 2
done

curl -X 'POST' -H 'Content-Type:application/json' -d @quickstart/wikiticker-index.json localhost:8090/druid/indexer/v1/task

while ! nc -z localhost 8081; do
  sleep 2
done

echo "Druid configured successfully"