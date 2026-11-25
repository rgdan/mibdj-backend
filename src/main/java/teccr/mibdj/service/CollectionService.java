package teccr.mibdj.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.CollectionItem;
import teccr.mibdj.repository.CollectionRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de la colección personal de juegos de un usuario (tabla Posee).
 */
@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserService userService;
    private final GameService gameService;

    public CollectionService(CollectionRepository collectionRepository,
                             UserService userService,
                             GameService gameService) {
        this.collectionRepository = collectionRepository;
        this.userService = userService;
        this.gameService = gameService;
    }

    public List<CollectionItem> getCollection(Long userId, String status) {
        userService.requireUser(userId);
        String statusFilter = status == null || status.isBlank()
                ? null : status.trim().toLowerCase();
        return collectionRepository.findByUser(userId, statusFilter);
    }

    public CollectionItem getItem(Long userId, Long gameId) {
        userService.requireUser(userId);
        return collectionRepository.findByUserAndGame(userId, gameId)
                .orElseThrow(() -> notInCollection(gameId));
    }

    @Transactional
    public CollectionItem addGame(Long userId, Long gameId, LocalDate date, String status) {
        userService.requireUser(userId);
        gameService.requireGame(gameId);
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
        userService.requireUser(userId);
        if (collectionRepository.update(userId, gameId, date, status) == 0) {
            throw notInCollection(gameId);
        }
        return getItem(userId, gameId);
    }

    @Transactional
    public void removeGame(Long userId, Long gameId) {
        userService.requireUser(userId);
        if (collectionRepository.delete(userId, gameId) == 0) {
            throw notInCollection(gameId);
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
