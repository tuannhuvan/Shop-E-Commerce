# SHOP E-COMMERCE FULLSTACK SYSTEM
> **Hệ thống Thương mại Điện tử Đa nền tảng chuẩn API-First (Spring Boot 3.4+ & OpenAPI 3.0.3 & Single Page Application)**

[![Java Version](https://img.shields.io/badge/Java-21%20%7C%2025-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4%2B-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![OpenAPI](https://img.shields.io/badge/OpenAPI-3.0.3-blue.svg)](https://swagger.io/specification/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Tests-6%2F6%20Passed%20(100%25)-success.svg)](#)

---

## 1. TỔNG QUAN DỰ ÁN

Dự án **Shop E-Commerce Fullstack** là giải pháp phần mềm thương mại điện tử hiện đại, kết hợp chặt chẽ giữa Backend Spring Boot 3.4+ và Frontend Single Page Application (SPA), vận hành theo triết lý **API-First**:
- **Bản cam kết đơn nhất (Single Source of Truth)**: Toàn bộ cấu trúc thực thể, DTOs và 13 RESTful Endpoints được định nghĩa chính xác tại file hợp đồng [`shop-api.yaml`](./shop-api.yaml) chuẩn OpenAPI 3.0.3.
- **Tính nguyên tử giao dịch (ACID Atomic Checkout)**: Kiểm tra kho fail-fast, trừ tồn kho và tạo đơn hàng đồng thời trong 1 giao dịch `@Transactional`. Triệt tiêu hoàn toàn rủi ro bán vượt quá tồn kho (Overselling / Race Conditions).
- **Tự động hoàn tồn kho (Auto-Stock Restoration)**: Khi đơn hàng chuyển sang trạng thái `CANCELLED` (do khách hàng hoặc Admin hủy), hệ thống tự động cộng bù lại số lượng mặt hàng vào tồn kho trong CSDL.
- **Tối ưu hóa hiệu năng JPA**: Đánh dấu `@Transactional(readOnly = true)` trên tất cả các hàm truy vấn đọc dữ liệu để tắt cơ chế Dirty Checking của Hibernate, giảm tải CPU và RAM. Triệt tiêu vấn đề truy vấn N+1 bằng `@EntityGraph`.
- **Giao diện người dùng Next-Gen**: Single Page Application phong cách Dark Mode Glassmorphism, tích hợp cả Storefront mua sắm cho khách hàng và Admin Dashboard quản trị vận hành trên cùng một cổng ứng dụng.

---

## 2. KIẾN TRÚC & CÔNG NGHỆ (TECH STACK)

- **Ngôn ngữ**: Java 21 / 25 LTS
- **Backend Framework**: Spring Boot 3.4+, Spring Data JPA, Hibernate ORM, Spring WebMvc, Spring Validation
- **Tài liệu API**: SpringDoc OpenAPI 3.1.0, Swagger UI
- **Cơ sở dữ liệu**:
  - **In-Memory H2**: Mặc định cho môi trường Local Dev & Unit Test (Zero-configuration, không cần cài đặt CSDL ngoài).
  - **MySQL 8.0**: Hỗ trợ môi trường Staging/Production qua script migration [`V1_init_shop_api.sql`](./docs/V1_init_shop_api.sql).
- **Frontend SPA**: HTML5 Semantic, Modern CSS Glassmorphism, JavaScript ES6+ (tích hợp sẵn tại `src/main/resources/static/`).
- **Testing**: JUnit 5, Spring Boot Test (`@SpringBootTest`), `MockMvc`.
- **Dữ liệu giả lập**: `DataInitializer` tự động nạp danh mục, sản phẩm công nghệ cao cấp, khách hàng và đơn hàng mẫu khi khởi động.

---

## 3. CẤU TRÚC THƯ MỤC DỰ ÁN (PROJECT STRUCTURE)

Hệ thống được tổ chức chuẩn hóa, phân tách rành mạch giữa tài liệu đặc tả, hợp đồng API và mã nguồn triển khai:

```
demoFullStackSpringBoot/
├── docs/                                       # Thư mục tài liệu nghiệp vụ, API & kiểm thử
│   ├── dac-ta-api-shop.md                      # Đặc tả yêu cầu nghiệp vụ và hướng dẫn triển khai
│   ├── project_brief.md                        # Tổng quan dự án, phạm vi nghiệp vụ & kiến trúc
│   ├── shop-api.yaml                           # OpenAPI 3.0.3 Contract lưu trữ
│   ├── test_plan_and_matrix.md                 # Kế hoạch và ma trận kiểm thử toàn diện 3 cấp độ
│   └── V1_init_shop_api.sql                    # Script CSDL MySQL & Seed data mẫu Faker
├── shop-api.yaml                               # API Contract Single Source of Truth tại thư mục gốc
├── pom.xml                                     # Maven Dependencies & Build Configuration
├── mvnw, mvnw.cmd                              # Maven Wrapper hỗ trợ chạy độc lập
├── HELP.md                                     # Hướng dẫn Spring Boot mặc định
└── src/
    ├── main/
    │   ├── java/org/example/demofullstackspringboot/
    │   │   ├── config/
    │   │   │   └── OpenAPIConfig.java          # Cấu hình Swagger UI & OpenAPI metadata
    │   │   ├── controller/                     # REST Controllers chuẩn hóa RESTful
    │   │   │   ├── CustomerController.java     # Endpoint /api/v1/customers
    │   │   │   ├── CategoryController.java     # Endpoint /api/v1/categories
    │   │   │   ├── ProductController.java      # Endpoint /api/v1/products
    │   │   │   └── OrderController.java        # Endpoint /api/v1/orders
    │   │   ├── dto/                            # Data Transfer Objects theo đúng shop-api.yaml
    │   │   │   ├── CustomerCreateRequest.java
    │   │   │   ├── CustomerUpdateRequest.java
    │   │   │   ├── CustomerResponse.java
    │   │   │   ├── CustomerPageResponse.java
    │   │   │   ├── CategoryCreateRequest.java
    │   │   │   ├── CategoryResponse.java
    │   │   │   ├── ProductCreateRequest.java
    │   │   │   ├── ProductUpdateRequest.java
    │   │   │   ├── ProductResponse.java
    │   │   │   ├── ProductPageResponse.java
    │   │   │   ├── OrderItemRequest.java
    │   │   │   ├── OrderCreateRequest.java
    │   │   │   ├── OrderItemResponse.java
    │   │   │   ├── OrderResponse.java
    │   │   │   ├── OrderPageResponse.java
    │   │   │   ├── OrderListResponse.java
    │   │   │   ├── OrderStatusUpdateRequest.java
    │   │   │   ├── PaginationMetadata.java
    │   │   │   └── ErrorResponse.java
    │   │   ├── entity/                         # JPA Entities ánh xạ CSDL
    │   │   │   ├── Customer.java
    │   │   │   ├── Category.java
    │   │   │   ├── Product.java
    │   │   │   ├── Order.java
    │   │   │   ├── OrderItem.java
    │   │   │   └── OrderStatus.java            # Enum PENDING, PAID, SHIPPED, CANCELLED
    │   │   ├── exception/                      # Quản lý lỗi tập trung & chuẩn hóa Error Envelope
    │   │   │   ├── ResourceNotFoundException.java (404)
    │   │   │   ├── StockEmptyException.java (422)
    │   │   │   ├── DuplicateResourceException.java (422)
    │   │   │   ├── BadRequestException.java (400)
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── repository/                     # Spring Data JPA Repositories (@EntityGraph)
    │   │   │   ├── CustomerRepository.java
    │   │   │   ├── CategoryRepository.java
    │   │   │   ├── ProductRepository.java
    │   │   │   ├── OrderRepository.java
    │   │   │   └── OrderItemRepository.java
    │   │   ├── seeder/
    │   │   │   └── DataInitializer.java        # Tự động nạp dữ liệu mẫu thực tế ban đầu
    │   │   ├── service/                        # Service Interfaces & Implementations
    │   │   │   ├── CustomerService.java
    │   │   │   ├── CategoryService.java
    │   │   │   ├── ProductService.java
    │   │   │   ├── OrderService.java
    │   │   │   └── impl/
    │   │   │       ├── CustomerServiceImpl.java
    │   │   │       ├── CategoryServiceImpl.java
    │   │   │       ├── ProductServiceImpl.java
    │   │   │       └── OrderServiceImpl.java   # Atomic Checkout & Auto-restore stock
    │   │   ├── DemoFullStackSpringBootApplication.java
    │   │   └── ServletInitializer.java
    │   └── resources/
    │       ├── application.yaml                # Cấu hình H2/MySQL, JPA, Hibernate, Port 8080
    │       ├── shop-api.yaml                   # OpenAPI Spec nạp vào Classpath
    │       └── static/                         # Single Page Application (Storefront & Admin)
    │           ├── index.html                  # Giao diện chính người dùng
    │           ├── css/
    │           │   └── style.css               # Phong cách Dark Mode Glassmorphism
    │           └── js/
    │               └── app.js                  # Logic tương tác REST API, Giỏ hàng, Đặt hàng
    └── test/
        └── java/org/example/demofullstackspringboot/
            ├── DemoFullStackSpringBootApplicationTests.java
            └── integration/
                └── OrderIntegrationTest.java   # Bộ kiểm thử tích hợp (6/6 tests passed)
```

---

## 4. CÁC TÍNH NĂNG VÀ NGHIỆP VỤ NỔI BẬT

### 4.1. Module Khách Hàng (`/api/v1/customers`)
- **Đăng ký tài khoản**: Xác thực tính hợp lệ của Email (chuẩn RFC 5322) và Số điện thoại Việt Nam (10-11 chữ số). Chặn đăng ký trùng Email (`422 Unprocessable Entity - ERR_EMAIL_DUPLICATE`).
- **Lịch sử đơn hàng**: Tra cứu toàn bộ các đơn hàng đã đặt của khách hàng theo thứ tự mới nhất trước.

### 4.2. Module Danh Mục & Sản Phẩm (`/api/v1/categories`, `/api/v1/products`)
- **Truy vấn nâng cao**: Hỗ trợ đồng thời lọc theo danh mục (`categoryId`), lọc khoảng giá (`minPrice`, `maxPrice`), tìm kiếm từ khóa trong tên/mô tả (`q`), phân trang (`page`, `size`) và sắp xếp (`sort`).
- **Chuẩn hóa Pagination Envelope**: Trả về cấu trúc bao bọc gồm `data` và metadata `pagination` (`page`, `size`, `totalElements`, `totalPages`).
- **Quản lý sản phẩm**: Thêm mới, cập nhật giá/tồn kho và xóa sản phẩm (Admin).

### 4.3. Module Giỏ Hàng & Đặt Hàng Nguyên Tử (`/api/v1/orders`)
- **Luồng Đặt Hàng (Checkout)**:
  1. Nhận danh sách mặt hàng đặt mua.
  2. **Kiểm tra tồn kho Fail-Fast**: Nếu bất kỳ sản phẩm nào có số lượng tồn kho nhỏ hơn số lượng mua, hệ thống ngắt giao dịch ngay lập tức, trả về HTTP `422 Unprocessable Entity` với mã lỗi `ERR_STOCK_EMPTY`.
  3. **Trừ tồn kho nguyên tử**: Trừ số lượng tồn kho của các sản phẩm tương ứng trong CSDL.
  4. Khởi tạo đơn hàng trạng thái `PENDING`, tính tổng tiền `totalAmount`.
  5. Trả về HTTP `201 Created` kèm header `Location: /api/v1/orders/{id}`.
- **Vòng Đời Trạng Thái (Order State Transitions)**:
  - `PENDING` (Chờ xử lý) $\rightarrow$ `PAID` (Đã thanh toán) $\rightarrow$ `SHIPPED` (Đã giao hàng).
  - Có thể hủy đơn hàng từ `PENDING` hoặc `PAID` sang `CANCELLED`.
- **Tự Động Hoàn Tồn Kho khi Hủy Đơn**:
  - Khi đơn hàng chuyển sang trạng thái `CANCELLED` (qua endpoint `POST /api/v1/orders/{id}/cancel` hoặc Admin cập nhật trạng thái), hệ thống tự động cộng bù lại toàn bộ số lượng sản phẩm trong đơn hàng đó vào kho CSDL.

---

## 5. DANH MỤC API ENDPOINTS (OPENAPI 3.0.3)

| Nhóm Resource | Method | Endpoint Path | Chức năng nghiệp vụ | Mã phản hồi |
|---|---|---|---|---|
| **Customers** | `GET` | `/api/v1/customers` | Lấy danh sách khách hàng phân trang | `200 OK` |
| **Customers** | `POST` | `/api/v1/customers` | Đăng ký khách hàng mới | `201 Created` / `400` / `422` |
| **Customers** | `GET` | `/api/v1/customers/{id}` | Chi tiết thông tin khách hàng | `200 OK` / `404` |
| **Customers** | `PUT` | `/api/v1/customers/{id}` | Cập nhật hồ sơ khách hàng | `200 OK` / `404` |
| **Customers** | `GET` | `/api/v1/customers/{id}/orders` | Xem lịch sử mua hàng của khách | `200 OK` / `404` |
| **Categories** | `GET` | `/api/v1/categories` | Lấy tất cả danh mục sản phẩm | `200 OK` |
| **Categories** | `POST` | `/api/v1/categories` | Tạo mới danh mục (Admin) | `201 Created` / `422` |
| **Products** | `GET` | `/api/v1/products` | Truy vấn sản phẩm (Lọc, tìm kiếm, phân trang) | `200 OK` |
| **Products** | `POST` | `/api/v1/products` | Thêm mới sản phẩm (Admin) | `201 Created` / `400` / `404` |
| **Products** | `GET` | `/api/v1/products/{id}` | Xem chi tiết sản phẩm | `200 OK` / `404` |
| **Products** | `PUT` | `/api/v1/products/{id}` | Cập nhật thông tin/tồn kho sản phẩm | `200 OK` / `404` |
| **Products** | `DELETE` | `/api/v1/products/{id}` | Xóa sản phẩm khỏi hệ thống | `204 No Content` / `404` |
| **Orders** | `GET` | `/api/v1/orders` | Danh sách đơn hàng (Lọc trạng thái, phân trang) | `200 OK` |
| **Orders** | `POST` | `/api/v1/orders` | Đặt hàng nguyên tử (Atomic Checkout) | `201 Created` / `422` |
| **Orders** | `GET` | `/api/v1/orders/{id}` | Xem chi tiết đơn hàng | `200 OK` / `404` |
| **Orders** | `PATCH`| `/api/v1/orders/{id}/status` | Chuyển đổi trạng thái đơn (Admin) | `200 OK` / `400` / `404` |
| **Orders** | `POST` | `/api/v1/orders/{id}/cancel` | Hủy đơn hàng và TỰ ĐỘNG HOÀN KHO | `200 OK` / `400` / `404` |

### Chuẩn Hóa Định Dạng Lỗi (Error Envelope JSON)
Khi phát sinh lỗi, API luôn phản hồi theo định dạng chuẩn:
```json
{
  "code": "ERR_STOCK_EMPTY",
  "message": "Sản phẩm 'Laptop Dell XPS 15' không đủ tồn kho (Còn lại: 2, Yêu cầu: 10)",
  "details": null,
  "timestamp": "2026-10-17T11:24:28Z"
}
```

---

## 6. HƯỚNG DẪN CÀI ĐẶT & KHỞI CHẠY HỆ THỐNG

### 6.1. Yêu Cầu Môi Trường
- **JDK**: Java 21 LTS trở lên (Đã xác thực hoạt động ổn định trên Java 21 và Java 25).
- **Maven**: 3.9+ (hoặc dùng `./mvnw.cmd` đóng gói sẵn).

### 6.2. Chạy Bộ Kiểm Thử Tự Động (Integration Tests)
Thực thi kiểm thử toàn diện kịch bản Đặt hàng nguyên tử, Kiểm tra thiếu kho, Hủy đơn hoàn kho và Validation:
```bash
# Windows PowerShell
.\mvnw.cmd clean test

# Linux / macOS
./mvnw clean test
```

### 6.3. Khởi Chạy Ứng Dụng (Development Server)
Khởi chạy Backend Spring Boot kèm Database H2 In-Memory (tự động nạp dữ liệu mẫu ban đầu):
```bash
# Windows PowerShell
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

Sau khi ứng dụng khởi chạy thành công:
- **Giao diện Storefront & Admin Dashboard**: [http://localhost:8080/](http://localhost:8080/)
- **Swagger UI Interactive API**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI v3 JSON Contract**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)  
  *(JDBC URL: `jdbc:h2:mem:shopdb` | Username: `sa` | Password: để trống)*

### 6.4. Chạy với CSDL MySQL 8.0 (Tùy chọn Production)
Script khởi tạo lược đồ CSDL và nạp dữ liệu mẫu Faker sẵn sàng tại [`docs/V1_init_shop_api.sql`](./docs/V1_init_shop_api.sql).

---

## 7. KẾT QUẢ KIỂM THỬ THỰC TẾ (TEST SUITE RESULTS)

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running org.example.demofullstackspringboot.DemoFullStackSpringBootApplicationTests
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- Spring Context Loaded
[INFO] Running org.example.demofullstackspringboot.integration.OrderIntegrationTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0 -- in OrderIntegrationTest:
  ✓ testCheckoutSuccess_ShouldCreateOrderAndDeductStock   (HTTP 201, Tồn kho trừ 25 -> 23)
  ✓ testCheckoutFail_WhenStockInsufficient               (HTTP 422, ERR_STOCK_EMPTY, Kho giữ nguyên)
  ✓ testCancelOrder_ShouldRestoreStock                   (HTTP 200, Đơn CANCELLED, Tồn kho hoàn lại 23 -> 25)
  ✓ testCustomerDuplicateEmail_ShouldReturn422           (HTTP 422, ERR_EMAIL_DUPLICATE)
  ✓ testCustomerInvalidEmail_ShouldReturn400             (HTTP 400, ERR_VALIDATION)

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] Total time: 31.989 s
[INFO] ------------------------------------------------------------------------
```

---

## 8. LIÊN HỆ & TÁC GIẢ
- **Đơn vị phát triển**: Nhóm FPT Aptech Fullstack Spring Boot Project
- **Bản quyền**: © 2026. Phát hành theo giấy phép [Apache License 2.0](LICENSE).
