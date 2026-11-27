package vn.rikkei.cinemabox.booking;

public class NotEnoughSeatsException extends RuntimeException {
    public NotEnoughSeatsException(String message) { super(message); }
}
