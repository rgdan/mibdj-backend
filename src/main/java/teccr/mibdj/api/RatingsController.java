package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.GameRatings;
import teccr.mibdj.data.Rating;
import teccr.mibdj.service.RatingService;

/**
 * Rutas de las valoraciones (calificación y comentario) de los juegos.
 */
@RestController
@RequestMapping("/api/v1")
public class RatingsController {

    private final RatingService ratingService;

    public RatingsController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/games/{gameId}/ratings")
    public GameRatings getGameRatings(@PathVariable Long gameId) {
        return ratingService.getGameRatings(gameId);
    }

    @GetMapping("/users/{userId}/ratings/{gameId}")
    public Rating getUserRating(@PathVariable Long userId, @PathVariable Long gameId) {
        return ratingService.getUserRating(userId, gameId);
    }

    @PutMapping("/users/{userId}/ratings/{gameId}")
    public ResponseEntity<Rating> saveRating(
            @PathVariable Long userId,
            @PathVariable Long gameId,
            @Valid @RequestBody RatingRequest request) {
        RatingService.SaveResult result =
                ratingService.saveRating(userId, gameId, request.rating(), request.comment());
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.rating());
    }

    public record RatingRequest(
            @NotNull @Min(1) @Max(10) Integer rating,
            @Size(max = 255) String comment) {
        public RatingRequest {
            // Un comentario vacío se guarda como NULL (sin comentario)
            comment = comment == null || comment.isBlank() ? null : comment.trim();
        }
    }
}
