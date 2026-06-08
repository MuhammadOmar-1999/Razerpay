package com.omar.razerpay.common.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Embeddable
// 1. Hide the all-args constructor from developers to enforce use of factory methods
@AllArgsConstructor(access = AccessLevel.PRIVATE)
// 2. Hide the no-args constructor, but leave it protected for JPA/Hibernate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Money {
    private int amountUnits;
    private String currency;

    public static Money of(int amountUnits, String currency) {
        return new Money(amountUnits, currency);
    }

    public static Money inr(int amountUnits){
        return new Money(amountUnits, "INR");
    }

    public Money add(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amountUnits + other.amountUnits, this.currency);
    }

    public Money subtract(Money other){
        if(!this.currency.equals(other.currency)){
            throw new IllegalArgumentException("Cannot subtract money with different currencies");
        }
        return new Money(this.amountUnits - other.amountUnits, this.currency);
    }
}
