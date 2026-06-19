package com.example.reportfrontapi.domain.cost;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QReportCost is a Querydsl query type for ReportCost
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportCost extends EntityPathBase<ReportCost> {

    private static final long serialVersionUID = 539064112L;

    public static final QReportCost reportCost = new QReportCost("reportCost");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final EnumPath<CostAmountDivision> amountDivision = createEnum("amountDivision", CostAmountDivision.class);

    public final StringPath categoryName = createString("categoryName");

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

    public QReportCost(String variable) {
        super(ReportCost.class, forVariable(variable));
    }

    public QReportCost(Path<? extends ReportCost> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReportCost(PathMetadata metadata) {
        super(ReportCost.class, metadata);
    }

}

