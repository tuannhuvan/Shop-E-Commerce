# TÀI LIỆU ĐẶC TẢ YÊU CẦU NGHIỆP VỤ VÀ HƯỚNG DẪN TRIỂN KHAI HỆ THỐNG FULLSTACK SHOP E-COMMERCE

**Dự án**: Bài Tập Lớn Fullstack Shop E-Commerce (Spring Boot 3.4+ & OpenAPI 3.0.3 & React)  
**Tài liệu căn cứ**: `14_10_2026_Đề-bài-mở-rộng-fullstack-spring-boot.pdf`, `Triển khai AI Agents hiệu quả.pdf`, `build-apis-you-wont-hate.pdf`  
**Vai trò lập**: Senior Business Analyst & Solution Architect  
**Phiên bản**: 2.0.0 (Mở rộng Fullstack API-First & AI Software Factory)  

---

## 1. TỔNG QUAN DỰ ÁN & MỤC TIÊU FULLSTACK

### 1.1 Mục tiêu đồ án
- **Phân tích BA & Quy trình Nghiệp vụ**: Xác định đầy đủ Luồng nghiệp vụ (Business Workflow) và User Stories cho 3 module chính (Khách hàng, Sản phẩm & Danh mục, Giỏ hàng & Đặt hàng).
- **Thiết kế API-First & Contract Test**: Soạn thảo hợp đồng OpenAPI 3.0.3 (`shop-api.yaml`), khởi chạy Mock Server (Swagger Editor / Postman Mock Server / Prism) kết nối UI Prototype và thực thi kịch bản API Contract Test tự động.
- **Phát triển Backend (Spring Boot 3.4+ & Java 21)**: Sinh mã Server Stub tự động (`openapi-generator-maven-plugin`), triển khai logic nghiệp vụ với `@Transactional` (tối ưu hóa readOnly=true cho thao tác đọc) và Database Seeding dữ liệu mẫu với thư viện Faker.
- **Phát triển Frontend (Storefront & Admin Dashboard)**: Xây dựng ứng dụng Single Page Application (React / JS / HTML5) kết nối linh hoạt giữa Mock Server (Giai đoạn 1) và Spring Boot Backend (Giai đoạn 2).
- **Kiểm thử Hệ thống Toàn diện**: Thực thi 3 cấp độ kiểm thử: Contract Test (Postman/Newman), Backend Unit & Integration Test (`@SpringBootTest`, `MockMvc`), và E2E Flow Test.

### 1.2 Phạm vi Module Chức năng
1. **Module Khách hàng (`/customers`)**: Đăng ký, đăng nhập, quản lý hồ sơ cá nhân và xem lịch sử đơn hàng.
2. **Module Danh mục & Sản phẩm (`/categories`, `/products`)**: Quản lý danh mục hàng hóa, danh sách sản phẩm (giá, hình ảnh, mô tả, số lượng tồn kho `quantity`). Hỗ trợ tìm kiếm, lọc theo khoảng giá và phân trang.
3. **Module Giỏ hàng & Đặt hàng (`/cart`, `/orders`)**: Quản lý giỏ hàng động, checkout nguyên tử (kiểm tra tồn kho -> tính tổng tiền -> tạo đơn `PENDING` -> trừ tồn kho trong CSDL).
4. **Quản trị Đơn hàng & Vòng đời Trạng thái (Admin Dashboard)**: Chuyển đổi trạng thái đơn hàng: `PENDING` -> `PAID` -> `SHIPPED` hoặc `CANCELLED`. Nếu hủy đơn, tự động hoàn trả số lượng tồn kho sản phẩm.

---

## 2. PHÂN TÍCH NGHIỆP VỤ BÀI TOÁN & USER STORIES (BA SPECIFICATION)

### 2.1 User Stories Theo Vai Trò

