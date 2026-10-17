# KẾ HOẠCH & MA TRẬN KIỂM THỬ TOÀN DIỆN (TEST PLAN & MATRIX)
> Hệ thống: Shop E-Commerce Fullstack (Spring Boot 3.4+ & OpenAPI 3.0.3 & React)

## 1. TỔNG QUAN CHIẾN LƯỢC KIỂM THỬ 3 CẤP ĐỘ
Để bảo đảm chất lượng toàn diện theo yêu cầu đồ án mở rộng, quy trình testing được chia làm 3 cấp độ:
1. **API Contract Test**: Chạy tự động với Postman/Newman kiểm tra toàn bộ Endpoints theo đặc tả `shop-api.yaml`.
2. **Backend Unit & Integration Test**: Sử dụng JUnit 5, Spring Boot Test (`@SpringBootTest`), `MockMvc` và H2 In-Memory DB.
3. **End-to-End (E2E) Test**: Thử nghiệm liên thông từ Frontend Storefront đặt hàng -> Backend ghi CSDL -> Admin Dashboard đổi trạng thái -> Khách hàng hủy đơn hoàn tồn kho.

---

## 2. MA TRẬN KỊCH BẢN KIỂM THỬ (TEST MATRIX)

### 2.1. Module Khách hàng (`/customers`)
| Test ID | Module | Feature / Scenario | Input / Request Payload | Expected HTTP Status | Expected Response Payload / Behavior | Priority |
|---|---|---|---|---|---|---|
| **TC-CUST-001** | Customers | Tạo mới KH hợp lệ | `{"fullName": "Trần Văn X", "email": "x.tran@example.com", "phone": "0912345678"}` | `201 Created` | Header `Location` chứa URI KH mới, Body chứa UUID | High |
| **TC-CUST-002** | Customers | Tạo KH sai định dạng Email | `{"fullName": "Lỗi Email", "email": "invalid-email", "phone": "0912345678"}` | `400 Bad Request` | `ERR_VALIDATION`: Email không đúng định dạng | High |
| **TC-CUST-003** | Customers | Tạo KH Email trùng lặp | `{"fullName": "Trùng Email", "email": "nguyen.van.an@example.com", "phone": "0912345678"}` | `422 Unprocessable` | `ERR_EMAIL_DUPLICATE`: Email đã tồn tại | Medium |
| **TC-CUST-004** | Customers | Xem lịch sử đơn hàng KH | `GET /api/v1/customers/c101a011-0000-4000-a000-000000000001/orders` | `200 OK` | Danh sách lịch sử các đơn hàng của KH | High |

### 2.2. Module Sản phẩm & Danh mục (`/categories`, `/products`)
| Test ID | Module | Feature / Scenario | Input / Query Parameters | Expected HTTP Status | Expected Response Payload / Behavior | Priority |
|---|---|---|---|---|---|---|
| **TC-PROD-001** | Products | Lấy danh sách sản phẩm phân trang | `GET /api/v1/products?page=0&size=10&sort=price,desc` | `200 OK` | Mảng `data` chứa 10 sp xếp giá giảm dần + mảng `pagination` | High |
| **TC-PROD-002** | Products | Lọc sản phẩm theo danh mục & khoảng giá | `GET /api/v1/products?categoryId=cat00001...&minPrice=10000000&maxPrice=40000000` | `200 OK` | Các sản phẩm thỏa mãn khoảng giá từ 10tr - 40tr | High |
| **TC-PROD-003** | Products | Thêm sản phẩm có giá âm | `{"name": "SP Lỗi", "price": -5000, "quantity": 10}` | `400 Bad Request` | `ERR_VALIDATION`: Price phải lớn hơn hoặc bằng 0 | Medium |
| **TC-PROD-004** | Products | Cập nhật số lượng tồn kho | `PUT /api/v1/products/p201a011...` với `quantity: 50` | `200 OK` | Tồn kho sản phẩm được cập nhật lên 50 | High |

### 2.3. Module Giỏ hàng & Đặt hàng (`/orders`)
| Test ID | Module | Feature / Scenario | Input / Scenario | Expected HTTP Status | Expected Response Payload / Behavior | Priority |
|---|---|---|---|---|---|---|
| **TC-ORD-001** | Orders | Đặt hàng thành công (Tồn kho đủ) | `POST /api/v1/orders` mua 2 chiếc Dell XPS 15 (kho còn 25) | `201 Created` | Đơn hàng trạng thái `PENDING`, kho giảm từ 25 -> 23 | Critical |
| **TC-ORD-002** | Orders | Đặt hàng thất bại do hết tồn kho (Fail-Fast) | `POST /api/v1/orders` mua 100 chiếc Dell XPS 15 (kho chỉ còn 25) | `422 Unprocessable` | `ERR_STOCK_EMPTY`: Số lượng tồn kho không đủ | Critical |
| **TC-ORD-003** | Orders | Admin cập nhật trạng thái đơn (PENDING -> PAID) | `PATCH /api/v1/orders/{id}/status` với `status: PAID` | `200 OK` | Trạng thái đơn chuyển sang `PAID` | High |
| **TC-ORD-004** | Orders | Hủy đơn hàng & Tự động hoàn tồn kho | `POST /api/v1/orders/{id}/cancel` đơn mua 2 chiếc Dell XPS 15 | `200 OK` | Đơn chuyển sang `CANCELLED`, kho được cộng trả lại 23 -> 25 | Critical |

---

## 3. MÃ NGUỒN MẪU BACKEND UNIT & INTEGRATION TEST (SPRING BOOT)

### 3.1. Integration Test cho Luồng Checkout Đặt Hàng (`OrderIntegrationTest.java`)
```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional
    public void testCheckoutSuccess_ShouldCreateOrderAndDeductStock() throws Exception {
        String requestJson = """
            {
                "customerId": "c101a011-0000-4000-a000-000000000001",
                "items": [
                    {
                        "productId": "p201a011-0000-4000-a000-000000000001",
                        "quantity": 2
                    }
                ]
            }
            """;

        mockMvc.perform(post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(70000000.00));

        // Kiểm tra tồn kho sản phẩm đã bị trừ nguyên tử
        Product product = productRepository.findById("p201a011-0000-4000-a000-000000000001").orElseThrow();
        assertEquals(23, product.getQuantity());
    }

    @Test
    public void testCheckoutFail_WhenStockInsufficient() throws Exception {
        String requestJson = """
            {
                "customerId": "c101a011-0000-4000-a000-000000000001",
                "items": [
                    {
                        "productId": "p201a011-0000-4000-a000-000000000001",
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
    }
}
```
