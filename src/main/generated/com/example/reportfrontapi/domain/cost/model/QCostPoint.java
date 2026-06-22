package com.example.reportfrontapi.domain.cost.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QCostPoint is a Querydsl query type for CostPoint
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCostPoint extends EntityPathBase<CostPoint> {

    private static final long serialVersionUID = -1141039473L;

    public static final QCostPoint costPoint = new QCostPoint("costPoint");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final NumberPath<Long> costPointId = createNumber("costPointId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final EnumPath<CostDivision> division = createEnum("division", CostDivision.class);

    public final EnumPath<com.example.reportfrontapi.domain.user.model.CostPersona> persona = createEnum("persona", com.example.reportfrontapi.domain.user.model.CostPersona.class);

    public final NumberPath<Integer> pointAmount = createNumber("pointAmount", Integer.class);

    public final NumberPath<Long> reportCostId = createNumber("reportCostId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCostPoint(String variable) {
        super(CostPoint.class, forVariable(variable));
    }

    public QCostPoint(Path<? extends CostPoint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCostPoint(PathMetadata metadata) {
        super(CostPoint.class, metadata);
    }

}

