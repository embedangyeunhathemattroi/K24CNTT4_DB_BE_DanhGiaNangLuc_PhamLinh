# Bài làm CinemaBox – Database + Backend

Họ tên: Phạm Ngọc Linh

Mã sinh viên: [Điền mã sinh viên]

Lớp: K24CNTT4

## Pha 1 (không dùng AI)

Tự đánh dấu phần đã xong trước mốc 1 (chỉ để GV tham khảo, không thay cho kết quả chấm):

- [ ] I1
- [ ] I2
- [ ] I3
- [ ] Endpoint GET /api/showtimes/{id}
- [ ] F1
- [ ] F2
- [ ] F3
- [ ] API khách hàng (tạo, xem, danh sách, validate, lỗi)

Ghi chú (nếu có):

Đây là bản thực hành ôn tập có tham khảo AI. Kiểm tra các câu SQL bằng H2 Console trên dữ liệu mẫu của đề.
## Pha 2 (được dùng AI)

### Cách bạn chống bán vượt số ghế khi nhiều người đặt cùng lúc

BookingService.create dùng @Transactional; ShowtimeRepository.findWithLockById dùng PESSIMISTIC_WRITE để khóa dòng suất chiếu. Request sau chờ transaction trước hoàn tất, đọc số ghế mới rồi mới quyết định. Đã chạy Maven test: nhieuNguoiDatCungSuatKhongVuotGhe đạt; sau tám request đồng thời, ghế còn lại khớp số vé CONFIRMED bán thêm.

### Cách bạn hoàn tác khi thanh toán lỗi

PaymentGateway.charge chạy trong transaction đặt vé. Khi cổng từ chối, PaymentFailedException là RuntimeException nên Spring rollback thay đổi số ghế; booking chỉ được lưu sau thanh toán thành công.

### Test bạn đã viết (tên lớp, tên test, mỗi test kiểm tra điều gì)

BookingApiTest: datVeThanhCong kiểm tra 201, giảm giá GOLD, paymentRef và ghế; khongDuGheKhongDoiDuLieu kiểm tra 409 và dữ liệu giữ nguyên; thanhToanThatBaiHoanTac kiểm tra 402 và rollback; nhieuNguoiDatCungSuatKhongVuotGhe kiểm tra tám luồng cùng đặt một suất. Maven test chạy thực tế ngày 27/11/2026: BookingApiTest 4 test, TuKiemTraTest 2 test; tổng 6 test, 0 failure, 0 error, 0 skipped.

### Nhật ký dùng AI

| # | Bạn hỏi AI điều gì | AI gợi ý gì | Bạn giữ / sửa / bỏ, vì sao |
|---|---|---|---|
| 1 | Đối chiếu phần Intern và Fresher với đề | Giữ SQL, API và DTO đang đúng | Giữ vì đã đọc mã và đối chiếu đề; không sửa phần đang hoạt động. |
| 2 | Hoàn thiện POST /api/bookings | DTO validation, transaction, khóa ghi, tính tiền GOLD | Giữ; đã kiểm chứng bằng 6 Maven test đạt và kiểm tra số ghế, số vé trong H2 qua JdbcTemplate. |
| 3 | Kiểm thử đặt vé | Test thành công, không đủ ghế, rollback và đồng thời | Sửa test từ gọi trực tiếp service sang MockMvc và JdbcTemplate do test compiler không nhận import class project; giữ vì 4 test đã chạy đạt. |
