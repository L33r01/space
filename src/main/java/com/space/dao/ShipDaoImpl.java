package com.space.dao;

import com.space.controller.ShipController;
import com.space.controller.ShipOrder;
import com.space.entity.Ship;
import com.space.mapper.ShipMapper;
import com.space.model.ShipType;
import org.omg.CosNaming.NamingContextPackage.NotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Repository
public class ShipDaoImpl implements ShipDao {
    @PersistenceContext
    public final EntityManager entityManager;
    public final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;
    public final PlatformTransactionManager transactionManager;

    @Override
    @Transactional
    public void deleteShip(String id) throws FileNotFoundException {
        String sql = "DELETE FROM ship WHERE id=?";
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, id);

        if (getShip(id) == null) {
            throw new FileNotFoundException("a ship with id =" + id + " is not found");
        }

        q.executeUpdate();


    }

    @Override
    @Transactional
    public Ship updateShip(String id, Ship shipBody) {

        String sql = "UPDATE ship SET ";
        Boolean oneFilterAlready = false;

        if (shipBody.getName() != null) {
            sql += "name='?' ".replace("?", shipBody.getName());
            oneFilterAlready = true;
        }

        if (shipBody.getPlanet() != null) {

            if (oneFilterAlready) {
                sql += ",planet='?' ".replace("?", shipBody.getPlanet());
            } else {
                sql += "planet='?' ".replace("?", shipBody.getPlanet());
                oneFilterAlready = true;
            }
        }
        if (shipBody.getShipType() != null) {
            if (oneFilterAlready) {
                sql += ",shipType='?' ".replace("?", shipBody.getShipType().name());
            } else {
                sql += "shipType='?' ".replace("?", shipBody.getShipType().name());
                oneFilterAlready = true;
            }
        }
        if (shipBody.getProdDate() != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String date = simpleDateFormat.format(shipBody.getProdDate());
            if (oneFilterAlready) {
                sql += ",prodDate=? ".replace("?", date);
            } else {
                sql += "prodDate=? ".replace("?", date);
                oneFilterAlready = true;
            }
        }
        if (shipBody.getUsed() != null) {
            if (oneFilterAlready) {
                sql += ",isUsed=? ".replace("?", shipBody.getUsed().toString());
            } else {
                sql += "isUsed=? ".replace("?", shipBody.getUsed().toString());
                oneFilterAlready = true;
            }
        }
        if (shipBody.getSpeed() != null) {
            if (oneFilterAlready) {
                sql += ",speed=? ".replace("?", String.valueOf(shipBody.getSpeed()));
            } else {
                sql += "speed=? ".replace("?", String.valueOf(shipBody.getSpeed()));
                oneFilterAlready = true;
            }
        }
        if (shipBody.getCrewSize() != null) {
            if (oneFilterAlready) {
                sql += ",crewSize=? ".replace("?", shipBody.getCrewSize().toString());
            } else {
                sql += "crewSize=? ".replace("?", shipBody.getCrewSize().toString());
                oneFilterAlready = true;
            }
        }

        sql += "WHERE id=?".replace("?", id);


        Query q = entityManager.createNativeQuery(sql);

        try {
            q.executeUpdate();
        } catch (Exception e) {
            return null;
        }
        Ship ship = getShip(id);
        ship.setRating(ShipController.countRating(ship));
        sql = "UPDATE ship SET rating=? WHERE id=?";
        q = entityManager.createNativeQuery(sql);
        q.setParameter(1, ship.getRating());
        q.setParameter(2, id);
        q.executeUpdate();
        return ship;

    }

    @Override
    public Ship getShip(String id) {

        String sql = "SELECT * from ship WHERE id = ?";
        Query q = entityManager.createNativeQuery(sql);
        q.setParameter(1, id);
        Object[] o = new Object[0];
        try {
            o = (Object[]) q.getSingleResult();
        } catch (Exception e) {
            return null;
        }
        ShipMapper shipMapper = new ShipMapper();
        Ship ship = shipMapper.readShip(o);
        return ship;
    }

    @Override
    @Transactional
    public Ship createShip(Ship ship) {
        String sql = "INSERT INTO ship (id,name,planet,shipType,prodDate,isUsed,speed,crewSize,rating) VALUES (?,?,?,?,?,?,?,?,?)";
        Query q = entityManager.createNativeQuery(sql);
        ship.setId(getShipsCount(new HashMap<String, String>()).longValue() + 1);

        q.setParameter(1, ship.getId());
        q.setParameter(2, ship.getName());
        q.setParameter(3, ship.getPlanet());
        q.setParameter(4, ship.getShipType().toString());
        q.setParameter(5, ship.getProdDate());
        q.setParameter(6, ship.getUsed());
        q.setParameter(7, ship.getSpeed());
        q.setParameter(8, ship.getCrewSize());
        q.setParameter(9, ship.getRating());

        q.executeUpdate();

        return ship;
    }


    @Override
    public Integer getShipsCount(Map<String, String> allParams) {
        List result = null;
        String sql = "SELECT * FROM ship";
        int where = 0;
        int count = 0;
        String part1Between = "2800/01/01";
        String part2Between = "3019/12/31";

        Set<String> whereParams = new HashSet<>();
        whereParams.add("name");
        whereParams.add("planet");
        whereParams.add("shipType");
        whereParams.add("isUsed");
        whereParams.add("after");
        whereParams.add("before");
        whereParams.add("minSpeed");
        whereParams.add("maxSpeed");
        whereParams.add("minCrewSize");
        whereParams.add("maxCrewSize");
        whereParams.add("minRating");
        whereParams.add("maxRating");

        for (String s :
                allParams.keySet()) {
            for (String ss :
                    whereParams) {
                if (ss.equals(s)) {
                    where++;
                }
            }
        }

        if (where > 0) {
            sql = sql + " WHERE ";
        }

        if (allParams.containsKey("name")) {
            sql = sql + "name LIKE '%:name%'".replace(":name", allParams.get("name"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }
        if (allParams.containsKey("planet")) {

            sql = sql + "planet LIKE '%:planet%'".replace(":planet", allParams.get("planet"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("shipType")) {

            sql = sql + "shipType=':shipType'".replace(":shipType", allParams.get("shipType"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("isUsed")) {

            sql = sql + "isUsed=:isUsed".replace(":isUsed", allParams.get("isUsed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("after")) {
            Date date = new Date(Long.parseLong(allParams.get("after")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String after = simpleDateFormat.format(date);

            sql = sql + "prodDate >= :after".replace(":after", after);
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("before")) {
            Date date = new Date(Long.parseLong(allParams.get("before")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String before = simpleDateFormat.format(date);

            sql = sql + "prodDate <= :before".replace(":before", before);
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minSpeed")) {

            sql = sql + "speed >= ':minSpeed'".replace(":minSpeed", allParams.get("minSpeed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("maxSpeed")) {

            sql = sql + "speed <= ':maxSpeed'".replace(":maxSpeed", allParams.get("maxSpeed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minCrewSize")) {

            sql = sql + "crewSize >= ':minCrewSize'".replace(":minCrewSize", allParams.get("minCrewSize"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("maxCrewSize")) {

            sql = sql + "crewSize <= ':maxCrewSize'".replace(":maxCrewSize", allParams.get("maxCrewSize"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minRating")) {

            sql = sql + "rating >= ':minRating'".replace(":minRating", allParams.get("minRating"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }
        if (allParams.containsKey("maxRating")) {

            sql = sql + "rating <= ':maxRating'".replace(":maxRating", allParams.get("maxRating"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        return entityManager.createNativeQuery(sql).getResultList().size();

    }

    @Autowired
    public ShipDaoImpl(EntityManager entityManager, LocalContainerEntityManagerFactoryBean entityManagerFactoryBean, PlatformTransactionManager transactionManager) {
        this.entityManager = entityManager;
        this.entityManagerFactoryBean = entityManagerFactoryBean;

        this.transactionManager = transactionManager;
    }


    @Override
    public List<Ship> getShipsList(Map<String, String> allParams) {
        List result = null;
        String sql = "SELECT * FROM ship";
        int where = 0;
        int count = 0;
        String part1Between = "2800/01/01";
        String part2Between = "3019/12/31";

        Set<String> whereParams = new HashSet<>();
        whereParams.add("name");
        whereParams.add("planet");
        whereParams.add("shipType");
        whereParams.add("isUsed");
        whereParams.add("after");
        whereParams.add("before");
        whereParams.add("minSpeed");
        whereParams.add("maxSpeed");
        whereParams.add("minCrewSize");
        whereParams.add("maxCrewSize");
        whereParams.add("minRating");
        whereParams.add("maxRating");

        for (String s :
                allParams.keySet()) {
            for (String ss :
                    whereParams) {
                if (ss.equals(s)) {
                    where++;
                }
            }
        }

        if (where > 0) {
            sql = sql + " WHERE ";
        }

        if (allParams.containsKey("name")) {
            sql = sql + "name LIKE '%:name%'".replace(":name", allParams.get("name"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }
        if (allParams.containsKey("planet")) {

            sql = sql + "planet LIKE '%:planet%'".replace(":planet", allParams.get("planet"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("shipType")) {

            sql = sql + "shipType=':shipType'".replace(":shipType", allParams.get("shipType"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("isUsed")) {

            sql = sql + "isUsed=:isUsed".replace(":isUsed", allParams.get("isUsed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }
        }

        if (allParams.containsKey("after")) {
            Date date = new Date(Long.parseLong(allParams.get("after")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String after = simpleDateFormat.format(date);

            sql = sql + "prodDate >= :after".replace(":after", after);
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("before")) {
            Date date = new Date(Long.parseLong(allParams.get("before")));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
            String before = simpleDateFormat.format(date);

            sql = sql + "prodDate <= :before".replace(":before", before);
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minSpeed")) {

            sql = sql + "speed >= ':minSpeed'".replace(":minSpeed", allParams.get("minSpeed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("maxSpeed")) {

            sql = sql + "speed <= ':maxSpeed'".replace(":maxSpeed", allParams.get("maxSpeed"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minCrewSize")) {

            sql = sql + "crewSize >= ':minCrewSize'".replace(":minCrewSize", allParams.get("minCrewSize"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("maxCrewSize")) {

            sql = sql + "crewSize <= ':maxCrewSize'".replace(":maxCrewSize", allParams.get("maxCrewSize"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("minRating")) {

            sql = sql + "rating >= ':minRating'".replace(":minRating", allParams.get("minRating"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }
        if (allParams.containsKey("maxRating")) {

            sql = sql + "rating <= ':maxRating'".replace(":maxRating", allParams.get("maxRating"));
            count++;
            if (count != where) {
                sql = sql + " AND ";
            }

        }

        if (allParams.containsKey("order")) {
            sql = sql + " ORDER BY " + ShipOrder.valueOf(allParams.get("order")).getFieldName();
        }

        if (allParams.containsKey("pageNumber")) {
            result = entityManager.createNativeQuery(sql).getResultList();

            int pageSize = 3;
            if (allParams.containsKey("pageSize")) {
                pageSize = Integer.parseInt(allParams.get("pageSize"));
            }
            int pageNumber = Integer.parseInt(allParams.get("pageNumber")) + 1;

            int toIndex = pageNumber * pageSize > result.size() ? result.size() : pageNumber * pageSize;
            int fromIndex = pageNumber * pageSize - pageSize;
            result = result.subList(fromIndex, toIndex);

        } else if (allParams.containsKey("pageSize")) {
            result = entityManager.createNativeQuery(sql).getResultList();
            int pageSize = Integer.parseInt(allParams.get("pageSize"));
            int pageNumber = 1;
            int toIndex = pageNumber * pageSize;
            int fromIndex = toIndex - pageSize;
            result = result.subList(fromIndex, toIndex);

        } else {
            result = entityManager.createNativeQuery(sql).getResultList();

            int pageSize = 3;
            int pageNumber = 1;
            int toIndex = pageNumber * pageSize > result.size() ? result.size() : pageNumber * pageSize;
            int fromIndex = pageNumber * pageSize - pageSize;
            result = result.subList(fromIndex, toIndex);
        }


        ShipMapper shipMapper = new ShipMapper();
        if (result == null) {
            result = entityManager.createNativeQuery(sql).getResultList();
        }
        List<Ship> ships = new ArrayList<>();
        for (int i = 0; i < result.size(); i++) {
            ships.add(shipMapper.readShip((Object[]) result.get(i)));
        }


        return ships;
    }
}
