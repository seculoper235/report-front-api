package com.example.reportfrontapi.domain.habit;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QReportHabit is a Querydsl query type for ReportHabit
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReportHabit extends EntityPathBase<ReportHabit> {

    private static final long serialVersionUID = 165231284L;

    public static final QReportHabit reportHabit = new QReportHabit("reportHabit");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final EnumPath<HabitDivision> habitDivision = createEnum("habitDivision", HabitDivision.class);

    public final StringPath habitName = createString("habitName");

    public final NumberPath<Integer> habitPoint = createNumber("habitPoint", Integer.class);

    public final NumberPath<Long> reportCostId = createNumber("reportCostId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QReportHabit(String variable) {
        super(ReportHabit.class, forVariable(variable));
    }

    public QReportHabit(Path<? extends ReportHabit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QReportHabit(PathMetadata metadata) {
        super(ReportHabit.class, metadata);
    }

}

