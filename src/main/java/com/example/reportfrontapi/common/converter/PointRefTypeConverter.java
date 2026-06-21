package com.example.reportfrontapi.common.converter;

import com.example.reportfrontapi.common.code.CodeEnum;
import com.example.reportfrontapi.domain.point.model.PointRefType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointRefTypeConverter implements AttributeConverter<PointRefType, String> {

    @Override
    public String convertToDatabaseColumn(PointRefType attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PointRefType convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CodeEnum.fromCode(PointRefType.class, dbData);
    }
}
