package com.omar.razerpay.payment.mapper;

import com.omar.razerpay.payment.dto.reponse.OrderResponse;
import com.omar.razerpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderResponse toOrderResponse(OrderRecord order);
}
