package teccr.mibdj.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;
import teccr.mibdj.repository.GameRepository;
import teccr.mibdj.repository.MatchRepository;
import teccr.mibdj.repository.UserRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;

    public MatchService(MatchRepository matchRepository,
                        UserRepository userRepository,
                        GameRepository gameRepository) {
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.gameRepository = gameRepository;
    }

    @Transactional
    public Match createMatch(long organizerUserId, long gameId, long placeId, LocalDate fecha,
                             int duracionMin, String notas, List<MatchParticipant> participantes) {
        // 1. Validar que no haya participantes repetidos -> 400
        Set<Long> uniqueUsers = new HashSet<>();
        for (MatchParticipant p : participantes) {
            if (!uniqueUsers.add(p.getUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se permiten participantes repetidos: " + p.getUserId());
            }
        }

        // 2. Validar existencia de referencias -> 404
        if (userRepository.findById(organizerUserId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario organizador no encontrado");
        }
        if (gameRepository.findById(gameId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Juego no encontrado");
        }
        if (!matchRepository.placeExists(placeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado");
        }
        for (MatchParticipant p : participantes) {
            if (userRepository.findById(p.getUserId()).isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Usuario participante no encontrado: " + p.getUserId());
            }
        }

        // 3. Validar que el lugar pertenezca al usuario organizador -> 400
        if (!matchRepository.placeBelongsToUser(placeId, organizerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El lugar no pertenece al usuario organizador");
        }

        // 4. Guardar Partida + Participa en una sola transacción
        return matchRepository.createMatch(
                organizerUserId, gameId, placeId, fecha, duracionMin, notas, participantes
        );
    }

    public List<Match> getUserMatches(long userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return matchRepository.findMatchesByUserId(userId);
    }

    public Match getUserMatchDetail(long userId, long matchId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return matchRepository.findMatchDetailForUser(userId, matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
    }
}