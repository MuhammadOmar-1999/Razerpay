package com.omar.razorpay.payment.mapper;

import com.omar.razorpay.payment.dto.reponse.OrderResponse;
import com.omar.razorpay.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {
    OrderResponse toOrderResponse(OrderRecord order);
}
