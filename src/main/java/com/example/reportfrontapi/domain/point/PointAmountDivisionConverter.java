package com.example.reportfrontapi.domain.point;

import com.example.reportfrontapi.common.code.CodeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PointAmountDivisionConverter implements AttributeConverter<PointAmountDivision, String> {

    @Override
    public String convertToDatabaseColumn(PointAmountDivision attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PointAmountDivision convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CodeEnum.fromCode(PointAmountDivision.class, dbData);
    }
}
