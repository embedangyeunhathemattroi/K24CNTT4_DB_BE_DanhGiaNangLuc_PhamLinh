
package vn.rikkei.cinemabox.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import vn.rikkei.cinemabox.payment.PaymentFailedException;
import vn.rikkei.cinemabox.booking.NotEnoughSeatsException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest req) {

        return build(HttpStatus.NOT_FOUND,
                "NOT_FOUND", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> handleNoEndpoint(
            NoResourceFoundException ex,
            HttpServletRequest req) {

        return build(HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                "Không có endpoint " + req.getRequestURI(),
                req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req) {

        List<ApiError.FieldViolation> details =
                ex.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(e -> new ApiError.FieldViolation(
                                e.getField(),
                                e.getDefaultMessage()
                        ))
                        .toList();

        return build(HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Dữ liệu không hợp lệ",
                req, details);
    }

    @ExceptionHandler({
            BadRequestException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiError> handleBadRequest(
            Exception ex, HttpServletRequest req) {

        return build(HttpStatus.BAD_REQUEST,
                "BAD_REQUEST",
                "Dữ liệu đầu vào không hợp lệ",
                req, List.of());
    }

    @ExceptionHandler({
            ConflictException.class,
            DataIntegrityViolationException.class
    })
    public ResponseEntity<ApiError> handleConflict(
            Exception ex, HttpServletRequest req) {

        return build(HttpStatus.CONFLICT,
                "CONFLICT",
                "Dữ liệu bị trùng hoặc vi phạm ràng buộc",
                req, List.of());
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> handlePayment(
            PaymentFailedException ex,
            HttpServletRequest req) {

        return build(HttpStatus.PAYMENT_REQUIRED,
                "PAYMENT_FAILED",
                ex.getMessage(),
                req, List.of());
    }

    @ExceptionHandler(NotEnoughSeatsException.class)
    public ResponseEntity<ApiError> handleSeats(NotEnoughSeatsException ex, HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, "NOT_ENOUGH_SEATS", ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethod(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest req) {

        return build(HttpStatus.METHOD_NOT_ALLOWED,
                "METHOD_NOT_ALLOWED",
                ex.getMessage(), req, List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleOther(
            Exception ex, HttpServletRequest req) {

        log.error("Lỗi chưa xử lý", ex);

        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_ERROR",
                "Lỗi hệ thống",
                req, List.of());
    }

    protected ResponseEntity<ApiError> build(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest req,
            List<ApiError.FieldViolation> details) {

        ApiError body = ApiError.of(
                status.value(),
                error,
                message,
                req.getRequestURI(),
                details
        );

        return ResponseEntity.status(status).body(body);
    }
}
