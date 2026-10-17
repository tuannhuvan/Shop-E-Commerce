package org.example.demofullstackspringboot.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPageResponse {
    private List<ProductResponse> data;
    private PaginationMetadata pagination;
}
