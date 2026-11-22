package teccr.mibdj.repository;

import teccr.mibdj.data.User;

import teccr.mibdj.api.UsersController.AverageDurationByGame;
import teccr.mibdj.api.UsersController.FrequentCompanion;
import teccr.mibdj.api.UsersController.MostPlayedGame;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> findAll();
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);
    List<MostPlayedGame> findMostPlayedGames(long userId);
    List<AverageDurationByGame> findAverageDurationByGame(long userId);
    List<FrequentCompanion> findFrequentCompanions(long userId);
}
