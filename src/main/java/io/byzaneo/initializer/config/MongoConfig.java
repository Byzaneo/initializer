package io.byzaneo.initializer.config;

import com.mongodb.reactivestreams.client.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import java.util.Collection;

import static com.mongodb.reactivestreams.client.MongoClients.create;
import static java.lang.String.format;
import static java.util.Collections.singleton;

@Configuration
@EnableMongoAuditing
@EnableReactiveMongoRepositories("io.byzaneo.initializer.data")
class MongoConfig extends AbstractReactiveMongoConfiguration {

    @Value("${spring.data.mongodb.host}")
    private String host;

    @Value("${spring.data.mongodb.port}")
    private Integer port;

    @Value("${spring.data.mongodb.database}")
    private String  database;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return create(format("mongodb://%s:%d", host, port));
    }

    @Override
    protected Collection<String> getMappingBasePackages() {
        return singleton("io.byzaneo.initializer");
    }
}
