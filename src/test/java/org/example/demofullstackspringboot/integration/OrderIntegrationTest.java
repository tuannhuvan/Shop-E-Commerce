package org.example.demofullstackspringboot.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.demofullstackspringboot.entity.Product;
import org.example.demofullstackspringboot.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrderIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static String createdOrderId;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    }

    @Test
    @Order(1)
    public void testCheckoutSuccess_ShouldCreateOrderAndDeductStock() throws Exception {
        // p201 ban đầu có 25 cái. Mua 2 cái -> còn 23 cái.
        String requestJson = """
            {
                "customerId": "c101",
                "items": [
                    {
                        "productId": "p201",
                        "quantity": 2
                    }
                ]
            }
            """;

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(70000000.00))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        createdOrderId = jsonNode.get("id").asText();

        // Kiểm tra tồn kho sản phẩm đã bị trừ nguyên tử
        Product product = productRepository.findById("p201").orElseThrow();
        assertEquals(23, product.getQuantity());
    }

    @Test
    @Order(2)
    public void testCheckoutFail_WhenStockInsufficient() throws Exception {
        // Yêu cầu mua 999 cái (trong khi kho chỉ còn 23 cái) -> Phải thất bại với status 422
        String requestJson = """
            {
                "customerId": "c101",
                "items": [
                    {
                        "productId": "p201",
                        "quantity": 999
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_STOCK_EMPTY"));

        // Kiểm tra tồn kho sản phẩm không bị thay đổi
        Product product = productRepository.findById("p201").orElseThrow();
        assertEquals(23, product.getQuantity());
    }

    @Test
    @Order(3)
    public void testCancelOrder_ShouldRestoreStock() throws Exception {
        // Hủy đơn hàng vừa tạo ở test 1 -> Tồn kho p201 phải được cộng trả lại 23 + 2 = 25
        mockMvc.perform(post("/api/v1/orders/" + createdOrderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        Product product = productRepository.findById("p201").orElseThrow();
        assertEquals(25, product.getQuantity());
    }

    @Test
    @Order(4)
    public void testCustomerDuplicateEmail_ShouldReturn422() throws Exception {
        // an.nguyen@test.com đã tồn tại trong DataInitializer
        String duplicateCustomerJson = """
            {
                "fullName": "Trùng Email",
                "email": "an.nguyen@test.com",
                "phone": "0911223344",
                "address": "TP.HCM"
            }
            """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateCustomerJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("ERR_EMAIL_DUPLICATE"));
    }

    @Test
    @Order(5)
    public void testCustomerInvalidEmail_ShouldReturn400() throws Exception {
        String invalidEmailJson = """
            {
                "fullName": "Sai Format",
                "email": "invalid-email-address",
                "phone": "0911223344"
            }
            """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEmailJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ERR_VALIDATION"));
    }
}
