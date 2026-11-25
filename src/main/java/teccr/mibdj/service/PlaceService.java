package teccr.mibdj.service;

import org.springframework.stereotype.Service;
import teccr.mibdj.data.Place;
import teccr.mibdj.repository.PlaceRepository;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;
    private final UserService userService;

    public PlaceService(PlaceRepository placeRepository, UserService userService) {
        this.placeRepository = placeRepository;
        this.userService = userService;
    }

    public List<Place> getPlacesByUserId(Long userId) {
        userService.requireUser(userId);
        return placeRepository.findByUserId(userId);
    }

    public Place createPlace(Long userId, String name, String address) {
        userService.requireUser(userId);
        return placeRepository.insert(userId, name, address);
    }
}

