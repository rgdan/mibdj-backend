package teccr.mibdj.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.WishlistItem;
import teccr.mibdj.repository.GameRepository;
import teccr.mibdj.repository.UserRepository;
import teccr.mibdj.repository.WishlistRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de la lista de deseos de un usuario (tabla Desea).
 */
@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public WishlistService(WishlistRepository wishlistRepository,
                           UserRepository userRepository,
                           GameRepository gameRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    public List<WishlistItem> getWishlist(Long userId, Integer priority) {
        requireUser(userId);
        return wishlistRepository.findByUser(userId, priority);
    }

    public WishlistItem getItem(Long userId, Long gameId) {
        requireUser(userId);
        return wishlistRepository.findByUserAndGame(userId, gameId)
                .orElseThrow(() -> notInWishlist(gameId));
    }

    @Transactional
    public WishlistItem addGame(Long userId, Long gameId, Integer priority, LocalDate date) {
        requireUser(userId);
        requireGame(gameId);
        if (wishlistRepository.findByUserAndGame(userId, gameId).isPresent()) {
            throw alreadyInWishlist(gameId);
        }
        try {
            wishlistRepository.insert(userId, gameId, priority,
                    date == null ? LocalDate.now() : date);
        } catch (DuplicateKeyException e) {
            throw alreadyInWishlist(gameId);
        }
        return getItem(userId, gameId);
    }

    @Transactional
    public WishlistItem updateGame(Long userId, Long gameId, Integer priority, LocalDate date) {
        requireUser(userId);
        if (wishlistRepository.update(userId, gameId, priority, date) == 0) {
            throw notInWishlist(gameId);
        }
        return getItem(userId, gameId);
    }

    @Transactional
    public void removeGame(Long userId, Long gameId) {
        requireUser(userId);
        if (wishlistRepository.delete(userId, gameId) == 0) {
            throw notInWishlist(gameId);
        }
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

    private static ResponseStatusException notInWishlist(Long gameId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "El juego " + gameId + " no está en la lista de deseos del usuario");
    }

    private static ResponseStatusException alreadyInWishlist(Long gameId) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "El juego " + gameId + " ya está en la lista de deseos del usuario");
    }
}
