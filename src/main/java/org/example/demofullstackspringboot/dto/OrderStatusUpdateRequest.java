package org.example.demofullstackspringboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Trạng thái không được để trống")
    private String status;
}
