package org.example.demofullstackspringboot.dto;

import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductUpdateRequest {
    private String name;
    private String categoryId;

    @Min(value = 0, message = "Giá sản phẩm phải lớn hơn hoặc bằng 0")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0")
    private Integer quantity;

    private String description;
    private String imageUrl;
}
