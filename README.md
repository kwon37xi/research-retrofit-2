# research for retrofit 2 on java environment

* [Retrofit 2](https://square.github.io/retrofit/)
* [Retrofit Github](https://github.com/square/retrofit)
* [Retrofit 2 Maven Repository](https://mvnrepository.com/artifact/com.squareup.retrofit2)

Retrofit 2 는 OKHTTP 를 사용하는 HTTP 호출을 편리하게 해주는 라이브러리이다.

OKHTTP 는 완전한 비동기 non-blocking IO를 지원하지 않고, 쓰레드 기반 비동기만 지원하는 라이브러리이므로, 순수 non-blocking 은 아님을 확인해야 한다.

```
implementation com.squareup.retrofit2:retrofit:2.9.0
implementation "com.squareup.retrofit2:converter-jackson:2.9.0"
```

## @Query 파라미터 타입 직렬화
기본적으로 `@Query("paramName") String paramName` 에서 해당 파라미터는 URL 파라미터로 전환시 `toString()` 메소드 호출 결과로 직렬화된다.
이를 강제로 변경하려면 `retrofit2.Converter.Factory` 를 상속하여, `stringConverter()` 메소드를 override 해줘야한다.

* [LocalDateTimeParamConverterFactory](https://github.com/kwon37xi/research-retrofit-2/blob/main/src/main/java/kr/pe/kwonnam/research/retrofit2/converterfactories/LocalDateTimeParamConverterFactory.java)
* [EnumCodePropertyParamConverterFactory](https://github.com/kwon37xi/research-retrofit-2/blob/main/src/main/java/kr/pe/kwonnam/research/retrofit2/converterfactories/EnumCodePropertyParamConverterFactory.java)

## Jackson ObjectMapper 및 OkHhtpClient 설정
* [RetrofitClientBuilder](https://github.com/kwon37xi/research-retrofit-2/blob/main/src/main/java/kr/pe/kwonnam/research/retrofit2/RetrofitClientBuilder.java) 참조

## 동기식 작동 및, 응답 클래스를 직접 지정하기
* [SynchronousCallAdapterFactory](https://github.com/kwon37xi/research-retrofit-2/blob/main/src/main/java/kr/pe/kwonnam/research/retrofit2/SynchronousCallAdapterFactory.java) 참조
* 응답 결과를 `Call<>`로 감싸지 않고 바로 지정할 수 있다. [HttpBinClient#syncGetWithQueryParams](https://github.com/kwon37xi/research-retrofit-2/blob/main/src/main/java/kr/pe/kwonnam/research/retrofit2/client/HttpBinClient.java)
* 오류 응답(4xx, 5xx) 일 경우 해당 결과를 Exception으로 변환할 수 있어야한다.
* 재시도를 해야 한다.

## TODO
- Retry
- todo failedResponseHandler 한번만 생성해서 주입하게 처리
- Retry 여부 결정로직분리.
- TODO RxJava, Scala, Reactor 등 자유롭게 추가 가능해야 한다.

- [basic auth](https://stackoverflow.com/questions/43366164/retrofit-and-okhttp-basic-authentication)
  - https://futurestud.io/tutorials/android-basic-authentication-with-retrofit

## 참조 문서
* [Retrofit Tutorial](https://futurestud.io/tutorials/tag/retrofit)
* [Retrofit 2과 함께하는 정말 쉬운 HTTP](https://academy.realm.io/kr/posts/droidcon-jake-wharton-simple-http-retrofit-2/)
* [Java - Retrofit이란? (retrofit 사용법 자세한 설명)](https://galid1.tistory.com/617)
* [Using Retrofit 2.x as REST client - Tutorial](https://www.vogella.com/tutorials/Retrofit/article.html)
* [Retrofit의 Query, Path 등에 Enum클래스 사용하기](https://medium.com/%EB%B0%95%EC%83%81%EA%B6%8C%EC%9D%98-%EC%82%BD%EC%A7%88%EB%B8%94%EB%A1%9C%EA%B7%B8/retrofit%EC%9D%98-query-path-%EB%93%B1%EC%97%90-enum%ED%81%B4%EB%9E%98%EC%8A%A4-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0-6da80311677b)
  * 이 글의 설명은 잘못되었다(혹은 버전 문제거나).
  Retrofit 2 는 enum 의 `name()` 으로 올바르게 enum 값을 직렬화한다.
  enum 을 `name()`이 아닌 다른 것으로 직렬화하고자 할 때 사용할 것.
* [HwangEunmi/Retrofit-Sample](https://github.com/HwangEunmi/Retrofit-Sample)
* [동기 응답](https://stackoverflow.com/a/35104080/1051402) : `Call`이나 `CompletableFuture` 등으로 감싸지
 않고 원하는 객체를 바로 반환하게 하는 방법
* [Easily Retrying Network Requests on Android (with Retrofit 2)](https://medium.com/shuttl/easily-retrying-network-requests-on-android-with-retrofit-2-ee4b4b379eb7)
