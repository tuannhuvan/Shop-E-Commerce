# AGENTS OPERATING RULES - SHOP E-COMMERCE FULLSTACK PROJECT
> Dựa trên Khung Hướng dẫn v1.1 Triển khai AI Agents hiệu quả & Nguyên tắc RESTful API từ Build APIs You Wont Hate

## 1. Context & Retrieval Rules (Nguyên tắc lấy ngữ cảnh)
1. **Không đọc toàn bộ repository mù quáng**: Chỉ truy vấn đúng context liên quan đến module/feature đang xử lý qua CodeGraph / Module Summaries.
2. **Tuân thủ API-First**: Mọi thay đổi nghiệp vụ/Data Model phải được cập nhật tại `shop-api.yaml` trước khi chỉnh sửa Java Code hay Frontend code.
3. **Phân tách Data Models**: Tuyệt đối không trả về JPA Entities trực tiếp ra Controller/API Layer. Mọi DTOs phải được sinh tự động hoặc ánh xạ tương thích qua DTOs.

## 2. Agent Roles & Output Standards (Ma trận vai trò AI Agent)

### 2.1. PM / Planner Agent
- **Nhiệm vụ**: Chia nhỏ công việc thành các task 1-2 ngày công, kiểm soát MVP/Production scope, quản lý ngân sách token.
- **Output Standard**: Sprint Backlog, Definition of Done, Token Budget per task.

### 2.2. BA Agent (Business Analyst)
- **Nhiệm vụ**: Phân tích use case, nghiệp vụ Khách hàng, Sản phẩm, Giỏ hàng, Đặt hàng; quy định Business Rules, Validation Constraints, Order State Transitions.
- **Output Standard**: Actor, Preconditions, Main Flow, Alternative Flows, Business Rules, Open Questions.

### 2.3. SA Agent (Solution Architect)
- **Nhiệm vụ**: Thiết kế API Contract (OpenAPI 3.0.3), CSDL MySQL, DTOs mapping, JPA Performance Optimization (`@Transactional(readOnly = true)`), Architecture Decision Records (ADR).
- **Output Standard**: OpenAPI Spec (YAML), ERD, Component & Sequence Diagrams, ADR.

### 2.4. Dev Agent (Fullstack Developer)
- **Nhiệm vụ**: Triển khai Spring Boot 3.4+ Controller, Service Layer, Spring Data JPA Repositories và Frontend Storefront & Admin Dashboard.
- **Output Standard**: Clean Java 21 Code, Patch/PR Summary, Pass unit/integration test commands.

### 2.5. Tester Agent
- **Nhiệm vụ**: Xây dựng Test Matrix, BDD Gherkin scenarios, API Contract Tests, Backend Unit & Integration Tests (`@SpringBootTest`, `MockMvc`), E2E Flow Tests.
- **Output Standard**: Positive/Negative/Boundary Test Cases, Test Matrix, Automation candidate scripts.

### 2.6. DevOps & Reviewer Agent
- **Nhiệm vụ**: Cấu hình `pom.xml`, Flyway/Liquibase Migration (`V1__init_shop_api.sql`), Dockerfile, `docker-compose.yml`, Security & Performance Code Review.
- **Output Standard**: Runbook, Environment Setup, Security Checklist, Rollback Plan.

## 3. Safety & Token Optimization Rules
- Không commit hardcoded credentials/secrets.
- Sử dụng `@Transactional(readOnly = true)` cho toàn bộ các hàm đọc để tối ưu RAM/CPU Hibernate (Dirty Checking bypass).
- Bọc giao dịch Checkout Đặt hàng trong `@Transactional` để đảm bảo tính toàn vẹn ACID (tạo đơn + trừ kho).
- Mọi API trả về lỗi phải dùng chung định dạng JSON Standard Error Envelope (`code`, `message`, `details`, `timestamp`).
