package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.WishlistItem;
import teccr.mibdj.service.WishlistService;

import java.time.LocalDate;
import java.util.List;

/**
 * Rutas de la lista de deseos (wishlist) de un usuario.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistItem> getWishlist(
            @PathVariable Long userId,
            @RequestParam(name = "priority", required = false) Integer priority) {
        return wishlistService.getWishlist(userId, priority);
    }

    @GetMapping("/{gameId}")
    public WishlistItem getItem(@PathVariable Long userId, @PathVariable Long gameId) {
        return wishlistService.getItem(userId, gameId);
    }

    @PostMapping
    public ResponseEntity<WishlistItem> addGame(
            @PathVariable Long userId,
            @Valid @RequestBody AddRequest request) {
        WishlistItem item = wishlistService.addGame(
                userId, request.gameId(), request.priority(), request.date());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{gameId}")
    public WishlistItem updateGame(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @Valid @RequestBody UpdateRequest request) {
        return wishlistService.updateGame(userId, gameId, request.priority(), request.date());
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGame(@PathVariable Long userId, @PathVariable Long gameId) {
        wishlistService.removeGame(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    public record AddRequest(
            @NotNull Long gameId,
            @NotNull @Min(1) @Max(3) Integer priority,
            @PastOrPresent LocalDate date) {
    }

    public record UpdateRequest(
            @NotNull @Min(1) @Max(3) Integer priority,
            @PastOrPresent LocalDate date) {
    }
}
