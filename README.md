# HelloWorldForFlinkWithKafkaNPostgreSQL
This is still Under development

# Abut Flink application

A Flink application project using Scala and SBT.

To run and test your application locally, you can just execute `sbt run` then select the main class that contains the Flink job . 

You can also package the application into a fat jar with `sbt assembly`, then submit it as usual, with something like: 

```
flink run -c org.example.WordCount /path/to/your/project/my-app/target/scala-2.11/testme-assembly-0.1-SNAPSHOT.jar
```

You can also run your application from within IntelliJ:  select the classpath of the 'mainRunner' module in the run/debug configurations.
Simply open 'Run -> Edit configurations...' and then select 'mainRunner' from the "Use classpath of module" dropbox. 

# About Local enviroment
## Pre-condition
* JDK8/11
* docker / docker compose

See also [here](#appendix-A)

## To Set-up
* Just run docker compose
```
sudo docker compose up -d
```

## About Kafka Web UI

* To see Kafka Web UI, go http://localhost:3000/

## About PostgreSQL UI (PgAdmin)

1. To see PostgreSQL Web UI, open http://localhost:18080/ on your browser
2. Input login parameters and press login button
 * Email Andree/Username: demo@sample.com
 * Password: password

## To Clean-up
```
sudo docker compose down

# If you want to remove docker volumes as well
# sudo docker compose down -v
```

## To register access info into Kafka connect
### Step1: Call Rest API to register access info into Kafka connect
```
curl -X POST \
 -H "Content-Type: application/json" \
 --data '{ "name": "demo-jdbc-sink", "config": { "connector.class": "PostgresSink", "tasks.max": 1, "connection.host": "localhost", "connection.port": "5431", "db.name": "demo_db", "connection.user": "postgres", "connection.password": "password", "insert.mode": "insert", "auto.create": "true", "topics": "demo_counter" } }' \
 http://localhost:8083/connectors
```
### Step2: Call Rest API to check Kafka connect status
```
# curl -s http://localhost:8083/connectors/(KAFKA_CONNECT_NAME)/status/
curl -s http://localhost:8083/connectors/demo-jdbc-sink/status/

{"name":"demo-jdbc-sink","connector":{"state":"RUNNING","worker_id":"localhost:8083"},"tasks":[{"id":0,"state":"RUNNING","worker_id":"localhost:8083"}],"type":"sink"}
```

# <span id="appendix-A">Appendix-A: Set-up docker/docker compose</span>
## To confirm your enviroment
* Java
```
$ java --version

openjdk 11.0.19 2023-04-18
OpenJDK Runtime Environment (build 11.0.19+7-post-Ubuntu-0ubuntu122.04.1)
OpenJDK 64-Bit Server VM (build 11.0.19+7-post-Ubuntu-0ubuntu122.04.1, mixed mode, sharing)
```
* docker/docker compose
```
$ docker --version
Docker version 24.0.4, build 3713ee1

$ docker compose --version
Docker version 24.0.4, build 3713ee1
```
## How to install
### Java
```
sudo apt update
sudo apt install openjdk-11-jre-headless
```
