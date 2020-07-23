package kr.pe.kwonnam.research.retrofit2.converterfactories;

import kr.pe.kwonnam.research.retrofit2.client.ProductType;
import kr.pe.kwonnam.research.retrofit2.client.ProductTypeWithCode;
import kr.pe.kwonnam.research.retrofit2.client.WithCode;
import org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import retrofit2.Converter;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EnumCodePropertyParamConverterFactoryTest")
class EnumCodePropertyParamConverterFactoryTest {

    private EnumCodePropertyParamConverterFactory enumCodePropertyParamConverterFactory;

    @BeforeEach
    void setUp() {
        enumCodePropertyParamConverterFactory = new EnumCodePropertyParamConverterFactory();
    }

    @Test
    @DisplayName("type 객체가 Class 가 아니면 null 을 리턴한다.")
    void typeIsNotClassReturnNull() {
        // 마구 만든, 어쨌든 Class 는 아닌 type
        GenericArrayType genericArrayType = () -> null;
        Converter<?, String> stringConverter = enumCodePropertyParamConverterFactory.stringConverter(genericArrayType, null, null);

        assertThat(stringConverter).isNull();
    }

    @Test
    @DisplayName("type 이 enum 이 아니면 null 을 리턴한다.")
    void typeIsNotEnumReturnNull() {
        Converter<?, String> stringConverter = enumCodePropertyParamConverterFactory.stringConverter(String.class, null, null);

        assertThat(stringConverter).isNull();
    }

    @Test
    @DisplayName("type 이 enum 이지만 WithCode는 아니면 null 을 리턴한다.")
    void typeIsEnumButNotWithCodeReturnNull() {
        Converter<?, String> stringConverter = enumCodePropertyParamConverterFactory.stringConverter(ProductType.class, null, null);

        assertThat(stringConverter).isNull();
    }

    @Test
    @DisplayName("type 이 enum 이 아니지만 WithCode라면 null 을 리턴한다.")
    void typeIsNotEnumButWithCodeReturnNull() {
        Converter<?, String> stringConverter = enumCodePropertyParamConverterFactory.stringConverter(WithCode.class, null, null);

        assertThat(stringConverter).isNull();
    }

    @DisplayName("type 이 enum 이면서 WithCode 라면 Converter 객체를 리턴한다.")
    @ParameterizedTest(name = "{index} - ProductType {0}")
    @EnumSource(ProductTypeWithCode.class)
    void typeIsEnumAndWithCodeReturnConverter(ProductTypeWithCode productTypeWithCode) throws IOException {
        Converter<WithCode, String> stringConverter = (Converter<WithCode, String>) enumCodePropertyParamConverterFactory.stringConverter(ProductTypeWithCode.class, null, null);

        assertThat(stringConverter).isNotNull();

        assertThat(stringConverter.convert(productTypeWithCode)).isEqualTo(productTypeWithCode.getCode());
    }
}