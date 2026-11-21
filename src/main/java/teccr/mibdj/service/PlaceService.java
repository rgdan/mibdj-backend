package teccr.mibdj.service;

import org.springframework.stereotype.Service;
import teccr.mibdj.data.Place;
import teccr.mibdj.repository.PlaceRepository;

import java.util.List;

@Service
public class PlaceService {

    private final PlaceRepository placeRepository;

    // Constructor injection of PlaceRepository
    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public List<Place> getPlacesByUserId(Long userId) {
        return placeRepository.findByUserId(userId);
    }
}

