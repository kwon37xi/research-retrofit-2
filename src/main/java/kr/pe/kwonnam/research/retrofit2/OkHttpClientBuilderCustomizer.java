package kr.pe.kwonnam.research.retrofit2;

import okhttp3.OkHttpClient;

public interface OkHttpClientBuilderCustomizer {
    void customize(OkHttpClient.Builder okHttpClientBuilder);
}
