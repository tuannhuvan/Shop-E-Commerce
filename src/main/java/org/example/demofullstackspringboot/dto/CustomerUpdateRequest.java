package org.example.demofullstackspringboot.dto;

import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerUpdateRequest {
    private String fullName;

    @Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8,9}$", message = "Số điện thoại không đúng định dạng (10-11 chữ số)")
    private String phone;

    private String address;
}
