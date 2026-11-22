package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.User;
import teccr.mibdj.data.Place;
import teccr.mibdj.service.PlaceService;
import teccr.mibdj.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class UsersController {

    private final UserService userService;
    private final PlaceService placeService;

    public UsersController(UserService userService, PlaceService placeService) {
        this.userService = userService;
        this.placeService = placeService;
    }

    @GetMapping("/users")
    public Iterable<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{id}/places")
    public ResponseEntity<List<Place>> getUserPlaces(@PathVariable Long id) {
        if (userService.getUserById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<Place> places = placeService.getPlacesByUserId(id);
        return ResponseEntity.ok(places);
    }

    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<UserStatsResponse> getUserStats(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserStats(userId));
    }

    public record MostPlayedGame(
            Long gameId,
            String gameTitle,
            Integer matchesPlayed) {
    }

    public record AverageDurationByGame(
            Long gameId,
            String gameTitle,
            Double averageDurationMin) {
    }

    public record FrequentCompanion(
            Long userId,
            String userName,
            Integer sharedMatches) {
    }

    public record UserStatsResponse(
            List<MostPlayedGame> mostPlayedGames,
            List<AverageDurationByGame> averageDurationByGame,
            List<FrequentCompanion> frequentCompanions) {
    }

    @PostMapping("/session")
    public ResponseEntity<User> login(@Valid @RequestBody SessionRequest request) {
        return userService.getUserByEmail(request.email())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record SessionRequest(@NotBlank @Email String email) {
        public SessionRequest {
            email = email == null ? null : email.trim();
        }
    }
}
