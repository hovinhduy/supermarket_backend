# Supermarket Management System

Hệ thống quản lý siêu thị toàn diện được xây dựng bằng Spring Boot 3, hỗ trợ bán hàng tại quầy (POS), bán hàng trực tuyến, quản lý kho, khuyến mãi và tích hợp AI Chatbot.

## Công Nghệ Sử Dụng

| Công nghệ | Phiên bản | Mô tả |
|-----------|-----------|-------|
| Java | 17 | Ngôn ngữ lập trình chính |
| Spring Boot | 3.5.x | Framework backend |
| Spring Security | - | Xác thực và phân quyền (JWT) |
| Spring Data JPA | - | ORM và truy vấn database |
| MySQL | 8.x | Cơ sở dữ liệu |
| Spring AI | - | Tích hợp AI Chatbot (Google Gemini) |
| Springdoc OpenAPI | - | Tài liệu API (Swagger UI) |
| PayOS | - | Cổng thanh toán điện tử |
| AWS S3 SDK | - | Lưu trữ file (Cloudflare R2) |
| Apache POI | - | Xuất/nhập file Excel |
| iText | - | Tạo hóa đơn PDF |
| ZXing | - | Tạo mã vạch (Barcode) |
| Lombok | - | Giảm boilerplate code |

## Kiến Trúc Hệ Thống

```
iuh.fit.supermarket
├── controller/     # REST API endpoints
├── service/        # Business logic interfaces
│   └── impl/       # Business logic implementations
├── repository/     # Data access layer (JPA)
├── entity/         # Database entities
├── dto/            # Data Transfer Objects
├── config/         # Cấu hình ứng dụng
├── security/       # Cấu hình bảo mật & JWT
├── exception/      # Xử lý ngoại lệ toàn cục
└── util/           # Các tiện ích
```

## Các Phân Hệ Chức Năng

### 1. Quản Lý Sản Phẩm
- Quản lý sản phẩm, danh mục, thương hiệu
- Quản lý đơn vị tính và hình ảnh sản phẩm
- Tạo và quản lý mã vạch

### 2. Bán Hàng (POS & Online)
- Bán hàng tại quầy (Point of Sale)
- Đặt hàng trực tuyến với giỏ hàng
- Thanh toán qua PayOS
- Xử lý trả hàng và hoàn tiền

### 3. Quản Lý Kho
- Quản lý kho hàng và tồn kho
- Nhập hàng từ nhà cung cấp
- Kiểm kê kho định kỳ

### 4. Quản Lý Khách Hàng & Nhân Viên
- Thông tin khách hàng và sổ địa chỉ
- Danh sách sản phẩm yêu thích
- Quản lý nhân viên và phân quyền

### 5. Khuyến Mãi
- Chương trình giảm giá theo phần trăm/số tiền
- Khuyến mãi Mua X Tặng Y
- Kiểm tra điều kiện áp dụng tự động

### 6. Báo Cáo & Thống Kê
- Dashboard tổng quan kinh doanh
- Báo cáo doanh thu, tồn kho
- Xuất báo cáo Excel/PDF

### 7. AI Chatbot
- Hỗ trợ khách hàng tự động
- Tích hợp Google Gemini AI

## Yêu Cầu Hệ Thống

- JDK 17 trở lên
- MySQL 8.x
- Maven 3.8+

## Cấu Hình Biến Môi Trường

Tạo các biến môi trường sau trước khi chạy ứng dụng:

```bash
# Database
DBMS_CONNECTION=jdbc:mysql://localhost:3306/supermarket
DBMS_USERNAME=root
DBMS_PASSWORD=your_password

# AI (Google Gemini)
GEMINI_KEY=your_gemini_api_key

# Storage (Cloudflare R2 / AWS S3)
ACCESS_KEY_ID=your_access_key
SECRET_ACCESS_KEY=your_secret_key
BUCKET_NAME=your_bucket_name
R2_API_ENDPOINT=your_r2_endpoint
R2_PUBLIC_URL=your_public_url

# Payment (PayOS)
PAYOS_CLIENT_ID=your_client_id
PAYOS_API_KEY=your_api_key
PAYOS_CHECKSUM_KEY=your_checksum_key
PAYOS_RETURN_URL=http://localhost:3000/payment/success
PAYOS_CANCEL_URL=http://localhost:3000/payment/cancel
```

## Cài Đặt & Chạy Ứng Dụng

### 1. Clone dự án

```bash
git clone <repository-url>
cd supermarket
```

### 2. Cấu hình database

Tạo database MySQL:

```sql
CREATE DATABASE supermarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. Build và chạy

**Windows:**
```bash
mvnw.cmd clean install -DskipTests
mvnw.cmd spring-boot:run
```

**Linux/macOS:**
```bash
./mvnw clean install -DskipTests
./mvnw spring-boot:run
```

### 4. Truy cập ứng dụng

- API Base URL: `http://localhost:8080/api`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## API Endpoints Chính

| Module | Endpoint | Mô tả |
|--------|----------|-------|
| Auth | `/api/auth/**` | Đăng nhập, đăng ký, refresh token |
| Product | `/api/products/**` | CRUD sản phẩm |
| Category | `/api/categories/**` | CRUD danh mục |
| Order | `/api/orders/**` | Quản lý đơn hàng online |
| Sale | `/api/sale/**` | Bán hàng tại quầy (POS) |
| Customer | `/api/customers/**` | Quản lý khách hàng |
| Employee | `/api/employees/**` | Quản lý nhân viên |
| Promotion | `/api/promotions/**` | Quản lý khuyến mãi |
| Warehouse | `/api/warehouses/**` | Quản lý kho |
| Report | `/api/reports/**` | Báo cáo thống kê |
| Chat | `/api/chat/**` | AI Chatbot |

## Cấu Trúc Response API

```json
{
  "success": true,
  "data": { ... },
  "message": "Thông báo kết quả",
  "timestamp": "2025-01-01T00:00:00Z"
}
```

## Testing

```bash
# Chạy tất cả tests
mvnw.cmd test

# Chạy test cho module cụ thể
mvnw.cmd test -Dtest=ProductServiceTest
```

## Đóng Góp

1. Fork repository
2. Tạo feature branch (`git checkout -b feature/TinhNangMoi`)
3. Commit thay đổi (`git commit -m 'Thêm tính năng mới'`)
4. Push lên branch (`git push origin feature/TinhNangMoi`)
5. Tạo Pull Request

## License

Dự án này được phát triển cho mục đích học tập tại IUH (Đại học Công nghiệp TP.HCM).

---

**Tác giả:** Hồ Vĩnh Duy
**Năm:** 2025
