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
public class Args {
    private String productName;
    private ProductType productType;
    private int quantity;
    private BigDecimal price;
    private LocalDate produceDate;
    private LocalTime produceTime;
    private LocalDateTime orderDateTime;
    private Year expireYear;
}
