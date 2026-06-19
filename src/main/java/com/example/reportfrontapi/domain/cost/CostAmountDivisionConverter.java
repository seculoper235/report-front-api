package com.example.reportfrontapi.domain.cost;

import com.example.reportfrontapi.common.code.CodeEnum;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CostAmountDivisionConverter implements AttributeConverter<CostAmountDivision, String> {

    @Override
    public String convertToDatabaseColumn(CostAmountDivision attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public CostAmountDivision convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return CodeEnum.fromCode(CostAmountDivision.class, dbData);
    }
}
