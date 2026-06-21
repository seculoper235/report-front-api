package com.example.reportfrontapi.domain.product.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QProduct is a Querydsl query type for Product
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProduct extends EntityPathBase<Product> {

    private static final long serialVersionUID = -1128057607L;

    public static final QProduct product = new QProduct("product");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final BooleanPath active = createBoolean("active");

    public final StringPath brand = createString("brand");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath name = createString("name");

    public final NumberPath<Integer> pointCost = createNumber("pointCost", Integer.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QProduct(String variable) {
        super(Product.class, forVariable(variable));
    }

    public QProduct(Path<? extends Product> path) {
        super(path.getType(), path.getMetadata());
    }

    public QProduct(PathMetadata metadata) {
        super(Product.class, metadata);
    }

}

