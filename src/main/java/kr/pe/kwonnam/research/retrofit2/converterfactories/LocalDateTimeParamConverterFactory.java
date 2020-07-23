package kr.pe.kwonnam.research.retrofit2.converterfactories;

import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * {@link java.time.LocalDateTime} Query 파라미터 변환
 */
public class LocalDateTimeParamConverterFactory extends Converter.Factory {
    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        // 관련 없는 Type 은 null 반환
        if (type != LocalDateTime.class) {
            return null;
        }

        return (Converter<LocalDateTime, String>) value -> {
            // 사실은 안해도 된다. 기본이 ISO_LOCAL_DATE_TIME 이라서.
            return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        };
    }
}
