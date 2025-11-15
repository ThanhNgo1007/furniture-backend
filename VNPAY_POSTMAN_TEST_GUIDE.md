# Hướng Dẫn Test VNPay Service trên Postman

## Thông tin cơ bản
- **Base URL**: `http://localhost:5454`
- **Port**: `5454`
- **JWT Format**: `Bearer <token>`

---

## Bước 1: Đăng nhập để lấy JWT Token

### Endpoint: `POST /auth/signing`

**Headers:**
```
Content-Type: application/json
```

**Body (JSON):**
```json
{
  "email": "your-email@example.com",
  "otp": "123456"
}
```

**Response:**
```json
{
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "message": "Login success",
  "role": "ROLE_USER"
}
```

**Lưu ý**: 
- Trước khi đăng nhập, cần gửi OTP qua endpoint `/auth/sent/login-signup-otp`
- Copy JWT token từ response để dùng cho các request sau

---

## Bước 2: Thêm sản phẩm vào giỏ hàng (nếu chưa có)

### Endpoint: `PUT /api/cart/add`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your-jwt-token>
```

**Body (JSON):**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response:**
```json
{
  "id": 1,
  "product": { ... },
  "quantity": 2,
  "sellingPrice": 200000,
  "msrpPrice": 250000
}
```

---

## Bước 3: Kiểm tra giỏ hàng (tùy chọn)

### Endpoint: `GET /api/cart`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
{
  "id": 1,
  "user": { ... },
  "cartItems": [
    {
      "id": 1,
      "product": { ... },
      "quantity": 2,
      "sellingPrice": 200000
    }
  ]
}
```

---

## Bước 4: Tạo đơn hàng và lấy VNPay Payment Link

### Endpoint: `POST /api/orders?paymentMethod=VNPAY`

**Headers:**
```
Content-Type: application/json
Authorization: Bearer <your-jwt-token>
```

**Query Parameters:**
- `paymentMethod`: `VNPAY` (bắt buộc)

**Body (JSON) - Address:**
```json
{
  "name": "Nguyễn Văn A",
  "locality": "Phường 1",
  "address": "123 Đường ABC",
  "city": "Cần Thơ",
  "ward": "Ninh Kiều",
  "pinCode": "94000",
  "mobile": "0123456789"
}
```

**Response (Thành công):**
```json
{
  "payment_link_url": "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=20000000&vnp_Command=pay&vnp_CreateDate=20240101120000&vnp_CurrCode=VND&vnp_ExpireDate=20240101121500&vnp_IpAddr=127.0.0.1&vnp_Locale=vn&vnp_OrderInfo=Thanh+toan+don+hang%3A1&vnp_OrderType=other&vnp_ReturnUrl=http%3A%2F%2Flocalhost%3A5454%2Fapi%2Fpayment%2Fvnpay%2Freturn&vnp_TmnCode=YOUR_TMN_CODE&vnp_TxnRef=1234567890&vnp_Version=2.1.0&vnp_SecureHash=abc123...",
  "payment_link_id": "1234567890"
}
```

**Lưu ý quan trọng:**
- `payment_link_url`: URL thanh toán VNPay, copy và mở trong browser để test
- `payment_link_id`: Mã giao dịch (vnp_TxnRef), dùng để kiểm tra sau

---

## Bước 5: Test thanh toán trên VNPay Sandbox

1. **Copy `payment_link_url`** từ response ở Bước 4
2. **Mở URL trong browser** (Chrome, Firefox, etc.)
3. **Điền thông tin thẻ test VNPay Sandbox:**
   - **Ngân hàng**: NCB
   - **Số thẻ**: `9704198526191432198`
   - **Tên chủ thẻ**: `NGUYEN VAN A`
   - **Ngày phát hành**: `07/15`
   - **OTP**: `123456`
4. **Hoàn tất thanh toán**
5. **VNPay sẽ redirect về**: `http://localhost:5454/api/payment/vnpay/return?...`

---

## Bước 6: Test Return URL (Sau khi thanh toán)

### Endpoint: `GET /api/payment/vnpay/return`

**Lưu ý**: Endpoint này được VNPay gọi tự động sau khi thanh toán, nhưng bạn có thể test thủ công.

**Query Parameters** (VNPay sẽ gửi kèm):
```
?vnp_Amount=20000000
&vnp_BankCode=NCB
&vnp_BankTranNo=VNP1234567890
&vnp_CardType=ATM
&vnp_OrderInfo=Thanh+toan+don+hang%3A1
&vnp_PayDate=20240101120000
&vnp_ResponseCode=00
&vnp_TmnCode=YOUR_TMN_CODE
&vnp_TransactionNo=1234567890
&vnp_TransactionStatus=00
&vnp_TxnRef=1234567890
&vnp_SecureHash=abc123...
```

**Response:**
- **Status Code**: `302 Found`
- **Location Header**: `http://localhost:3000/payment/result?success=true&vnp_TxnRef=1234567890`

