package org.example.demofullstackspringboot.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPageResponse {
    private List<OrderResponse> data;
    private PaginationMetadata pagination;
}
