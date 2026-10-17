package org.example.demofullstackspringboot.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderCreateRequest {

    @NotBlank(message = "Mã khách hàng không được để trống")
    private String customerId;

    @NotEmpty(message = "Danh sách sản phẩm không được rỗng")
    @Valid
    private List<OrderItemRequest> items;
}
