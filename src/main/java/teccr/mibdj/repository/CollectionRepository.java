package teccr.mibdj.repository;

import teccr.mibdj.data.CollectionItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CollectionRepository {
    List<CollectionItem> findByUser(Long userId, String status);
    Optional<CollectionItem> findByUserAndGame(Long userId, Long gameId);
    void insert(Long userId, Long gameId, LocalDate date, String status);
    int update(Long userId, Long gameId, LocalDate date, String status);
    int delete(Long userId, Long gameId);
}
