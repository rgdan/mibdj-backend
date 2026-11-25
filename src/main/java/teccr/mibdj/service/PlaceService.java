package teccr.mibdj.service;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import teccr.mibdj.data.Place;
import teccr.mibdj.repository.PlaceRepository;
import teccr.mibdj.repository.UserRepository;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;

    public PlaceService(PlaceRepository placeRepository, UserRepository userRepository) {
        this.placeRepository = placeRepository;
        this.userRepository = userRepository;
    }

    public List<Place> getPlacesByUserId(Long userId) {
        return placeRepository.findByUserId(userId);
    }

    public Place createPlace(Long userId, String name, String address) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El usuario no existe");
        }
        return placeRepository.insert(userId, name, address);
    }
}

