package teccr.mibdj.data;

import lombok.Data;



@Data
public class Place {
    private Long placeId;
    private String name;
    private String address;
}