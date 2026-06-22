package com.example.reportfrontapi.domain.category.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QCostCategory is a Querydsl query type for CostCategory
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCostCategory extends EntityPathBase<CostCategory> {

    private static final long serialVersionUID = -679250994L;

    public static final QCostCategory costCategory = new QCostCategory("costCategory");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final NumberPath<Long> categoryId = createNumber("categoryId", Long.class);

    public final StringPath categoryName = createString("categoryName");

    public final StringPath color = createString("color");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QCostCategory(String variable) {
        super(CostCategory.class, forVariable(variable));
    }

    public QCostCategory(Path<? extends CostCategory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCostCategory(PathMetadata metadata) {
        super(CostCategory.class, metadata);
    }

}