#### A. Khách hàng (Customer Storefront)
- **US-CUST-01**: Là khách hàng, tôi muốn đăng ký/đăng nhập tài khoản bằng Email và SĐT hợp lệ để bắt đầu mua sắm.
- **US-CUST-02**: Là khách hàng, tôi muốn xem danh sách sản phẩm có phân trang, lọc theo danh mục và tìm kiếm từ khóa để dễ dàng lựa chọn mặt hàng ưng ý.
- **US-CUST-03**: Là khách hàng, tôi muốn xem chi tiết sản phẩm (hình ảnh, giá bán, mô tả, số lượng còn trong kho) để quyết định mua hàng.
- **US-CUST-04**: Là khách hàng, tôi muốn thêm/sửa/xóa sản phẩm trong Giỏ hàng động và thực hiện Đặt hàng (Checkout).
- **US-CUST-05**: Là khách hàng, tôi muốn xem lịch sử các đơn hàng đã đặt và có thể bấm Hủy đơn hàng khi đơn còn ở trạng thái `PENDING`.

#### B. Quản trị viên (Admin Dashboard)
- **US-ADM-01**: Là Admin, tôi muốn quản lý danh mục và thêm/sửa/xóa sản phẩm cùng số lượng tồn kho (`quantity`).
- **US-ADM-02**: Là Admin, tôi muốn xem toàn bộ danh sách đơn hàng của hệ thống và lọc theo trạng thái (`PENDING`, `PAID`, `SHIPPED`, `CANCELLED`).
- **US-ADM-03**: Là Admin, tôi muốn cập nhật trạng thái đơn hàng (Duyệt thanh toán `PAID`, Giao hàng `SHIPPED`).

---

### 2.2 Sơ Đồ Luồng Nghiệp Vụ Đặt Hàng & Hủy Đơn (Business Workflow)

```
[Khách hàng chọn SP vào Giỏ hàng]
                 │
                 ▼
     [Bấm Checkout Đặt hàng]
                 │
                 ▼
    <Kiểm tra Tồn kho (Stock)?> ────(Không đủ)────► [Báo lỗi ERR_STOCK_EMPTY - Status 422]
                 │
              (Đủ hàng)
                 │
                 ▼
  [Tạo Đơn hàng trạng thái PENDING]
                 │
                 ▼
 [Cập nhật trừ Tồn kho sản phẩm (ACID)]
                 │
                 ▼
  [Trả về HTTP 201 Created + Location]
                 │
                 ├─────────────────────────────────────────────────┐
                 │                                                 │
                 ▼                                                 ▼
   [Admin duyệt Thanh toán -> PAID]                   [Khách hàng / Admin bấm Hủy đơn]
                 │                                                 │
                 ▼                                                 ▼
     [Admin Giao hàng -> SHIPPED]                  [Đổi trạng thái đơn sang CANCELLED]
                                                                   │
                                                                   ▼
                                                 [Tự động CỘNG HOÀN LẠI Tồn kho CSDL]
```

---

## 3. THIẾT KẾ RESTFUL API CONTRACT & QUY TRÌNH API-FIRST

### 3.1 Quy chuẩn Thiết kế API (Phil Sturgeon Best Practices)
- **URL Structure**: Danh từ số nhiều (`/api/v1/customers`, `/api/v1/products`, `/api/v1/orders`), tuyệt đối không chứa động từ trên đường dẫn URL.
- **HTTP Verbs**: `GET` (Đọc), `POST` (Tạo mới -> `201 Created` + Header `Location`), `PUT`/`PATCH` (Cập nhật), `DELETE` (Xóa -> `204 No Content`).
- **Phân trang (Pagination Envelope)**: Query params `?page=0&size=10&sort=price,desc`, trả về JSON bọc `data` và metadata `pagination`.
- **Chuẩn hóa Phản hồi Lỗi**: Mọi API lỗi trả về cấu trúc bọc chuẩn `{"code": "ERR_xxx", "message": "...", "details": {...}, "timestamp": "..."}`.

