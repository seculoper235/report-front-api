package com.example.reportfrontapi.common.code;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.ValueSerializerModifier;

/**
 * CodeEnum을 name이 아닌 code 값으로 (역)직렬화하는 Jackson 모듈.
 * CodeEnum을 구현하는 모든 enum(PaymentMethod, CostAmountDivision 등)에 일괄 적용된다.
 * - 직렬화: enum -> code 문자열
 * - 역직렬화: code 문자열 -> enum (CodeEnum.fromCode)
 */
public class CodeEnumModule extends SimpleModule {

    public CodeEnumModule() {
        super("CodeEnumModule");

        setSerializerModifier(new ValueSerializerModifier() {
            @Override
            public ValueSerializer<?> modifyEnumSerializer(SerializationConfig config, JavaType valueType,
                                                           BeanDescription.Supplier beanDescRef,
                                                           ValueSerializer<?> serializer) {
                if (CodeEnum.class.isAssignableFrom(valueType.getRawClass())) {
                    return new CodeEnumSerializer();
                }
                return serializer;
            }
        });

        setDeserializerModifier(new ValueDeserializerModifier() {
            @Override
            public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type,
                                                               BeanDescription.Supplier beanDescRef,
                                                               ValueDeserializer<?> deserializer) {
                Class<?> raw = type.getRawClass();
                if (CodeEnum.class.isAssignableFrom(raw)) {
                    return new CodeEnumDeserializer(raw);
                }
                return deserializer;
            }
        });
    }

    private static class CodeEnumSerializer extends ValueSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
            gen.writeString(((CodeEnum) value).getCode());
        }
    }

    private static class CodeEnumDeserializer extends ValueDeserializer<Object> {
        private final Class<?> rawType;

        CodeEnumDeserializer(Class<?> rawType) {
            this.rawType = rawType;
        }

        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Object deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String code = p.getValueAsString();
            if (code == null) {
                return null;
            }
            return CodeEnum.fromCode((Class) rawType, code);
        }
    }
}
