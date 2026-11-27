package vn.rikkei.cinemabox.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long customerId,
        @NotNull Long showtimeId,
        @NotNull @Min(1) @Max(10) Integer quantity) {
}
