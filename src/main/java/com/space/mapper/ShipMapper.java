package com.space.mapper;

import com.space.entity.Ship;
import com.space.model.ShipType;
import org.springframework.jdbc.core.RowMapper;

import javax.persistence.criteria.CriteriaBuilder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class ShipMapper {

    public Ship readShip(Object[] o){
        Ship ship = new Ship();
        ship.setId(Long.parseLong(o[0].toString()));
        ship.setName((String)o[1]);
        ship.setPlanet((String)o[2]);
        ship.setShipType(ShipType.valueOf((String)o[3]));
        ship.setProdDate(((Date)o[4]));
        ship.setUsed((Boolean)o[5]);
        ship.setSpeed((Double)o[6]);
        ship.setCrewSize((Integer)o[7]);
        ship.setRating((Double)o[8]);

        return ship;
    }
}
