package teccr.mibdj.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import teccr.mibdj.data.Place;
import teccr.mibdj.data.User;
import teccr.mibdj.repository.PlaceRepository;
import teccr.mibdj.repository.UserRepository;
import teccr.mibdj.service.PlaceService;
import teccr.mibdj.service.UserService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlacesControllerTest {
    private PlaceRepository places;
    private UserRepository users;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        places = mock(PlaceRepository.class);
        users = mock(UserRepository.class);
        when(users.findById(anyLong())).thenReturn(Optional.of(new User()));
        mvc = MockMvcBuilders.standaloneSetup(new UsersController(
                new UserService(users), new PlaceService(places, users))).build();
    }

    @Test
    void createsPlaceWithTrimmedFields() throws Exception {
        Place saved = place(20L, "Casa de Ana", "Alajuela centro");
        when(places.insert(1L, saved.getName(), saved.getAddress())).thenReturn(saved);

        mvc.perform(post("/api/v1/users/1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"  Casa de Ana  \",\"address\":\"  Alajuela centro  \"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.placeId").value(20))
                .andExpect(jsonPath("$.name").value("Casa de Ana"))
                .andExpect(jsonPath("$.address").value("Alajuela centro"));
        verify(places).insert(1L, "Casa de Ana", "Alajuela centro");
    }

    @Test
    void accepts255CharactersAfterTrimming() throws Exception {
        String text = "a".repeat(255);
        when(places.insert(1L, text, text)).thenReturn(place(21L, text, text));
        mvc.perform(post("/api/v1/users/1/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" " + text + " \",\"address\":\" " + text + " \"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void rejectsMissingBlankAndOversizedFieldsWithoutWriting() throws Exception {
        for (String body : List.of(
                "{}", "{\"name\":null,\"address\":\"Casa\"}",
                "{\"name\":\"Casa\",\"address\":null}",
                "{\"name\":\"   \",\"address\":\"Casa\"}",
                "{\"name\":\"Casa\",\"address\":\"   \"}",
                "{\"name\":\"" + "a".repeat(256) + "\",\"address\":\"Casa\"}",
                "{\"name\":\"Casa\",\"address\":\"" + "a".repeat(256) + "\"}")) {
            mvc.perform(post("/api/v1/users/1/places")
                            .contentType(MediaType.APPLICATION_JSON).content(body))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(places);
    }

    @Test
    void missingUserReturns404WithoutWriting() throws Exception {
        when(users.findById(999L)).thenReturn(Optional.empty());
        mvc.perform(post("/api/v1/users/999/places")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Casa\",\"address\":\"Centro\"}"))
                .andExpect(status().isNotFound());
        verifyNoInteractions(places);
    }

    @Test
    void listsPlacesForEachUserSeparately() throws Exception {
        when(places.findByUserId(1L)).thenReturn(List.of(place(1L, "Casa de Ana", "Alajuela")));
        when(places.findByUserId(2L)).thenReturn(List.of(place(2L, "Casa de Luis", "Heredia")));
        mvc.perform(get("/api/v1/users/1/places"))
                .andExpect(jsonPath("$[0].name").value("Casa de Ana"))
                .andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/users/2/places"))
                .andExpect(jsonPath("$[0].name").value("Casa de Luis"))
                .andExpect(jsonPath("$.length()").value(1));
    }

    private Place place(Long id, String name, String address) {
        Place place = new Place();
        place.setPlaceId(id);
        place.setName(name);
        place.setAddress(address);
        return place;
    }
}
