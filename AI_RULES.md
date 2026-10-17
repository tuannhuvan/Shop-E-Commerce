# AI_RULES.md - QUY TẮC BẮT BUỘC KHI TRIỂN KHAI AI AGENTS
> Dự án: Shop E-Commerce Fullstack (Spring Boot 3.4+ & OpenAPI 3.0.3 & React)

## 1. CẤU TRÚC VÙNG LÀM VIỆC AI (AI WORKSPACE LAYOUT)
Hệ thống tuân thủ cấu trúc thư mục tiêu chuẩn từ Khung triển khai AI Software Factory v1.1:
```
project-root/
├── AGENTS.md                   # Rules & Roles cho các AI Agent
├── AI_RULES.md                 # Quy tắc mã nguồn, an toàn & tối ưu token
├── shop-api.yaml               # API Contract Single Source of Truth
├── .ai/
│   ├── 00_project_brief.md     # Tổng quan dự án & Tech Stack
│   ├── 01_repo_inventory.md    # Sơ đồ mã nguồn & các module
│   ├── 02_architecture_map.md  # Sơ đồ C4 Component & ERD
│   ├── 06_use_cases/           # Catalog Use Cases & Business Rules
│   └── 07_test_matrix/         # Test Matrix cho Contract, Backend & E2E
└── docs/
    ├── BA/                     # Đặc tả nghiệp vụ BA
    ├── SA/                     # Thiết kế kiến trúc SA & ADR
    ├── TEST/                   # Kịch bản kiểm thử Postman/JUnit
    └── DEVOPS/                 # Runbook & Script SQL Migration
```

## 2. QUY TẮC TỐI ƯU TOKEN & CHỌN MODEL (TOKEN OPTIMIZATION)
1. **Chia tầng Model (Model Routing)**:
   - **Local / Lightweight Model**: Tóm tắt file, sinh checklist, phân loại endpoint, draft testcase.
   - **Strong Cloud Model**: Phân tích kiến trúc SA, refactor giao dịch Checkout, Security Review, viết hợp đồng OpenAPI.
2. **Nguyên tắc "Graph First, File Second"**:
   - Sử dụng CodeGraph / Symbol search để tìm đúng class/interface trước khi đọc file mã nguồn.
   - Chỉ đọc các file liên quan trực tiếp đến task (Controller, Service, Repository, DTO).

## 3. PROMPT CONTRACT CHUẨN CHO MỌI AGENT
Mọi câu lệnh gửi đến AI Agent phải đáp ứng đủ 6 thành tố:
1. **Role**: Vai trò cụ thể (Senior BA, SA, Spring Boot Dev, Tester, DevOps).
2. **Task**: Yêu cầu công việc cần hoàn thành.
3. **Context**: Dữ liệu đầu vào (`shop-api.yaml`, schema DB, log lỗi).
4. **Constraints**: Các ràng buộc (Không trả về Entity, bắt buộc `@Transactional(readOnly=true)`).
5. **Output Format**: Định dạng đầu ra mong muốn (Markdown, YAML, Java, SQL).
6. **Validation Checklist**: Tiêu chí kiểm tra tính đúng đắn trước khi bàn giao.

## 4. CHÍNH SÁCH AN TOÀN & CODE QUALITY (SAFETY & QUALITY)
- **Cơ chế Duyệt Mã**: Mọi code do AI sinh ra phải qua PR Review và chạy thử Unit Test thành công.
- **Không Hardcode**: Tuyệt đối không lưu mật khẩu, API keys hay secrets trong mã nguồn.
- **Fail-Fast Validation**: Sử dụng Spring Validation (`@Valid`, `@NotNull`, `@Min`) ngay tại Controller layer.
- **Atomicity**: Thao tác đặt hàng phải đảm bảo nguyên tố (ACID), trừ kho và tạo đơn hàng đồng thời trong 1 transaction.
