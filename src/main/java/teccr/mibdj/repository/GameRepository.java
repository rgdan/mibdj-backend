package teccr.mibdj.repository;

import teccr.mibdj.data.Game;

import java.util.List;
import java.util.Optional;

public interface GameRepository {
    List<Game> findAll(String title, Long categoryId);
    Optional<Game> findById(Long id);
}
