package kr.pe.kwonnam.research.retrofit2.converterfactories;

import retrofit2.Converter;
import retrofit2.Retrofit;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;

/**
 * {@link Year} Query 파라미터 변환
 */
public class YearParamConverterFactory extends Converter.Factory {

    public static final DateTimeFormatter YEAR_PATTERN = DateTimeFormatter.ofPattern("yyyy");

    @Override
    public Converter<?, String> stringConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        // 관련 없는 Type 은 null 반환
        if (type != Year.class) {
            return null;
        }

        return (Converter<Year, String>) value -> {
            // 사실은 안해도 된다. 기본 패턴이라서.
            return value.format(YEAR_PATTERN);
        };
    }
}
