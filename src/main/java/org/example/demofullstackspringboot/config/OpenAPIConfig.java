package org.example.demofullstackspringboot.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI shopOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Shop E-Commerce RESTful API (API-First Contract)")
                        .description("Tài liệu đặc tả OpenAPI 3.0.3 cho hệ thống Shop E-Commerce Fullstack (Spring Boot 3.4+ & React/SPA). " +
                                "Hỗ trợ luồng kiểm tra tồn kho nguyên tử (Atomic Checkout), hoàn trả kho tự động khi hủy đơn và phân trang chuẩn RESTful.")
                        .version("2.0.0")
                        .contact(new Contact()
                                .name("Solution Architecture & BA Team")
                                .email("contact@shopecommerce.local"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Spring Boot Server")
                ));
    }
}
