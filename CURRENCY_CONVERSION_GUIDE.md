# Hướng Dẫn Xử Lý Currency (USD/VND) với VNPay

## Vấn đề

**VNPay chỉ hỗ trợ VND**, không hỗ trợ USD hay các loại tiền tệ khác. Nếu website của bạn sử dụng USD, bạn cần convert sang VND trước khi gửi đến VNPay.

## Giải pháp đã triển khai

Code đã được cập nhật để **tự động convert USD sang VND** khi tạo payment link với VNPay.

---

## Cấu hình

### 1. Cấu hình trong `application.properties`

```properties
# Currency Configuration
app.currency.default=USD
app.currency.exchange.rate.usd.to.vnd=25000
```

**Giải thích:**
- `app.currency.default`: Currency mặc định của website (`USD` hoặc `VND`)
- `app.currency.exchange.rate.usd.to.vnd`: Tỷ giá quy đổi 1 USD = ? VND

**Ví dụ:**
- Nếu 1 USD = 25,000 VND → `app.currency.exchange.rate.usd.to.vnd=25000`
- Nếu 1 USD = 24,500 VND → `app.currency.exchange.rate.usd.to.vnd=24500`

### 2. Các trường hợp sử dụng

#### Trường hợp 1: Website dùng USD
```properties
app.currency.default=USD
app.currency.exchange.rate.usd.to.vnd=25000
```

**Flow:**
1. User đặt hàng với giá **$100 USD**
2. System tự động convert: `100 USD × 25,000 = 2,500,000 VND`
3. Gửi `2,500,000 VND` đến VNPay
4. User thanh toán `2,500,000 VND` trên VNPay
5. Database vẫn lưu `$100 USD` (original amount)

#### Trường hợp 2: Website dùng VND
```properties
app.currency.default=VND
app.currency.exchange.rate.usd.to.vnd=25000
```

**Flow:**
1. User đặt hàng với giá **2,500,000 VND**
2. Không cần convert (đã là VND)
3. Gửi `2,500,000 VND` trực tiếp đến VNPay
4. User thanh toán `2,500,000 VND` trên VNPay

---

## Cách hoạt động trong code

### PaymentServiceImpl.createVnpayPaymentLink()

```java
// Convert amount từ USD sang VND nếu cần
long amountInVnd = amount;
String defaultCurrency = vnPayConfig.getDefaultCurrency();

if ("USD".equalsIgnoreCase(defaultCurrency)) {
    // Convert USD sang VND
    double exchangeRate = vnPayConfig.getUsdToVndRate();
    amountInVnd = Math.round(amount * exchangeRate);
}

// VNPay yêu cầu amount phải nhân 100
long vnpAmount = amountInVnd * 100;
```

**Ví dụ tính toán:**
- Input: `amount = 100` (USD), `exchangeRate = 25000`
- Step 1: `amountInVnd = 100 × 25000 = 2,500,000 VND`
- Step 2: `vnpAmount = 2,500,000 × 100 = 250,000,000` (VNPay format)
- Output: Gửi `250,000,000` đến VNPay

---

## Lưu ý quan trọng

### 1. Tỷ giá hối đoái

⚠️ **Tỷ giá hối đoái thay đổi liên tục!**

**Giải pháp:**
- **Option 1**: Cập nhật tỷ giá thủ công trong `application.properties` mỗi ngày
- **Option 2**: Tích hợp API tỷ giá (ví dụ: ExchangeRate-API, Fixer.io)
- **Option 3**: Lấy tỷ giá tại thời điểm tạo order và lưu vào database

**Ví dụ tích hợp API tỷ giá:**
```java
// Có thể tạo service để lấy tỷ giá real-time
public class ExchangeRateService {
    public double getUsdToVndRate() {
        // Gọi API lấy tỷ giá
        // Hoặc cache tỷ giá và update mỗi giờ
    }
}
```

### 2. Làm tròn số

Code sử dụng `Math.round()` để làm tròn số khi convert:
- `100.4 USD × 25000 = 2,510,000 VND` (làm tròn)
- `100.6 USD × 25000 = 2,515,000 VND` (làm tròn)

