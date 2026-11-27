package vn.rikkei.cinemabox;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BookingApiTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private int seats(long id) {
        return jdbc.queryForObject("SELECT available_seats FROM showtimes WHERE id = ?", Integer.class, id);
    }

    private int confirmed(long id) {
        return jdbc.queryForObject("SELECT COALESCE(SUM(quantity), 0) FROM bookings WHERE showtime_id = ? AND status = 'CONFIRMED'", Integer.class, id);
    }

    @Test
    void datVeThanhCongVaGiamGiaGold() throws Exception {
        int before = seats(1);
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"showtimeId\":1,\"quantity\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(216000))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.paymentRef").isNotEmpty());
        assertEquals(before - 2, seats(1));
    }

    @Test
    void khongDuGheKhongDoiDuLieu() throws Exception {
        int before = seats(10);
        int sold = confirmed(10);
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":2,\"showtimeId\":10,\"quantity\":1}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NOT_ENOUGH_SEATS"));
        assertEquals(before, seats(10));
        assertEquals(sold, confirmed(10));
    }

    @Test
    void thanhToanThatBaiHoanTac() throws Exception {
        int before = seats(11);
        int sold = confirmed(11);
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":2,\"showtimeId\":11,\"quantity\":10}"))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.error").value("PAYMENT_FAILED"));
        assertEquals(before, seats(11));
        assertEquals(sold, confirmed(11));
    }

    @Test
    void nhieuNguoiDatCungSuatKhongVuotGhe() throws Exception {
        int before = seats(6);
        int soldBefore = confirmed(6);
        ExecutorService pool = Executors.newFixedThreadPool(8);
        CountDownLatch ready = new CountDownLatch(8);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        try {
            for (int i = 0; i < 8; i++) {
                results.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    MvcResult result = mvc.perform(post("/api/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"customerId\":2,\"showtimeId\":6,\"quantity\":5}"))
                            .andReturn();
                    return result.getResponse().getStatus();
                }));
            }
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            start.countDown();
            int success = 0;
            for (Future<Integer> result : results) {
                int status = result.get(15, TimeUnit.SECONDS);
                assertTrue(status == 201 || status == 409, "HTTP " + status);
                if (status == 201) success++;
            }
            assertEquals(before - success * 5, seats(6));
            assertEquals(soldBefore + success * 5, confirmed(6));
            assertTrue(seats(6) >= 0);
        } finally {
            pool.shutdownNow();
        }
    }
}