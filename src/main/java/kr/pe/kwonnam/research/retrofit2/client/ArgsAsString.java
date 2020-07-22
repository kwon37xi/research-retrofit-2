package kr.pe.kwonnam.research.retrofit2.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

@Getter
@Setter
@ToString
public class ArgsAsString {
    private String productName;
    private String productType;
    private int quantity;
    private String price;
    private String produceDate;
    private String produceTime;
    private String orderDateTime;
    private String expireYear;

//    private String productName;
//    private int quantity;
//    private BigDecimal price;
//    private LocalDate produceDate;
//    private LocalTime produceTime;
//    private LocalDateTime orderDate;
//    private Year expireYear;
}
