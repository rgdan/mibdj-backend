package teccr.mibdj.repository;

import teccr.mibdj.data.WishlistItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WishlistRepository {
    List<WishlistItem> findByUser(Long userId, Integer priority);
    Optional<WishlistItem> findByUserAndGame(Long userId, Long gameId);
    void insert(Long userId, Long gameId, Integer priority, LocalDate date);
    int update(Long userId, Long gameId, Integer priority, LocalDate date);
    int delete(Long userId, Long gameId);
}
