package teccr.mibdj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.Game;
import teccr.mibdj.data.MatchParticipant;
import teccr.mibdj.data.User;
import teccr.mibdj.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntityChecksTest {
    private UserRepository users;
    private GameRepository games;
    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        games = mock(GameRepository.class);
        userService = new UserService(users);
        gameService = new GameService(games);
    }

    @Test
    void existingEntitiesAreReturned() {
        User user = new User();
        Game game = new Game();
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(games.findById(1L)).thenReturn(Optional.of(game));
        assertSame(user, userService.requireUser(1L));
        assertSame(game, gameService.requireGame(1L));
    }

    @Test
    void missingUsersAreRejectedBeforeFeatureQueriesOrWrites() {
        CollectionRepository collection = mock(CollectionRepository.class);
        WishlistRepository wishlist = mock(WishlistRepository.class);
        RatingRepository ratings = mock(RatingRepository.class);
        MatchRepository matches = mock(MatchRepository.class);
        PlaceRepository places = mock(PlaceRepository.class);

        for (Executable action : List.<Executable>of(
                () -> new CollectionService(collection, userService, gameService).getCollection(99L, null),
                () -> new WishlistService(wishlist, userService, gameService).getWishlist(99L, null),
                () -> new RatingService(ratings, userService, gameService).saveRating(99L, 1L, 8, null),
                () -> new MatchService(matches, userService, gameService).getUserMatches(99L),
                () -> new PlaceService(places, userService).getPlacesByUserId(99L),
                () -> new PlaceService(places, userService).createPlace(99L, "Casa", "Centro"),
                () -> userService.getUserStats(99L))) {
            assertMissing(action, "El usuario no existe");
        }
        verifyNoInteractions(collection, wishlist, ratings, matches, places);
    }

    @Test
    void missingGamesAreRejectedBeforeWriting() {
        when(users.findById(1L)).thenReturn(Optional.of(new User()));
        CollectionRepository collection = mock(CollectionRepository.class);
        WishlistRepository wishlist = mock(WishlistRepository.class);
        RatingRepository ratings = mock(RatingRepository.class);
        MatchRepository matches = mock(MatchRepository.class);

        for (Executable action : List.<Executable>of(
                () -> new CollectionService(collection, userService, gameService).addGame(1L, 99L, null, "nuevo"),
                () -> new WishlistService(wishlist, userService, gameService).addGame(1L, 99L, 2, null),
                () -> new RatingService(ratings, userService, gameService).saveRating(1L, 99L, 8, null),
                () -> new RatingService(ratings, userService, gameService).getGameRatings(99L),
                () -> new MatchService(matches, userService, gameService)
                        .createMatch(1L, 99L, 1L, LocalDate.now(), 30, null, List.of()))) {
            assertMissing(action, "El juego no existe");
        }
        verifyNoInteractions(collection, wishlist, ratings, matches);
    }

    @Test
    void matchStillChecksParticipantsAndPlaceOwnership() {
        when(users.findById(1L)).thenReturn(Optional.of(new User()));
        when(games.findById(1L)).thenReturn(Optional.of(new Game()));
        MatchRepository matches = mock(MatchRepository.class);
        when(matches.placeExists(1L)).thenReturn(true);
        MatchService service = new MatchService(matches, userService, gameService);
        MatchParticipant participant = new MatchParticipant();
        participant.setUserId(2L);

        assertMissing(() -> service.createMatch(1L, 1L, 1L, LocalDate.now(), 30, null,
                List.of(participant)), "El usuario no existe");

        when(users.findById(2L)).thenReturn(Optional.of(new User()));
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.createMatch(1L, 1L, 1L, LocalDate.now(), 30, null, List.of(participant)));
        assertEquals(400, error.getStatusCode().value());
        verify(matches, never()).createMatch(anyLong(), anyLong(), anyLong(), any(), anyInt(), any(), anyList());
    }

    private void assertMissing(Executable action, String reason) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, action);
        assertEquals(404, error.getStatusCode().value());
        assertEquals(reason, error.getReason());
    }
}
