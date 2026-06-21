package com.example.reportfrontapi.common.code;

import com.example.reportfrontapi.domain.cost.model.CostAmountDivision;
import com.example.reportfrontapi.domain.cost.model.PaymentMethod;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CodeEnumJsonTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void serializesUsingCode() throws Exception {
        assertThat(objectMapper.writeValueAsString(CostAmountDivision.INCREASE))
                .isEqualTo("\"RP010001\"");
        assertThat(objectMapper.writeValueAsString(PaymentMethod.CARD))
                .isEqualTo("\"RP010002\"");
    }

    @Test
    void deserializesUsingCode() throws Exception {
        assertThat(objectMapper.readValue("\"RP010002\"", CostAmountDivision.class))
                .isEqualTo(CostAmountDivision.DECREASE);
        assertThat(objectMapper.readValue("\"RP010003\"", PaymentMethod.class))
                .isEqualTo(PaymentMethod.CREDIT_CARD);
    }
}
