package org.example.demofullstackspringboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.*;
import org.example.demofullstackspringboot.entity.*;
import org.example.demofullstackspringboot.exception.BadRequestException;
import org.example.demofullstackspringboot.exception.ResourceNotFoundException;
import org.example.demofullstackspringboot.exception.StockEmptyException;
import org.example.demofullstackspringboot.repository.CustomerRepository;
import org.example.demofullstackspringboot.repository.OrderRepository;
import org.example.demofullstackspringboot.repository.ProductRepository;
import org.example.demofullstackspringboot.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderPageResponse getOrders(OrderStatus status, Pageable pageable) {
        Page<Order> page = orderRepository.findOrdersWithFilter(status, pageable);
        List<OrderResponse> items = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        PaginationMetadata pagination = PaginationMetadata.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return OrderPageResponse.builder()
                .data(items)
                .pagination(pagination)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String id) {
        Order order = orderRepository.findOrderWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));
        return mapToResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderListResponse getCustomerOrders(String customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + customerId);
        }
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        List<OrderResponse> list = orders.stream()
                .map(this::mapToResponse)
                .toList();
        return OrderListResponse.builder().data(list).build();
    }

    @Override
    @Transactional // Atomic Transaction (ACID)
    public OrderResponse createOrder(OrderCreateRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + request.getCustomerId()));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BadRequestException("Danh sách mặt hàng đặt mua không được để trống");
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .customer(customer)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 1. Kiểm tra tồn kho Fail-Fast và tính tổng tiền
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + itemReq.getProductId()));

            if (product.getQuantity() < itemReq.getQuantity()) {
                throw new StockEmptyException("Sản phẩm '" + product.getName() + "' không đủ tồn kho (Còn lại: " + product.getQuantity() + ", Yêu cầu: " + itemReq.getQuantity() + ")");
            }

            // Trừ tồn kho nguyên tử
            product.setQuantity(product.getQuantity() - itemReq.getQuantity());
            productRepository.save(product);

            BigDecimal unitPrice = product.getPrice();
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .id(UUID.randomUUID().toString())
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        return mapToResponse(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String id, String newStatusStr) {
        Order order = orderRepository.findOrderWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(newStatusStr.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + newStatusStr + ". Cho phép: PENDING, PAID, SHIPPED, CANCELLED");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã ở trạng thái CANCELLED, không thể thay đổi trạng thái");
        }
        if (order.getStatus() == OrderStatus.SHIPPED && newStatus != OrderStatus.SHIPPED) {
            throw new BadRequestException("Đơn hàng đã được giao (SHIPPED), không thể chuyển trạng thái khác");
        }

        // Nếu chuyển sang CANCELLED thì thực hiện hoàn tồn kho
        if (newStatus == OrderStatus.CANCELLED) {
            restoreStockForOrder(order);
            order.setCancelReason("Hủy bởi Quản trị viên");
        }

        order.setStatus(newStatus);
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(String id) {
        Order order = orderRepository.findOrderWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng với ID: " + id));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy trước đó");
        }

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new BadRequestException("Đơn hàng đã được giao thành công, không thể hủy");
        }

        // Tự động hoàn trả tồn kho sản phẩm
        restoreStockForOrder(order);

        order.setStatus(OrderStatus.CANCELLED);
        if (order.getCancelReason() == null) {
            order.setCancelReason("Hủy bởi Khách hàng hoặc Hệ thống");
        }

        return mapToResponse(orderRepository.save(order));
    }

    private void restoreStockForOrder(Order order) {
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                itemResponses.add(OrderItemResponse.builder()
                        .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                        .productName(item.getProduct() != null ? item.getProduct().getName() : "Sản phẩm")
                        .unitPrice(item.getUnitPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getSubtotal())
                        .build());
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomer() != null ? order.getCustomer().getId() : null)
                .customerName(order.getCustomer() != null ? order.getCustomer().getFullName() : null)
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .cancelReason(order.getCancelReason())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
