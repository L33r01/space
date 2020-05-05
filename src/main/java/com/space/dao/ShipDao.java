package com.space.dao;

import com.space.entity.Ship;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

public interface ShipDao {
    List<Ship> getShipsList(Map<String, String> allParams);

    Integer getShipsCount(Map<String, String> allParams);

    Ship createShip(Ship ship);

    Ship getShip(String id);

    Ship updateShip(String id, Ship ship);

    void deleteShip(String id) throws FileNotFoundException;
}