### 3.2 Bảng Tổng Hợp Endpoints API Contract (`shop-api.yaml`)

| Resource | Method | Endpoint Path | Chức năng Nghiệp vụ | Status Code |
|---|---|---|---|---|
| **Customers** | `GET` | `/api/v1/customers` | Danh sách khách hàng (Phân trang) | `200 OK` |
| **Customers** | `POST` | `/api/v1/customers` | Đăng ký khách hàng mới | `201 Created` |
| **Customers** | `GET` | `/api/v1/customers/{id}` | Chi tiết khách hàng | `200 OK` / `404` |
| **Customers** | `GET` | `/api/v1/customers/{id}/orders` | Xem lịch sử đơn hàng của khách hàng | `200 OK` |
| **Categories**| `GET` | `/api/v1/categories` | Danh sách danh mục sản phẩm | `200 OK` |
| **Categories**| `POST`| `/api/v1/categories` | Tạo mới danh mục (Admin) | `201 Created` |
| **Products**  | `GET` | `/api/v1/products` | Lọc sản phẩm (danh mục, khoảng giá, từ khóa) | `200 OK` |
| **Products**  | `POST`| `/api/v1/products` | Thêm sản phẩm mới (Admin) | `201 Created` |
| **Products**  | `PUT` | `/api/v1/products/{id}` | Cập nhật thông tin/tồn kho sản phẩm | `200 OK` |
| **Products**  | `DELETE`| `/api/v1/products/{id}` | Xóa sản phẩm | `204 No Content` |
| **Orders**    | `GET` | `/api/v1/orders` | Danh sách đơn hàng (Admin Dashboard filter status) | `200 OK` |
| **Orders**    | `POST`| `/api/v1/orders` | Đặt hàng (Kiểm tra kho -> Tạo đơn -> Trừ kho) | `201 Created` / `422` |
| **Orders**    | `PATCH`| `/api/v1/orders/{id}/status` | Đổi trạng thái đơn (`PENDING` -> `PAID` -> `SHIPPED`) | `200 OK` |
| **Orders**    | `POST`| `/api/v1/orders/{id}/cancel` | Hủy đơn hàng và TỰ ĐỘNG HOÀN TỒN KHO | `200 OK` |

---

## 4. HƯỚNG DẪN TRIỂN KHAI BACKEND SPRING BOOT 3.X & TOÁN TỐI ƯU JPA

