package org.example.demofullstackspringboot.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {
    private String id;
    private String name;
    private String slug;
    private String description;
}
