package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import kr.pe.kwonnam.research.retrofit2.converterfactories.LocalDateTimeQueryParamConverterFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;
import java.util.Locale;
import java.util.TimeZone;

@Slf4j
public class RetrofitHttpBinClientBuilder {

    private final Logger okhttpLogger = LoggerFactory.getLogger("okhttp");

    private Retrofit createRetrofitInstance(String baseUrl, Duration connectionTimeout, Duration readTimeout, Duration writeTimeout) {
        ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ParameterNamesModule())
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule())
            .setLocale(Locale.getDefault())
            .setTimeZone(TimeZone.getDefault())
            // 모르은 property 를 역직렬화 할 때 오류없이 무시하게 한다.
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // 모르는 ENUM 값을 역직렬화 할 때 null로 취급하게 한다.
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            // 시간 관련 객체(LocalDateTime, java.util.Date)를 직렬화 할 때 timestamp 숫자값이 아닌 포맷팅 문자열로 한다.
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(connectionTimeout)
            .readTimeout(readTimeout)
            .writeTimeout(writeTimeout)
            .addInterceptor(new HttpLoggingInterceptor(message -> okhttpLogger.info("OKHTTP : {}", message))
                .setLevel(HttpLoggingInterceptor.Level.BODY))
            .build();

        return new Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .addConverterFactory(new LocalDateTimeQueryParamConverterFactory())
            .client(okHttpClient)
            .build();
    }

    public <T> T createHttpBinClient(Class<T> clientClass, String baseUrl, Duration connectionTimeout, Duration readTimeout, Duration writeTimeout) {
        return createRetrofitInstance(baseUrl, connectionTimeout, readTimeout, writeTimeout).create(clientClass);
    }
}
