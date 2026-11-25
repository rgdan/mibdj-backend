package teccr.mibdj.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.Match;
import teccr.mibdj.data.MatchParticipant;
import teccr.mibdj.repository.MatchRepository;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final UserService userService;
    private final GameService gameService;

    public MatchService(MatchRepository matchRepository,
                        UserService userService,
                        GameService gameService) {
        this.matchRepository = matchRepository;
        this.userService = userService;
        this.gameService = gameService;
    }

    @Transactional
    public Match createMatch(long organizerUserId, long gameId, long placeId, LocalDate fecha,
                             int duracionMin, String notas, List<MatchParticipant> participantes) {

        Set<Long> uniqueUsers = new HashSet<>();
        for (MatchParticipant p : participantes) {
            if (!uniqueUsers.add(p.getUserId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se permiten participantes repetidos: " + p.getUserId());
            }
        }


        userService.requireUser(organizerUserId);
        gameService.requireGame(gameId);
        if (!matchRepository.placeExists(placeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lugar no encontrado");
        }
        for (MatchParticipant p : participantes) {
            userService.requireUser(p.getUserId());
        }


        if (!matchRepository.placeBelongsToUser(placeId, organizerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El lugar no pertenece al usuario organizador");
        }


        return matchRepository.createMatch(
                organizerUserId, gameId, placeId, fecha, duracionMin, notas, participantes
        );
    }

    public List<Match> getUserMatches(long userId) {
        userService.requireUser(userId);
        return matchRepository.findMatchesByUserId(userId);
    }

    public Match getUserMatchDetail(long userId, long matchId) {
        userService.requireUser(userId);
        return matchRepository.findMatchDetailForUser(userId, matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Partida no encontrada"));
    }
}