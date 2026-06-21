package com.example.reportfrontapi.domain.point.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QReportPoint is a Querydsl query type for ReportPoint
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportPoint extends EntityPathBase<ReportPoint> {

    private static final long serialVersionUID = -1904469593L;

    public static final QReportPoint reportPoint = new QReportPoint("reportPoint");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Integer> pointAmount = createNumber("pointAmount", Integer.class);

    public final EnumPath<PointAmountDivision> pointAmountDivision = createEnum("pointAmountDivision", PointAmountDivision.class);

    public final EnumPath<PointReason> reason = createEnum("reason", PointReason.class);

    public final NumberPath<Long> refId = createNumber("refId", Long.class);

    public final EnumPath<PointRefType> refType = createEnum("refType", PointRefType.class);

    public final NumberPath<Long> reportPointId = createNumber("reportPointId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QReportPoint(String variable) {
        super(ReportPoint.class, forVariable(variable));
    }

    public QReportPoint(Path<? extends ReportPoint> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReportPoint(PathMetadata metadata) {
        super(ReportPoint.class, metadata);
    }

}

