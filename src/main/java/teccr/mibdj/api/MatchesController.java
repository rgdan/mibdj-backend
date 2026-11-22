package teccr.mibdj.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;
import teccr.mibdj.service.MatchService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users/{userId}/matches")
public class MatchesController {

    private final MatchService matchService;

    public MatchesController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public ResponseEntity<List<Match>> getUserMatches(@PathVariable Long userId) {
        return ResponseEntity.ok(matchService.getUserMatches(userId));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<Match> getUserMatchDetail(
            @PathVariable Long userId,
            @PathVariable Long matchId) {
        return ResponseEntity.ok(matchService.getUserMatchDetail(userId, matchId));
    }

    @PostMapping
    public ResponseEntity<Match> createMatch(
            @PathVariable Long userId,
            @Valid @RequestBody CreateMatchRequest request) {
        List<MatchParticipant> participantes = request.participantes().stream()
                .map(p -> {
                    MatchParticipant mp = new MatchParticipant();
                    mp.setUserId(p.userId());
                    mp.setPuntos(p.puntos());
                    mp.setGanador(p.ganador());
                    return mp;
                })
                .toList();

        Match created = matchService.createMatch(
                userId,
                request.gameId(),
                request.placeId(),
                request.fecha(),
                request.duracionMin(),
                request.notas(),
                participantes
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    public record ParticipantRequest(
            @NotNull Long userId,
            @NotNull @PositiveOrZero Integer puntos,
            @NotNull Boolean ganador) {
    }

    public record CreateMatchRequest(
            @NotNull Long gameId,
            @NotNull Long placeId,
            @NotNull LocalDate fecha,
            @NotNull @Positive Integer duracionMin,
            String notas,
            @NotEmpty List<@Valid ParticipantRequest> participantes) {
        public CreateMatchRequest {
            notas = notas == null ? null : notas.trim();
        }
    }
}