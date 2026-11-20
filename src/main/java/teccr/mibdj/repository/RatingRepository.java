package teccr.mibdj.repository;

import teccr.mibdj.data.Rating;

import java.util.List;
import java.util.Optional;

public interface RatingRepository {
    List<Rating> findByGame(Long gameId);
    Optional<Rating> findByUserAndGame(Long userId, Long gameId);
    void insert(Long userId, Long gameId, Integer rating, String comment);
    int update(Long userId, Long gameId, Integer rating, String comment);
}
