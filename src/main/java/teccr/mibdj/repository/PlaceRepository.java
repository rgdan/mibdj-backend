package teccr.mibdj.repository;

import teccr.mibdj.data.Place;


import java.util.List;


public interface PlaceRepository {

    List<Place> findByUserId(Long userId);

    Place insert(Long userId, String name, String address);

}
