package teccr.mibdj.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.CollectionItem;
import teccr.mibdj.repository.CollectionRepository;
import teccr.mibdj.repository.GameRepository;
import teccr.mibdj.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de la colección personal de juegos de un usuario (tabla Posee).
 */
@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public CollectionService(CollectionRepository collectionRepository,
                             UserRepository userRepository,
                             GameRepository gameRepository) {
        this.collectionRepository = collectionRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    public List<CollectionItem> getCollection(Long userId, String status) {
        requireUser(userId);
        String statusFilter = status == null || status.isBlank()
                ? null : status.trim().toLowerCase();
        return collectionRepository.findByUser(userId, statusFilter);
    }

    public CollectionItem getItem(Long userId, Long gameId) {
        requireUser(userId);
        return collectionRepository.findByUserAndGame(userId, gameId)
                .orElseThrow(() -> notInCollection(gameId));
    }

    @Transactional
    public CollectionItem addGame(Long userId, Long gameId, LocalDate date, String status) {
        requireUser(userId);
        requireGame(gameId);
        if (collectionRepository.findByUserAndGame(userId, gameId).isPresent()) {
            throw alreadyInCollection(gameId);
        }
        try {
            collectionRepository.insert(userId, gameId,
                    date == null ? LocalDate.now() : date, status);
        } catch (DuplicateKeyException e) {
            throw alreadyInCollection(gameId);
        }
        return getItem(userId, gameId);
    }

    @Transactional
    public CollectionItem updateGame(Long userId, Long gameId, LocalDate date, String status) {
        requireUser(userId);
        if (collectionRepository.update(userId, gameId, date, status) == 0) {
            throw notInCollection(gameId);
        }
        return getItem(userId, gameId);
    }

    @Transactional
    public void removeGame(Long userId, Long gameId) {
        requireUser(userId);
        if (collectionRepository.delete(userId, gameId) == 0) {
            throw notInCollection(gameId);
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

    private static ResponseStatusException notInCollection(Long gameId) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND,
                "El juego " + gameId + " no está en la colección del usuario");
    }

    private static ResponseStatusException alreadyInCollection(Long gameId) {
        return new ResponseStatusException(HttpStatus.CONFLICT,
                "El juego " + gameId + " ya está en la colección del usuario");
    }
}
