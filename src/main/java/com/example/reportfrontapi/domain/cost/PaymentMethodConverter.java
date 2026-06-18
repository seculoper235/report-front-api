package com.example.reportfrontapi.domain.cost;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Arrays;

@Converter(autoApply = true)
public class PaymentMethodConverter implements AttributeConverter<PaymentMethod, String> {

    @Override
    public String convertToDatabaseColumn(PaymentMethod attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PaymentMethod convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return Arrays.stream(PaymentMethod.values())
                .filter(method -> method.getCode().equals(dbData))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment method code: " + dbData));
    }
}
