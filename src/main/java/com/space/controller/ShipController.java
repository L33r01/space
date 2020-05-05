package com.space.controller;

import com.space.entity.Ship;
import com.space.mapper.ShipMapper;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;
import java.time.temporal.TemporalField;
import java.util.*;

@RestController
@RequestMapping("/rest")
public class ShipController {

    @Autowired
    public ShipService shipService;

    @GetMapping("/ships")
    public ResponseEntity<List<Ship>> getShipsList(@RequestParam Map<String, String> allParams) {

        List<Ship> allShips = shipService.getShipsList(allParams);
        ResponseEntity<List<Ship>> responseEntity = new ResponseEntity<>(allShips, HttpStatus.OK);

        return responseEntity;
    }




    @GetMapping("/ships/count")
    public ResponseEntity<Integer> getShipsCount(@RequestParam Map<String, String> allParams) {
        return new ResponseEntity<>(shipService.getShipsCount(allParams), HttpStatus.OK);
    }

    @PostMapping("/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship shipBody) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.set(2800, 01, 01);
        calendar2.set(3019, 01, 01);

        if (shipBody.getUsed() == null) {
            shipBody.setUsed(false);
        }

        if (shipBody.getName() == null
                || shipBody.getName().equals("") || shipBody.getPlanet().equals("")
                || shipBody.getSpeed() == null || shipBody.getCrewSize() == null
                || shipBody.getName().length() > 50 || shipBody.getPlanet().length() > 50
                || shipBody.getProdDate().getTime() < 0
                || shipBody.getProdDate().getTime() < calendar.getTimeInMillis()
                || shipBody.getProdDate().getTime() > calendar2.getTimeInMillis()
                || shipBody.getSpeed() < 0.01 || shipBody.getSpeed() > 0.99
                || shipBody.getCrewSize() < 1 || shipBody.getCrewSize() > 9999) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        shipBody.setSpeed(new BigDecimal(shipBody.getSpeed()).setScale(2, RoundingMode.HALF_UP).doubleValue());
        shipBody.setRating(countRating(shipBody));
        Ship ship = shipService.createShip(shipBody);

        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable String id) {
        try {
            Integer i = Integer.parseInt(id);
            if (i < 1) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShip(id);
        if (ship == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable String id ,@RequestBody Ship shipBody) {
        Calendar calendar = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        calendar.set(2800, 01, 01);
        calendar2.set(3019, 01, 01);
       if (     shipBody.getName()!=null &&   shipBody.getName().equals("") || shipBody.getPlanet()!=null && shipBody.getPlanet().equals("")
                || shipBody.getName()!=null && shipBody.getName().length() > 50 ||shipBody.getPlanet()!=null && shipBody.getPlanet().length() > 50
                || shipBody.getProdDate()!=null && shipBody.getProdDate().getTime() < 0
                || shipBody.getProdDate()!=null && shipBody.getProdDate().getTime() < calendar.getTimeInMillis()
                || shipBody.getProdDate()!=null && shipBody.getProdDate().getTime() > calendar2.getTimeInMillis()
                || shipBody.getSpeed()!=null    && shipBody.getSpeed() < 0.01 ||shipBody.getSpeed()!=null&& shipBody.getSpeed() > 0.99
                || shipBody.getCrewSize()!=null && shipBody.getCrewSize() < 1 || shipBody.getCrewSize()!=null && shipBody.getCrewSize() > 9999) {

            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Integer i = Integer.parseInt(id);
            if (i < 1) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        if (shipBody.getName()==null
                &&shipBody.getPlanet()==null
                &&shipBody.getShipType()==null
                &&shipBody.getProdDate()==null
                &&shipBody.getUsed()==null
                &&shipBody.getSpeed()==null
                &&shipBody.getCrewSize()==null){
            return new ResponseEntity<>(shipService.getShip(id),HttpStatus.OK);
        }

        Ship ship = null;
        try {
            ship = shipService.updateShip(id,shipBody);
        } catch (Exception e) {
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        if (ship == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @DeleteMapping("/ships/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable String id){
        try {
            Integer i = Integer.parseInt(id);
            if (i < 1) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } catch (NumberFormatException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        try {
            shipService.deleteShip(id);
        } catch (FileNotFoundException e) {
            return new ResponseEntity<>(null,HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    public static Double countRating(Ship ship) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        double k = ship.getUsed() ? 0.5 : 1.0;
        int y0 = 3019;
        int y1 = calendar.get(Calendar.YEAR);

        double result = (80 * ship.getSpeed() * k) / (y0 - y1 + 1);
        result = new BigDecimal(result).setScale(2, RoundingMode.HALF_UP).doubleValue();
        return result;
    }


}