### 4.1 Maven OpenAPI Codegen Configuration (`pom.xml`)
Sử dụng plugin `openapi-generator-maven-plugin` để tự động sinh Server Interfaces và DTOs vào `target/generated-sources`:
```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <version>7.4.0</version>
    <executions>
        <execution>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/shop-api.yaml</inputSpec>
                <generatorName>spring</generatorName>
                <apiPackage>com.shopecommerce.api</apiPackage>
                <modelPackage>com.shopecommerce.model</modelPackage>
                <configOptions>
                    <interfaceOnly>true</interfaceOnly>
                    <useSpringBoot3>true</useSpringBoot3>
                    <useJakartaEe>true</useJakartaEe>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### 4.2 Tối ưu hóa Giao dịch JPA (`@Transactional`)
1. **Read-Only Operations**: Đánh dấu `@Transactional(readOnly = true)` trên tất cả các phương thức Service truy vấn (`GET`). Giúp Hibernate tắt Dirty Checking, giảm tải CPU và tối ưu RAM bộ nhớ đệm.
2. **Transactional Checkout & Cancel Order**:
   ```java
   @Service
   @RequiredArgsConstructor
   public class OrderServiceImpl implements OrderService {

       private final ProductRepository productRepository;
       private final OrderRepository orderRepository;

       @Override
       @Transactional // Atomic Transaction
       public OrderResponse createOrder(OrderCreateRequest request) {
           // 1. Kiểm tra tồn kho Fail-Fast
           for (OrderItemRequest item : request.getItems()) {
               Product product = productRepository.findById(item.getProductId())
                   .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tồn tại"));
               if (product.getQuantity() < item.getQuantity()) {
                   throw new StockEmptyException("Sản phẩm " + product.getName() + " không đủ tồn kho");
               }
               // Trừ tồn kho nguyên tử
               product.setQuantity(product.getQuantity() - item.getQuantity());
           }
           // 2. Lưu Đơn hàng trạng thái PENDING
           Order order = new Order();
           order.setStatus(OrderStatus.PENDING);
           // ...
           return orderMapper.toResponse(orderRepository.save(order));
       }

       @Override
       @Transactional
       public OrderResponse cancelOrder(String orderId) {
           Order order = orderRepository.findById(orderId)
               .orElseThrow(() -> new ResourceNotFoundException("Đơn hàng không tồn tại"));
           if (order.getStatus() == OrderStatus.CANCELLED) {
               throw new BadRequestException("Đơn hàng đã bị hủy trước đó");
           }
           // Tự động CỘNG HOÀN TRẢ tồn kho sản phẩm
           for (OrderItem item : order.getItems()) {
               Product product = item.getProduct();
               product.setQuantity(product.getQuantity() + item.getQuantity());
           }
           order.setStatus(OrderStatus.CANCELLED);
           return orderMapper.toResponse(orderRepository.save(order));
       }
   }
   ```

---

## 5. BỘ TÀI LIỆU VÀ CÁC FILE CẤU HÌNH ĐÃ TRIỂN KHAI

Để triển khai hiệu quả hệ thống kết hợp mô hình AI Software Factory, các tệp sau đây đã được khởi tạo sẵn sàng:

1. **`shop-api.yaml`**: API Contract Single Source of Truth chuẩn OpenAPI 3.0.3 cho tất cả Endpoints.
2. **`V1__init_shop_api.sql`**: Database Schema MySQL & Seed Data giả lập chuẩn Faker.
3. **`AGENTS.md`**: Quy định vai trò và tiêu chuẩn đầu ra cho 6 AI Agent (PM, BA, SA, Dev, Tester, DevOps).
4. **`AI_RULES.md`**: Quy tắc bắt buộc khi dùng AI Agent (Tối ưu Token, Code Security, Prompt Contract).
5. **`project_brief.md`**: Bản tóm tắt dự án làm ngữ cảnh nền cho AI Agent.
6. **`test_plan_and_matrix.md`**: Kế hoạch kiểm thử & Ma trận Test Cases 3 cấp độ (Contract, Backend Unit/Integration, E2E).

---

## 6. RUBRIC ĐÁNH GIÁ ĐỒ ÁN (THANG ĐIỂM 10.0)

| STT | Tiêu chí đánh giá | Nội dung yêu cầu chi tiết | Điểm |
|:---:|:---|:---|:---:|
| **1** | **Tài liệu BA & Flow** | Đầy đủ User Stories, sơ đồ luồng nghiệp vụ Đặt hàng, Hủy đơn, hoàn tồn kho và quy tắc chuyển đổi trạng thái. | **1.5** |
| **2** | **Quy trình API-First** | File `shop-api.yaml` chuẩn OpenAPI 3.0, chạy Mock Server kết nối UI prototype, chạy API Contract Test thành công. | **2.5** |
| **3** | **Backend & Tối ưu JPA** | Maven Codegen sinh code, áp dụng `@Transactional(readOnly=true)` cho thao tác đọc, xử lý giao dịch Đặt hàng & Hủy đơn an toàn, Seeding dữ liệu bằng Faker. | **2.5** |
| **4** | **Frontend UI/UX** | Giao diện Storefront & Admin Dashboard hoạt động mượt mà, kết nối thành công với Spring Boot Backend. | **2.0** |
| **5** | **Testing & Nộp bài** | Đầy đủ Unit Test / Integration Test Backend, E2E Test và báo cáo thử nghiệm. | **1.5** |
