package org.example.demofullstackspringboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCreateRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    private String description;
}
