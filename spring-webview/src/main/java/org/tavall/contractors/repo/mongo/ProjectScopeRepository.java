package org.tavall.contractors.repo.mongo;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.tavall.contractors.domain.mongo.ProjectScopeDocument;

import java.math.BigDecimal;
import java.util.List;

public interface ProjectScopeRepository extends MongoRepository<ProjectScopeDocument, String> {

    List<ProjectScopeDocument> findByStatusAndTalentIdIsNull(String status);

    List<ProjectScopeDocument> findByClientId(Long clientId);

    List<ProjectScopeDocument> findByTalentId(Long talentId);

    List<ProjectScopeDocument> findByCheckoutId(Long checkoutId);

    long countByStatus(String status);

    @Aggregation(pipeline = {
        "{ $match: { status: 'COMPLETED' } }",
        "{ $group: { _id: '$domain', totalProjects: { $sum: 1 }, coverImage: { $first: '$coverImage' } } }",
        "{ $project: { _id: 0, domain: '$_id', totalProjects: 1, coverImage: 1 } }"
    })
    List<PortfolioAggregate> aggregateCompletedByDomain();

    @Aggregation(pipeline = {
        "{ $match: { status: { $in: ['FUNDED','ACTIVE','REVIEW'] } } }",
        "{ $group: { _id: null, total: { $sum: '$escrowBalance' } } }",
        "{ $project: { _id: 0, total: 1 } }"
    })
    List<EscrowSumAggregate> sumActiveEscrow();

    interface PortfolioAggregate {
        String getDomain();

        Integer getTotalProjects();

        String getCoverImage();
    }

    interface EscrowSumAggregate {
        BigDecimal getTotal();
    }
}
