# PROJECT BRIEF - SHOP E-COMMERCE FULLSTACK
> Single Source of Truth for AI Software Factory Context

## 1. TỔNG QUAN HỆ THỐNG
- **Tên dự án**: Shop E-Commerce System
- **Kiến trúc**: API-First, Micro-service ready Monolith, Monorepo.
- **Tech Stack**:
  - **Backend**: Java 21, Spring Boot 3.4+, Spring Data JPA, Hibernate, MySQL 8.0, Flyway.
  - **API Spec & Codegen**: OpenAPI 3.0.3, `openapi-generator-maven-plugin`.
  - **Frontend**: Single Page Application (React / JS / HTML5), Storefront & Admin Dashboard.
  - **Testing**: Postman / Newman (Contract Test), JUnit 5, `@SpringBootTest`, `MockMvc` (Backend Test), Playwright (E2E Test).

## 2. PHẠM VI NGHIỆP VỤ (CORE MODULES)
1. **Module Khách hàng (`/customers`)**:
   - Đăng ký/Đăng nhập, quản lý hồ sơ, lịch sử giao dịch đơn hàng.
   - Validation: Email định dạng chuẩn, SĐT 10-11 số.
2. **Module Danh mục & Sản phẩm (`/categories`, `/products`)**:
   - Quản lý danh mục hàng hóa, danh sách sản phẩm (giá, số lượng tồn kho `quantity`, mô tả, hình ảnh).
   - Truy vấn nâng cao: Phân trang `?page=0&size=10`, lọc theo danh mục, khoảng giá, từ khóa.
3. **Module Giỏ hàng & Đặt hàng (`/cart`, `/orders`)**:
   - Giỏ hàng động tại Frontend.
   - Checkout: Kiểm tra tồn kho nguyên tử -> Tạo đơn `PENDING` -> Trừ tồn kho trong CSDL.
   - Vòng đời trạng thái đơn hàng (Order State Transition):
     `PENDING` (Chờ xử lý) -> `PAID` (Đã thanh toán) -> `SHIPPED` (Đã giao hàng) HOẶC `CANCELLED` (Đã hủy).
   - **Tự động hoàn tồn kho**: Nếu đơn hàng chuyển sang `CANCELLED`, hệ thống tự động cộng lại số lượng sản phẩm vào kho.

## 3. CHUẨN RESTFUL & JPA OPTIMIZATION
- URL dùng **danh từ số nhiều**, định dạng `kebab-case`, tuyệt đối không chứa động từ trên đường dẫn.
- Trả về đúng HTTP Status Codes (`200`, `201`, `204`, `400`, `404`, `422`, `500`).
- Mọi thao tác đọc dữ liệu đánh dấu `@Transactional(readOnly = true)` để tắt Dirty Checking của Hibernate.
- Database Seeding với thư viện Faker tạo dữ liệu mẫu thực tế cho môi trường Dev/Staging.
