package com.example.reportfrontapi.domain.cost.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReportCost is a Querydsl query type for ReportCost
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportCost extends EntityPathBase<ReportCost> {

    private static final long serialVersionUID = 1130317781L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReportCost reportCost = new QReportCost("reportCost");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final EnumPath<CostAmountDivision> amountDivision = createEnum("amountDivision", CostAmountDivision.class);

    public final com.example.reportfrontapi.domain.category.model.QCostCategory category;

    public final NumberPath<java.math.BigInteger> costAmount = createNumber("costAmount", java.math.BigInteger.class);

    public final StringPath costDescription = createString("costDescription");

    public final EnumPath<CostDivision> costDivision = createEnum("costDivision", CostDivision.class);

    public final StringPath costName = createString("costName");

    public final NumberPath<Integer> costPoint = createNumber("costPoint", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final EnumPath<com.example.reportfrontapi.common.dto.Yn> fixedYn = createEnum("fixedYn", com.example.reportfrontapi.common.dto.Yn.class);

    public final DateTimePath<java.time.LocalDateTime> paymentAt = createDateTime("paymentAt", java.time.LocalDateTime.class);

    public final EnumPath<PaymentMethod> paymentMethod = createEnum("paymentMethod", PaymentMethod.class);

    public final NumberPath<Long> reportCostId = createNumber("reportCostId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QReportCost(String variable) {
        this(ReportCost.class, forVariable(variable), INITS);
    }

    public QReportCost(Path<? extends ReportCost> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReportCost(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReportCost(PathMetadata metadata, PathInits inits) {
        this(ReportCost.class, metadata, inits);
    }

    public QReportCost(Class<? extends ReportCost> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new com.example.reportfrontapi.domain.category.model.QCostCategory(forProperty("category")) : null;
    }

}

