package org.example.demofullstackspringboot.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerPageResponse {
    private List<CustomerResponse> data;
    private PaginationMetadata pagination;
}
