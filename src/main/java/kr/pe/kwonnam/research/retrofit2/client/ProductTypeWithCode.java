package kr.pe.kwonnam.research.retrofit2.client;

public enum ProductTypeWithCode implements WithCode {
    CAR("CA"),
    AUDIO("AD"),
    MOBILE_PHONE("MP"),
    LAPTOP("LT");

    private String code;

    ProductTypeWithCode(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
