package com.example.reportfrontapi.domain.gift;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QGiftInventory is a Querydsl query type for GiftInventory
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGiftInventory extends EntityPathBase<GiftInventory> {

    private static final long serialVersionUID = -297644096L;

    public static final QGiftInventory giftInventory = new QGiftInventory("giftInventory");

    public final com.example.reportfrontapi.common.entity.QBaseEntity _super = new com.example.reportfrontapi.common.entity.QBaseEntity(this);

    public final StringPath barcodeImageUrl = createString("barcodeImageUrl");

    public final StringPath code = createString("code");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    public final NumberPath<Long> giftInventoryId = createNumber("giftInventoryId", Long.class);

    public final NumberPath<Long> productId = createNumber("productId", Long.class);

    public final NumberPath<Long> redemptionOrderId = createNumber("redemptionOrderId", Long.class);

    public final EnumPath<GiftInventoryStatus> status = createEnum("status", GiftInventoryStatus.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final DatePath<java.time.LocalDate> validUntil = createDate("validUntil", java.time.LocalDate.class);

    public QGiftInventory(String variable) {
        super(GiftInventory.class, forVariable(variable));
    }

    public QGiftInventory(Path<? extends GiftInventory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGiftInventory(PathMetadata metadata) {
        super(GiftInventory.class, metadata);
    }

}

