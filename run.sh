#!/usr/bin/env bash
paths=/Users/aborowski/.m2/repository/org/apache/activemq/activemq-core/5.7.0/activemq-core-5.7.0.jar:/Users/aborowski/.m2/repository/org/slf4j/slf4j-api/1.6.6/slf4j-api-1.6.6.jar:/Users/aborowski/.m2/repository/org/apache/geronimo/specs/geronimo-jms_1.1_spec/1.1.1/geronimo-jms_1.1_spec-1.1.1.jar:/Users/aborowski/.m2/repository/org/apache/activemq/kahadb/5.7.0/kahadb-5.7.0.jar:/Users/aborowski/.m2/repository/org/apache/activemq/protobuf/activemq-protobuf/1.1/activemq-protobuf-1.1.jar:/Users/aborowski/.m2/repository/org/fusesource/mqtt-client/mqtt-client/1.3/mqtt-client-1.3.jar:/Users/aborowski/.m2/repository/org/fusesource/hawtdispatch/hawtdispatch-transport/1.11/hawtdispatch-transport-1.11.jar:/Users/aborowski/.m2/repository/org/fusesource/hawtdispatch/hawtdispatch/1.11/hawtdispatch-1.11.jar:/Users/aborowski/.m2/repository/org/fusesource/hawtbuf/hawtbuf/1.9/hawtbuf-1.9.jar:/Users/aborowski/.m2/repository/org/apache/geronimo/specs/geronimo-j2ee-management_1.1_spec/1.0.1/geronimo-j2ee-management_1.1_spec-1.0.1.jar:/Users/aborowski/.m2/repository/org/springframework/spring-context/3.0.7.RELEASE/spring-context-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/org/springframework/spring-aop/3.0.7.RELEASE/spring-aop-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/Users/aborowski/.m2/repository/org/springframework/spring-beans/3.0.7.RELEASE/spring-beans-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/org/springframework/spring-core/3.0.7.RELEASE/spring-core-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/commons-logging/commons-logging/1.1.1/commons-logging-1.1.1.jar:/Users/aborowski/.m2/repository/org/springframework/spring-expression/3.0.7.RELEASE/spring-expression-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/org/springframework/spring-asm/3.0.7.RELEASE/spring-asm-3.0.7.RELEASE.jar:/Users/aborowski/.m2/repository/commons-net/commons-net/3.1/commons-net-3.1.jar:/Users/aborowski/.m2/repository/org/jasypt/jasypt/1.9.0/jasypt-1.9.0.jar:/Users/aborowski/.m2/repository/org/projectlombok/lombok/1.16.8/lombok-1.16.8.jar:/Users/aborowski/.m2/repository/com/google/guava/guava/19.0/guava-19.0.jar:/Users/aborowski/.m2/repository/log4j/log4j/1.2.17/log4j-1.2.17.jar:/Users/aborowski/.m2/repository/args4j/args4j/2.33/args4j-2.33.jar
file=target/dcframework-1.0-SNAPSHOT.jar
while [ 1 ]; do
java -ea -cp ${paths}:${file} pl.adamborowski.zar.Main "$@"
done