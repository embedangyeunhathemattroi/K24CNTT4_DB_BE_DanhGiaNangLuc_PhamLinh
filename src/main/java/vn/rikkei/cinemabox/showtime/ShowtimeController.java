
package vn.rikkei.cinemabox.showtime;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/showtimes")
public class ShowtimeController {

    private final ShowtimeService service;

    public ShowtimeController(ShowtimeService service) {
        this.service = service;
    }

    @GetMapping("/{id}")
    public ShowtimeResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }
}
