package org.example.demofullstackspringboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationMetadata {
    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;
}
