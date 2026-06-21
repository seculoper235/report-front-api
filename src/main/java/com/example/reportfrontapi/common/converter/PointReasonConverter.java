package com.example.reportfrontapi.common.converter;

import com.example.reportfrontapi.common.code.CodeEnum;
import com.example.reportfrontapi.domain.point.model.PointReason;
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
