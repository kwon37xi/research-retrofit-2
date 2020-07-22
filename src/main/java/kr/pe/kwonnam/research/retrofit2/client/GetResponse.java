package kr.pe.kwonnam.research.retrofit2.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class GetResponse {
    private ArgsAsString args;
    private Map<String, String> headers;
    @JsonProperty("origin")
    private String originIp;
    private String url;
}
