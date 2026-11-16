package teccr.mibdj.service;

import org.springframework.stereotype.Service;
import teccr.mibdj.data.Game;
import teccr.mibdj.repository.GameRepository;

import java.util.List;
import java.util.Optional;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    public List<Game> getAllGames(String title, Long categoryId) {
        String searchTitle = title == null || title.isBlank()
                ? null : title.trim();
        return gameRepository.findAll(searchTitle, categoryId);
    }

    public Optional<Game> getGameById(Long id) {
        return gameRepository.findById(id);
    }
}
