package org.example.demofullstackspringboot.service;

import org.example.demofullstackspringboot.dto.CustomerCreateRequest;
import org.example.demofullstackspringboot.dto.CustomerPageResponse;
import org.example.demofullstackspringboot.dto.CustomerResponse;
import org.example.demofullstackspringboot.dto.CustomerUpdateRequest;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerPageResponse getCustomers(Pageable pageable);
    CustomerResponse getCustomerById(String id);
    CustomerResponse createCustomer(CustomerCreateRequest request);
    CustomerResponse updateCustomer(String id, CustomerUpdateRequest request);
}
