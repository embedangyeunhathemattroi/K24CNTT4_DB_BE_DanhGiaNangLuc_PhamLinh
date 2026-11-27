package vn.rikkei.cinemabox.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.rikkei.cinemabox.common.ResourceNotFoundException;
import vn.rikkei.cinemabox.customer.Customer;
import vn.rikkei.cinemabox.customer.CustomerRepository;
import vn.rikkei.cinemabox.payment.PaymentGateway;
import vn.rikkei.cinemabox.showtime.Showtime;
import vn.rikkei.cinemabox.showtime.ShowtimeRepository;

@Service
public class BookingService {
    private final BookingRepository bookings;
    private final CustomerRepository customers;
    private final ShowtimeRepository showtimes;
    private final PaymentGateway payment;

    public BookingService(BookingRepository bookings, CustomerRepository customers,
                          ShowtimeRepository showtimes, PaymentGateway payment) {
        this.bookings = bookings;
        this.customers = customers;
        this.showtimes = showtimes;
        this.payment = payment;
    }

    @Transactional
    public BookingResponse create(CreateBookingRequest request) {
        Customer customer = customers.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng id=" + request.customerId()));
        Showtime showtime = showtimes.findWithLockById(request.showtimeId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy suất chiếu id=" + request.showtimeId()));
        if (showtime.getAvailableSeats() < request.quantity()) {
            throw new NotEnoughSeatsException("Suất chiếu không đủ ghế");
        }

        long amount = Math.multiplyExact(showtime.getPrice(), request.quantity());
        if ("GOLD".equals(customer.getMemberLevel())) {
            amount = Math.floorDiv(Math.multiplyExact(amount, 9), 10);
        }
        showtime.setAvailableSeats(showtime.getAvailableSeats() - request.quantity());
        String paymentRef = payment.charge(customer.getId(), amount);

        Booking booking = new Booking();
        booking.setCustomer(customer);
        booking.setShowtime(showtime);
        booking.setQuantity(request.quantity());
        booking.setTotalAmount(amount);
        booking.setStatus("CONFIRMED");
        booking.setPaymentRef(paymentRef);
        return BookingResponse.from(bookings.saveAndFlush(booking));
    }
}