**Test trên Postman:**
1. Tạo request mới: `GET http://localhost:5454/api/payment/vnpay/return`
2. Thêm các query parameters từ VNPay (hoặc tự tạo với checksum hợp lệ)
3. Gửi request
4. Kiểm tra response có redirect về frontend URL không

---

## Bước 7: Kiểm tra trạng thái đơn hàng (Tùy chọn)

### Endpoint: `GET /api/orders/user`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Response:**
```json
[
  {
    "id": 1,
    "orderId": "ORD-123",
    "orderStatus": "PLACED",
    "paymentStatus": "COMPLETED",
    "totalSellingPrice": 200000,
    "paymentDetails": {
      "paymentId": "1234567890",
      "status": "COMPLETED"
    }
  }
]
```

---

## Bước 8: Test Endpoint Xác nhận Thanh toán (Tùy chọn)

### Endpoint: `GET /api/payment/{paymentId}`

**Headers:**
```
Authorization: Bearer <your-jwt-token>
```

**Path Parameters:**
- `paymentId`: Mã giao dịch VNPay (vnp_TransactionNo)

**Query Parameters:**
- `paymentLinkId`: Mã giao dịch (vnp_TxnRef)
- `vnp_ResponseCode`: `00` (tùy chọn, mặc định `00`)
- `vnp_TransactionStatus`: `00` (tùy chọn, mặc định `00`)

**Example:**
```
GET http://localhost:5454/api/payment/1234567890?paymentLinkId=1234567890&vnp_ResponseCode=00&vnp_TransactionStatus=00
```

**Response:**
```json
{
  "message": "Payment successful"
}
```

---

## Collection Postman Mẫu

### 1. Login
```
POST http://localhost:5454/auth/signing
Body: { "email": "...", "otp": "..." }
```

### 2. Add to Cart
```
PUT http://localhost:5454/api/cart/add
Headers: Authorization: Bearer {{jwt_token}}
Body: { "productId": 1, "quantity": 2 }
```

### 3. Create Order with VNPay
```
POST http://localhost:5454/api/orders?paymentMethod=VNPAY
Headers: 
  Authorization: Bearer {{jwt_token}}
  Content-Type: application/json
Body: {
  "name": "Nguyễn Văn A",
  "locality": "Phường 1",
  "address": "123 Đường ABC",
  "city": "Cần Thơ",
  "ward": "Ninh Kiều",
  "pinCode": "94000",
  "mobile": "0123456789"
}
```

### 4. Test Return URL
```
GET http://localhost:5454/api/payment/vnpay/return
Query Params: (từ VNPay hoặc tự tạo)
```

---

## Lưu ý quan trọng

1. **JWT Token**: Phải có format `Bearer <token>`, không có dấu ngoặc kép
2. **VNPay Config**: Đảm bảo đã cấu hình `vnpay.tmn.code` và `vnpay.secret.key` trong `application.properties`
3. **Return URL**: Phải là `http://localhost:5454/api/payment/vnpay/return` (không phải HTTPS)
4. **Checksum**: VNPay sử dụng HMAC SHA512 để verify, đảm bảo secret key đúng
5. **Test Card**: Chỉ dùng thẻ test trong môi trường Sandbox
6. **Frontend URL**: Return URL sẽ redirect về `http://localhost:3000/payment/result` (có thể thay đổi trong code)

---

## Troubleshooting

### Lỗi: "Invalid Checksum"
- Kiểm tra `vnpay.secret.key` trong `application.properties` có đúng không
- Đảm bảo secret key từ email VNPay được copy đầy đủ

### Lỗi: "Order not found"
- Kiểm tra `paymentLinkId` (vnp_TxnRef) có đúng không
- Đảm bảo đơn hàng đã được tạo trước khi thanh toán

### Lỗi: "401 Unauthorized"
- Kiểm tra JWT token có hợp lệ không
- Đảm bảo format header: `Authorization: Bearer <token>`

### Return URL không hoạt động
- Kiểm tra server có đang chạy trên port 5454 không
- Kiểm tra VNPay có thể truy cập được localhost không (thường không được, cần dùng ngrok)

---

## Test với ngrok (Nếu cần)

Nếu muốn test Return URL từ internet (VNPay có thể gọi về):

1. Cài đặt ngrok: `brew install ngrok` (Mac) hoặc download từ ngrok.com
2. Chạy ngrok: `ngrok http 5454`
3. Copy URL public (ví dụ: `https://abc123.ngrok.io`)
4. Cập nhật `vnpay.return.url` trong `application.properties`:
   ```
   vnpay.return.url=https://abc123.ngrok.io/api/payment/vnpay/return
   ```
5. Restart server và test lại

---

## Kết luận

Với hướng dẫn này, bạn có thể test đầy đủ flow thanh toán VNPay trên Postman. Flow chính:
1. Login → Lấy JWT
2. Add to Cart → Thêm sản phẩm
3. Create Order → Lấy payment URL
4. Thanh toán trên VNPay Sandbox
5. Kiểm tra Return URL và trạng thái đơn hàng

