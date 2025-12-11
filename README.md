# Furniture E-commerce Backend System

[![Deploy on Railway](https://railway.app/button.svg)](https://furniture-backend-production-33d2.up.railway.app)
> **Live Demo API**: [https://furniture-backend-production-33d2.up.railway.app](https://furniture-backend-production-33d2.up.railway.app)

Chào mừng bạn đến với backend của hệ thống thương mại điện tử nội thất (Furniture E-commerce). Dự án này được xây dựng mạnh mẽ bằng **Java Spring Boot**, cung cấp đầy đủ các API RESTful và chức năng Real-time WebSocket để phục vụ cho một nền tảng mua sắm trực tuyến hiện đại, đa người dùng (Khách hàng, Người bán, Quản trị viên).

## 🚀 Tính năng nổi bật

### 👤 Quản lý người dùng & Phân quyền
*   **Xác thực bảo mật**: Đăng ký, Đăng nhập sử dụng JWT (JSON Web Token).
*   **Phân quyền (Role-based Authorization)**: Hỗ trợ 3 vai trò riêng biệt: `ROLE_CUSTOMER`, `ROLE_SELLER`, `ROLE_ADMIN`.
*   **Profile người dùng**: Quản lý thông tin cá nhân, địa chỉ giao hàng.

### 🛒 Mua sắm & Sản phẩm
*   **Quản lý sản phẩm**: CRUD sản phẩm, biến thể, hình ảnh (tích hợp Cloudinary).
*   **Tìm kiếm & Lọc**: Tìm kiếm sản phẩm theo tên, danh mục, giá cả.
*   **Giỏ hàng (Cart)**: Thêm, sửa, xóa sản phẩm, tính tổng tiền tự động.
*   **Yêu thích (Wishlist)**: Lưu sản phẩm quan tâm.
*   **Đánh giá (Review)**: Người dùng có thể đánh giá và bình luận sản phẩm đã mua.

### 📦 Đơn hàng & Thanh toán
*   **Quản lý đơn hàng**: Tạo đơn hàng, theo dõi trạng thái (Placed, Confirmed, Shipped, Delivered, Cancelled).
*   **Thanh toán đa dạng**:
    *   Thanh toán khi nhận hàng (COD).
    *   **Thanh toán online qua ví VNPay** (tích hợp Sandbox).
*   **Mã giảm giá (Coupon)**: Áp dụng mã giảm giá cho đơn hàng.

### 🏪 Dành cho Người bán (Seller) & Quản trị (Admin)
*   **Seller Dashboard**: Thống kê doanh thu, quản lý đơn hàng của shop, quản lý sản phẩm.
*   **Seller Report**: Báo cáo tình hình kinh doanh chi tiết.
*   **Admin Dashboard**: Quản lý toàn bộ hệ thống, người dùng, category, deal/khuyến mãi hệ thống.

### 💬 Hệ thống Chat Real-time
*   **WebSocket Integration**: Chat trực tiếp giữa Người mua và Người bán.
*   **Thông báo trạng thái**: Cập nhật trạng thái tin nhắn (đã gửi, đã xem) theo thời gian thực.
*   **Gửi hình ảnh/Sản phẩm**: Hỗ trợ gửi thông tin sản phẩm trong tin nhắn.

### 📧 Dịch vụ Email
*   Tự động gửi email xác nhận, thông báo đơn hàng qua **Gmail SMTP** hoặc **SendGrid**.

---

## 🛠 Công nghệ sử dụng

*   **Ngôn ngữ chính**: Java 21
*   **Framework**: Spring Boot 3.x (Spring Web, Spring Security, Spring Data JPA)
*   **Database**: MySQL (tương thích AWS RDS hoặc Local)
*   **Real-time Communication**: Spring WebSocket (STOMP protocol)
*   **Build Tool**: Maven
*   **Cloud Storage**: Cloudinary (lưu trữ ảnh)
*   **Payment**: VNPay API
*   **Deploy**: Docker, Railway (hoặc bất kỳ nền tảng hỗ trợ Java/Docker nào)

---

## ⚙️ Yêu cầu cài đặt

Trước khi bắt đầu, hãy đảm bảo máy của bạn đã cài đặt:

*   [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21)
*   [Maven](https://maven.apache.org/) (hoặc sử dụng `mvnw` có sẵn trong dự án)
*   [MySQL Server](https://dev.mysql.com/downloads/mysql/) (hoặc Docker container MySQL)
*   [Git](https://git-scm.com/)

---

## 📥 Hướng dẫn chạy dự án

### 1. Clone dự án

```bash
git clone https://github.com/ThanhNgo1007/furniture-backend.git
cd furniture-backend
```

### 2. Cấu hình cơ sở dữ liệu

Tạo một database trống trong MySQL:

```sql
CREATE DATABASE nl_ecommerce;
```

### 3. Cấu hình biến môi trường

Dự án sử dụng biến môi trường để bảo mật thông tin. Bạn có thể thiết lập trực tiếp trong hệ điều hành hoặc tạo file `.env` (nếu chạy bằng Docker) hoặc chỉnh sửa file `src/main/resources/application.properties` (cho dev local).

Các biến môi trường quan trọng:

| Biến (Variable) | Mô tả | Mặc định |
| :--- | :--- | :--- |
| `PORT` | Port chạy ứng dụng | `5454` |
| `DB_URL` | URL kết nối JDBC | `jdbc:mysql://localhost:3306/nl_ecommerce` |
| `DB_USERNAME` | Username MySQL | `root` |
| `DB_PASSWORD` | Password MySQL | _(trống)_ |
| `JWT_SECRET` | Secret key để kỹ JWT | _(Bắt buộc)_ |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary Cloud Name | `dtlxpw3eh` |
| `CLOUDINARY_API_KEY` | Cloudinary API Key | _(Bắt buộc)_ |
| `CLOUDINARY_API_SECRET` | Cloudinary API Secret | _(Bắt buộc)_ |
| `VNPAY_TMN_CODE` | Mã website VNPay (Terminal ID) | _(Bắt buộc)_ |
| `VNPAY_SECRET_KEY` | Secret Key VNPay (Checksum) | _(Bắt buộc)_ |
| `MAIL_USERNAME` | Email gửi thông báo (Gmail) | _(Tùy chọn)_ |
| `MAIL_PASSWORD` | App Password của Gmail | _(Tùy chọn)_ |

### 4. Build và Chạy ứng dụng

Sử dụng Maven wrapper có sẵn:

```bash
# Trên Linux/macOS
./mvnw spring-boot:run

# Trên Windows
mvnw.cmd spring-boot:run
```

Hoặc nếu bạn đã cài Maven global:

```bash
mvn spring-boot:run
```

Sau khi chạy thành công, API sẽ hoạt động tại: `http://localhost:5454`

---

## 🐳 Chạy bằng Docker

Dự án đã có sẵn `Dockerfile`. Bạn có thể build và chạy container dễ dàng.

```bash
# 1. Build image
docker build -t furniture-backend .

# 2. Run container (nhớ thay thế các biến môi trường thực tế)
docker run -p 5454:5454 \
  -e DB_URL=jdbc:mysql://host.docker.internal:3306/nl_ecommerce \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  -e JWT_SECRET=supersecretkey \
  furniture-backend
```

---

## 📚 API Documentation (Sơ lược)

Dưới đây là các nhóm API chính (Prefix `/api`):

*   **Auth**: `/auth/signup`, `/auth/signin`
*   **Products**: `/products`, `/products/id/{id}`, `/products/search`
*   **Users**: `/api/users/profile`
*   **Orders**: `/api/orders`, `/api/orders/{id}`
*   **Cart**: `/api/cart`, `/api/cart_items`
*   **Admin**: `/api/admin/products`, `/api/admin/orders`
*   **Seller**: `/api/seller/orders`, `/api/seller/products`
*   **Payments**: `/api/payment/{paymentMethod}/order/{orderId}`

---

## 🤝 Đóng góp (Contributing)

Mọi đóng góp đều được hoan nghênh! Nếu bạn muốn cải thiện dự án:
1.  Fork dự án
2.  Tạo branch tính năng mới (`git checkout -b feature/NewFeature`)
3.  Commit thay đổi (`git commit -m 'Add new feature'`)
4.  Push lên branch (`git push origin feature/NewFeature`)
5.  Tạo Pull Request

---

## 📄 License

Dự án này là mã nguồn mở và được sử dụng cho mục đích học tập/nghiên cứu.

---

**Phát triển bởi [Thanh Ngo](https://github.com/ThanhNgo1007)**
