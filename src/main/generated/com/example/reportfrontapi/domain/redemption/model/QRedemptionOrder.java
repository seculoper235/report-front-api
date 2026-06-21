package com.example.reportfrontapi.domain.redemption.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QRedemptionOrder is a Querydsl query type for RedemptionOrder
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QRedemptionOrder extends EntityPathBase<RedemptionOrder> {

    private static final long serialVersionUID = -706985459L;

    public static final QRedemptionOrder redemptionOrder = new QRedemptionOrder("redemptionOrder");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> giftInventoryId = createNumber("giftInventoryId", Long.class);

    public final StringPath idempotencyKey = createString("idempotencyKey");

    public final NumberPath<Integer> pointCost = createNumber("pointCost", Integer.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> redemptionOrderId = createNumber("redemptionOrderId", Long.class);

    public final EnumPath<RedemptionStatus> status = createEnum("status", RedemptionStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QRedemptionOrder(String variable) {
        super(RedemptionOrder.class, forVariable(variable));
    }

    public QRedemptionOrder(Path<? extends RedemptionOrder> path) {
        super(path.getType(), path.getMetadata());
    }

    public QRedemptionOrder(PathMetadata metadata) {
        super(RedemptionOrder.class, metadata);
    }

}

