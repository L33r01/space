package com.space.service;

import com.space.dao.ShipDao;
import com.space.entity.Ship;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

@Service
public class ShipServiceImpl implements ShipService{

    public final ShipDao shipDao;
    @Autowired
    public ShipServiceImpl(ShipDao shipDao) {
        this.shipDao = shipDao;
    }

    @Override
    public Integer getShipsCount(Map<String, String> allParams) {
        return shipDao.getShipsCount(allParams);
    }

    @Override
    public List<Ship> getShipsList(Map<String,String> allParams) {
      return shipDao.getShipsList(allParams);
    }

    @Override
    public Ship createShip(Ship ship) {
        return shipDao.createShip(ship);
    }

    @Override
    public Ship updateShip(String id, Ship ship) {
        return shipDao.updateShip(id,ship);
    }

    @Override
    public void deleteShip(String id) throws FileNotFoundException {
        shipDao.deleteShip(id);
    }

    @Override
    public Ship getShip(String id) {
        return shipDao.getShip(id);
    }
}

