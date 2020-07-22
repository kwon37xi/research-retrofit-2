package kr.pe.kwonnam.research.retrofit2.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class PostResponse {
    /**
     * query 파라미터
     */
    private Args args;

    /**
     * 요청 json body를 전체를 문자열로 받아서 Jackson 이 올바르게 직렬화했는지 검증.
     */
    private Map<String, Object> json;
    private Map<String, String> headers;
    private String origin;
    private String url;
    private String data;
    private Map<String, String> files;
    private Map<String, String> form;
}
