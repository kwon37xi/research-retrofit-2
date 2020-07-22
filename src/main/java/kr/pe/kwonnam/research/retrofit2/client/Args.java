package kr.pe.kwonnam.research.retrofit2.client;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Year;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Args {
    private String productName;
    private ProductType productType;
    private int quantity;
    private BigDecimal price;
    private LocalDate produceDate;
    private LocalTime produceTime;
    private LocalDateTime orderDateTime;
    private Year expireYear;

    @Builder
    Args(String productName, ProductType productType, int quantity, BigDecimal price, LocalDate produceDate, LocalTime produceTime, LocalDateTime orderDateTime, Year expireYear) {
        this.productName = productName;
        this.productType = productType;
        this.quantity = quantity;
        this.price = price;
        this.produceDate = produceDate;
        this.produceTime = produceTime;
        this.orderDateTime = orderDateTime;
        this.expireYear = expireYear;
    }
}
