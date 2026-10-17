package org.example.demofullstackspringboot.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.*;
import org.example.demofullstackspringboot.service.CustomerService;
import org.example.demofullstackspringboot.service.OrderService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Quản lý thông tin và lịch sử khách hàng")
public class CustomerController {

    private final CustomerService customerService;
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Lấy danh sách khách hàng phân trang")
    public ResponseEntity<CustomerPageResponse> getCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.getCustomers(pageable));
    }

    @PostMapping
    @Operation(summary = "Đăng ký khách hàng mới")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse response = customerService.createCustomer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Lấy thông tin chi tiết khách hàng")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable String id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật hồ sơ khách hàng")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerUpdateRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @GetMapping("/{id}/orders")
    @Operation(summary = "Lịch sử đơn hàng của khách hàng")
    public ResponseEntity<OrderListResponse> getCustomerOrders(@PathVariable String id) {
        return ResponseEntity.ok(orderService.getCustomerOrders(id));
    }
}
