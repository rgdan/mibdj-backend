package teccr.mibdj.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.Game;
import teccr.mibdj.data.GameRatings;
import teccr.mibdj.data.Rating;
import teccr.mibdj.repository.GameRepository;
import teccr.mibdj.repository.RatingRepository;
import teccr.mibdj.repository.UserRepository;

import java.util.List;

/**
 * Lógica de las valoraciones (calificación y comentario) de los juegos (tabla Valora).
 */
@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public RatingService(RatingRepository ratingRepository,
                         UserRepository userRepository,
                         GameRepository gameRepository) {
        this.ratingRepository = ratingRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    /** Resultado de guardar una valoración: indica si se creó o se actualizó. */
    public record SaveResult(Rating rating, boolean created) {
    }

    public GameRatings getGameRatings(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El juego no existe"));
        List<Rating> ratings = ratingRepository.findByGame(gameId);

        GameRatings result = new GameRatings();
        result.setGameId(game.getId());
        result.setGameTitle(game.getTitle());
        result.setRatings(ratings);
        result.setCount(ratings.size());
        if (!ratings.isEmpty()) {
            double average = ratings.stream().mapToInt(Rating::getRating).average().orElse(0);
            result.setAverage(Math.round(average * 10) / 10.0);
        }
        return result;
    }

    public Rating getUserRating(Long userId, Long gameId) {
        requireUser(userId);
        requireGame(gameId);
        return ratingRepository.findByUserAndGame(userId, gameId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "El usuario todavía no ha valorado este juego"));
    }

    /**
     * Crea la valoración si el usuario no había valorado el juego; si ya existía, la reemplaza.
     */
    @Transactional
    public SaveResult saveRating(Long userId, Long gameId, Integer rating, String comment) {
        requireUser(userId);
        requireGame(gameId);
        boolean created = ratingRepository.update(userId, gameId, rating, comment) == 0;
        if (created) {
            ratingRepository.insert(userId, gameId, rating, comment);
        }
        Rating saved = ratingRepository.findByUserAndGame(userId, gameId).orElseThrow();
        return new SaveResult(saved, created);
    }

    private void requireUser(Long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe");
        }
    }

    private void requireGame(Long gameId) {
        if (gameRepository.findById(gameId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El juego no existe");
        }
    }
}
