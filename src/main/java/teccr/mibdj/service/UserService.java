package teccr.mibdj.service;


import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import teccr.mibdj.data.User;
import teccr.mibdj.repository.UserRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service responsible for user-related business logic.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    // Constructor injection of UserRepository
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(Long id) {
        if (id == null || id < 0) {
            return Optional.empty();
        }
        return userRepository.findById(id);
    }

    public User requireUser(Long userId) {
        return getUserById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe"));
    }

    public teccr.mibdj.api.UsersController.UserStatsResponse getUserStats(long userId) {
        requireUser(userId);

        return new teccr.mibdj.api.UsersController.UserStatsResponse(
                userRepository.findMostPlayedGames(userId),
                userRepository.findAverageDurationByGame(userId),
                userRepository.findFrequentCompanions(userId)
        );
    }
}
