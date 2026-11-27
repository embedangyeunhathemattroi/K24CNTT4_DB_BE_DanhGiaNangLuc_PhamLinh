
package vn.rikkei.cinemabox.customer;

import jakarta.validation.constraints.*;

public record CreateCustomerRequest(

        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 100)
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 150)
        String email,

        @Pattern(
                regexp = "^0[0-9]{9}$",
                message = "Số điện thoại phải gồm 10 chữ số và bắt đầu bằng 0"
        )
        String phone,

        @Pattern(
                regexp = "STANDARD|GOLD",
                message = "Hạng thành viên không hợp lệ"
        )
        String memberLevel
) {
}
