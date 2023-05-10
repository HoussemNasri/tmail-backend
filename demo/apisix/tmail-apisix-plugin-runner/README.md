# TMail apisix plugin runner 

The plugin for handler revoked token:
- Provide the http server to receive logout_token from lemonLdap when has event logout channel
- Pre-filter for Apisix by checking token was revoked or not

## How to compile it 

- Java 11
- Run maven command:
```
mvn clean package
```

## How to use it
- Build Apisix docker image that support Java plugin runner
    - Require: 
      - Dockerfile (example: ../apisix/Dockerfile)
      - Runner Library `apache-apisix-java-plugin-runner-0.4.0-bin.tar.gz` (original: https://github.com/apache/apisix-java-plugin-runner)
    - Docker build: `docker build -t linagora/apisix:3.2.0-debian-javaplugin .`
- Import `tmail-apisix-plugin-runner-{version}.jar` into Apisix container. For detail ([Apache APISIX® Java Plugin Runner](https://apisix.apache.org/docs/java-plugin-runner/how-it-works/))

## Choice revoked token repository
- Be default the plugin use in-memory for storage. 
### Redis
- Provide the properties
    - `redis.url` (or environment `REDIS_URL`): [String] the redis url. For several url, use `,` character for separate
    - `redis.cluster.enabled` (or environment `REDIS_CLUSTER_ENABLE`): [Boolean]. `true` for redis master-replicas topology, 
    `false` for standalone topology

Eg: 
```yaml
redis:
  url: redis://secret1@redis.example.com:6379
  cluster.enable: false 
```
or
```yaml
redis:
  url: redis://secret1@redis-master.example.com:6379,redis://secret1@redis-replica1.example.com:6379
  cluster.enable: true 
```