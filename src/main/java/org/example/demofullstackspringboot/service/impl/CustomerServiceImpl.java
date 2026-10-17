package org.example.demofullstackspringboot.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.demofullstackspringboot.dto.*;
import org.example.demofullstackspringboot.entity.Customer;
import org.example.demofullstackspringboot.exception.DuplicateResourceException;
import org.example.demofullstackspringboot.exception.ResourceNotFoundException;
import org.example.demofullstackspringboot.repository.CustomerRepository;
import org.example.demofullstackspringboot.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Override
    @Transactional(readOnly = true)
    public CustomerPageResponse getCustomers(Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(pageable);
        List<CustomerResponse> items = page.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        PaginationMetadata pagination = PaginationMetadata.builder()
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();

        return CustomerPageResponse.builder()
                .data(items)
                .pagination(pagination)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email '" + request.getEmail() + "' đã được sử dụng");
        }

        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .createdAt(LocalDateTime.now())
                .build();

        return mapToResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(String id, CustomerUpdateRequest request) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng với ID: " + id));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            customer.setFullName(request.getFullName());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            customer.setPhone(request.getPhone());
        }
        if (request.getAddress() != null) {
            customer.setAddress(request.getAddress());
        }

        return mapToResponse(customerRepository.save(customer));
    }

    private CustomerResponse mapToResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullName(customer.getFullName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .address(customer.getAddress())
                .createdAt(customer.getCreatedAt())
                .build();
    }
}
