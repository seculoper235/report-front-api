package com.example.reportfrontapi.domain.point;

import com.example.reportfrontapi.common.code.CodeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointReasonConverter implements AttributeConverter<PointReason, String> {

    @Override
    public String convertToDatabaseColumn(PointReason attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PointReason convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CodeEnum.fromCode(PointReason.class, dbData);
    }
}
