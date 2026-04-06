package org.tavall.contractors.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;
import org.tavall.contractors.domain.mongo.DashboardConfig;
import org.tavall.contractors.domain.mongo.ProjectScopeDocument;

@Component
public class MongoSchemaInitializer implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    public MongoSchemaInitializer(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        ensureCollection(ProjectScopeDocument.class);
        ensureCollection(DashboardConfig.class);

        mongoTemplate.indexOps(ProjectScopeDocument.class)
            .ensureIndex(new Index().on("status", Sort.Direction.ASC).on("talentId", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProjectScopeDocument.class)
            .ensureIndex(new Index().on("clientId", Sort.Direction.ASC));
        mongoTemplate.indexOps(ProjectScopeDocument.class)
            .ensureIndex(new Index().on("checkoutId", Sort.Direction.ASC));

        mongoTemplate.indexOps(DashboardConfig.class)
            .ensureIndex(new Index().on("userId", Sort.Direction.ASC).unique());
    }

    private void ensureCollection(Class<?> type) {
        if (!mongoTemplate.collectionExists(type)) {
            mongoTemplate.createCollection(type);
        }
    }
}