### 3. Lưu trữ dữ liệu

**Hiện tại:**
- `PaymentOrder.amount`: Lưu amount gốc (USD hoặc VND tùy config)
- Không lưu amount đã convert (VND) riêng

**Đề xuất cải thiện (nếu cần):**
```java
// Thêm field vào PaymentOrder
private Long originalAmount;      // Amount gốc (USD)
private String originalCurrency;  // Currency gốc (USD)
private Long convertedAmount;     // Amount đã convert (VND)
private Double exchangeRateUsed; // Tỷ giá đã dùng
```

### 4. Hiển thị cho user

**Trên Frontend:**
- Hiển thị giá gốc: `$100 USD`
- Hiển thị giá thanh toán: `2,500,000 VND` (khi chọn VNPay)
- Hiển thị tỷ giá: `1 USD = 25,000 VND`

**Ví dụ UI:**
```
Order Total: $100 USD
Payment via VNPay: 2,500,000 VND
Exchange Rate: 1 USD = 25,000 VND
```

---

## Test với Postman

### Test với USD

1. **Cấu hình:**
   ```properties
   app.currency.default=USD
   app.currency.exchange.rate.usd.to.vnd=25000
   ```

2. **Tạo order với amount = 100 (USD):**
   ```
   POST /api/orders?paymentMethod=VNPAY
   Body: { ... }
   ```

3. **Response sẽ có payment_link_url:**
   - Amount gửi đến VNPay: `2,500,000 VND` (100 × 25,000)
   - VNPay format: `250,000,000` (2,500,000 × 100)

4. **Kiểm tra trên VNPay:**
   - Mở payment URL
   - Sẽ thấy số tiền: `2,500,000 VND`

### Test với VND

1. **Cấu hình:**
   ```properties
   app.currency.default=VND
   app.currency.exchange.rate.usd.to.vnd=25000
   ```

2. **Tạo order với amount = 2500000 (VND):**
   ```
   POST /api/orders?paymentMethod=VNPAY
   Body: { ... }
   ```

3. **Response:**
   - Amount gửi đến VNPay: `2,500,000 VND` (không convert)
   - VNPay format: `250,000,000` (2,500,000 × 100)

---

## Troubleshooting

### Lỗi: Amount không đúng trên VNPay

**Nguyên nhân:**
- Tỷ giá hối đoái không đúng
- Amount input không đúng đơn vị

**Giải pháp:**
1. Kiểm tra `app.currency.default` có đúng không
2. Kiểm tra `app.currency.exchange.rate.usd.to.vnd` có đúng không
3. Kiểm tra amount input có đúng đơn vị không (USD hay VND)

### Lỗi: Số tiền quá lớn hoặc quá nhỏ

**Nguyên nhân:**
- Tỷ giá quá cao hoặc quá thấp
- Nhầm lẫn giữa USD và VND

**Giải pháp:**
1. Kiểm tra lại tỷ giá
2. Đảm bảo amount input đúng đơn vị
3. Log amount trước và sau khi convert để debug

---

## Best Practices

1. **Cập nhật tỷ giá thường xuyên**: Tỷ giá thay đổi mỗi ngày, nên cập nhật định kỳ
2. **Lưu tỷ giá đã dùng**: Lưu tỷ giá tại thời điểm tạo order để audit
3. **Hiển thị rõ ràng**: Hiển thị cả giá gốc và giá đã convert cho user
4. **Xử lý làm tròn**: Quyết định làm tròn lên hay xuống (hiện tại dùng Math.round)
5. **Test kỹ**: Test với nhiều giá trị khác nhau (số nhỏ, số lớn, số thập phân)

---

## Kết luận

Với cấu hình này, hệ thống sẽ tự động convert USD sang VND khi cần thiết. Bạn chỉ cần:

1. ✅ Cấu hình `app.currency.default` và `app.currency.exchange.rate.usd.to.vnd`
2. ✅ Đảm bảo amount input đúng đơn vị
3. ✅ Cập nhật tỷ giá định kỳ

Hệ thống sẽ tự động xử lý phần còn lại! 🎉

