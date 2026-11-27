package vn.rikkei.cinemabox.booking;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id, Long customerId, Long showtimeId, Integer quantity,
        Long totalAmount, String status, String paymentRef,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") LocalDateTime createdAt) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getCustomer().getId(),
                b.getShowtime().getId(), b.getQuantity(), b.getTotalAmount(),
                b.getStatus(), b.getPaymentRef(), b.getCreatedAt());
    }
}
