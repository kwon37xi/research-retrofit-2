package kr.pe.kwonnam.research.retrofit2;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.time.Duration;

@Slf4j
public class RetrofitHttpBinClientBuilder {

    public static final String HTTPBIN_BASE_URL = "https://httpbin.org";

    private static Retrofit createRetrofitInstance() {
        ObjectMapper objectMapper = new ObjectMapper();
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofMillis(1000))
            .readTimeout(Duration.ofMillis(5000))
            .writeTimeout(Duration.ofMillis(5000))
            .build();

        return new Retrofit.Builder()
            .baseUrl(HTTPBIN_BASE_URL)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .client(okHttpClient)
            .build();
    }

    public static HttpBinClient createHttpBinClient() {
        return createRetrofitInstance().create(HttpBinClient.class);
    }
}
