package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
            ;

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
