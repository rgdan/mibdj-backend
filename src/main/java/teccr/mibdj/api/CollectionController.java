package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.CollectionItem;
import teccr.mibdj.service.CollectionService;

import java.time.LocalDate;
import java.util.List;

/**
 * Rutas de la colección personal de juegos de un usuario.
 */
@RestController
@RequestMapping("/api/v1/users/{userId}/collection")
public class CollectionController {

    private static final String VALID_STATUS = "nuevo|usado|prestado";

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping
    public List<CollectionItem> getCollection(
            @PathVariable Long userId,
            @RequestParam(name = "status", required = false) String status) {
        return collectionService.getCollection(userId, status);
    }

    @GetMapping("/{gameId}")
    public CollectionItem getItem(@PathVariable Long userId, @PathVariable Long gameId) {
        return collectionService.getItem(userId, gameId);
    }

    @PostMapping
    public ResponseEntity<CollectionItem> addGame(
            @PathVariable Long userId,
            @Valid @RequestBody AddRequest request) {
        CollectionItem item = collectionService.addGame(
                userId, request.gameId(), request.date(), request.status());
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @PutMapping("/{gameId}")
    public CollectionItem updateGame(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @Valid @RequestBody UpdateRequest request) {
        return collectionService.updateGame(userId, gameId, request.date(), request.status());
    }

    @DeleteMapping("/{gameId}")
    public ResponseEntity<Void> removeGame(@PathVariable Long userId, @PathVariable Long gameId) {
        collectionService.removeGame(userId, gameId);
        return ResponseEntity.noContent().build();
    }

    public record AddRequest(
            @NotNull Long gameId,
            @PastOrPresent LocalDate date,
            @NotBlank @Pattern(regexp = VALID_STATUS) String status) {
        public AddRequest {
            status = status == null ? null : status.trim().toLowerCase();
        }
    }

    public record UpdateRequest(
            @PastOrPresent LocalDate date,
            @NotBlank @Pattern(regexp = VALID_STATUS) String status) {
        public UpdateRequest {
            status = status == null ? null : status.trim().toLowerCase();
        }
    }
}
