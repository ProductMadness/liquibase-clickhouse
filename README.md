# liquibase-clickhouse

## How to use

Add gradle dependency:

``` groovy
implementation 'com.productmadness:liquibase-clickhouse:<latest version>'
```

Apply the annotation:
```java
@EnableClickhouseLiquibase
```

Then autoconfiguration will do the magic by injecting handlers for the ClickHouse DB into the Liquibase.

## Cluster mode
The cluster mode can be activated by adding the **_liquibaseClickhouse.conf_** file to the classpath (liquibase/lib/).
```
cluster {
    clusterName="{cluster}"
    tableZooKeeperPathPrefix="/clickhouse/tables/{shard}/{database}/"
    tableReplicaName="{replica}"
}
```
In this mode, liquibase will create its own tables as replicated.<br/>
All changes in these files will be replicated on the entire cluster.<br/>
Your updates should also affect the entire cluster either by using ON CLUSTER clause, or by using replicated tables.
