package org.example.demofullstackspringboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.OrderCreateRequest;
import org.example.demofullstackspringboot.dto.OrderPageResponse;
import org.example.demofullstackspringboot.dto.OrderResponse;
import org.example.demofullstackspringboot.dto.OrderStatusUpdateRequest;
import org.example.demofullstackspringboot.entity.OrderStatus;
import org.example.demofullstackspringboot.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Quản lý luồng đặt hàng, thanh toán và vòng đời đơn hàng")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Tra cứu danh sách đơn hàng (hỗ trợ lọc trạng thái và phân trang)")
    public ResponseEntity<OrderPageResponse> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status
    ) {
        OrderStatus orderStatus = null;
        if (status != null && !status.isBlank()) {
            orderStatus = OrderStatus.valueOf(status.toUpperCase().trim());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(orderService.getOrders(orderStatus, pageable));
    }

    @PostMapping
    @Operation(summary = "Đặt hàng nguyên tử (Atomic Checkout)")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderCreateRequest request) {
        OrderResponse response = orderService.createOrder(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Xem chi tiết đơn hàng")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Cập nhật trạng thái đơn hàng (Admin Dashboard)")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Hủy đơn hàng và tự động hoàn trả tồn kho sản phẩm")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
