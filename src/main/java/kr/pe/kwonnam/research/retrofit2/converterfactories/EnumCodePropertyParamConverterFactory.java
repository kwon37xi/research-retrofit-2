package kr.pe.kwonnam.research.retrofit2.converterfactories;

import kr.pe.kwonnam.research.retrofit2.client.WithCode;
import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * {@code String getCode()} 메소드가 존재하는 객체(보통은 enum)은 {@code getCode()} 값으로 직렬화하게 하는 컨버터
 */
public class EnumCodePropertyParamConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        if (!(type instanceof Class)) {
            return null;
        }

        Class<?> clazz = (Class<?>)type;
        if (!(clazz.isEnum() && WithCode.class.isAssignableFrom(clazz))) {
            return null;
        }
        return (Converter<WithCode, String>) WithCode::getCode;
    }
}
