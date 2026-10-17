package org.example.demofullstackspringboot.service;

import org.example.demofullstackspringboot.dto.OrderCreateRequest;
import org.example.demofullstackspringboot.dto.OrderListResponse;
import org.example.demofullstackspringboot.dto.OrderPageResponse;
import org.example.demofullstackspringboot.dto.OrderResponse;
import org.example.demofullstackspringboot.entity.OrderStatus;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderPageResponse getOrders(OrderStatus status, Pageable pageable);
    OrderResponse getOrderById(String id);
    OrderListResponse getCustomerOrders(String customerId);
    OrderResponse createOrder(OrderCreateRequest request);
    OrderResponse updateOrderStatus(String id, String status);
    OrderResponse cancelOrder(String id);
}
