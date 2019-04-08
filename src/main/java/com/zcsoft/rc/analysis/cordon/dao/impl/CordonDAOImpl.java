package com.zcsoft.rc.analysis.cordon.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.zcsoft.rc.analysis.app.dao.impl.CubeMongoDBDAOImpl;
import com.zcsoft.rc.analysis.cordon.dao.CordonDAO;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class CordonDAOImpl extends CubeMongoDBDAOImpl implements CordonDAO {

    @Override
    protected MongoCollection<Document> getCollection() {
        return getMongoDatabase().getCollection("rc.cordon");
    }

    @Override
    public String near(String fieldName, double longitude, double latitude, double maxDistance, double minDistance) {

        Point point = new Point(new Position(longitude, latitude));
        Document document = getCollection().find(Filters.near(
                fieldName
                ,point
                ,maxDistance
                ,minDistance
        )).first();

        if(document == null) {
            return null;
        }

        return document.get("id", String.class);
    }
}
