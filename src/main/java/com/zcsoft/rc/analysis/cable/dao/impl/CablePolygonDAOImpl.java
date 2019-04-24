package com.zcsoft.rc.analysis.cable.dao.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;
import com.zcsoft.rc.analysis.app.dao.impl.CubeMongoDBDAOImpl;
import com.zcsoft.rc.analysis.cable.dao.CablePolygonDAO;
import org.bson.Document;
import org.springframework.stereotype.Repository;

@Repository
public class CablePolygonDAOImpl extends CubeMongoDBDAOImpl implements CablePolygonDAO {

    @Override
    protected MongoCollection<Document> getCollection() {
        return getMongoDatabase().getCollection("rc.cablePolygon");
    }

    public String intersects(String fieldName, double longitude, double latitude) {
        Point point = new Point(new Position(longitude, latitude));

        ;

        Document document = getCollection().find(Filters.geoIntersects(
                fieldName
                ,point
        )).first();

        if(document == null) {
            return null;
        }

        return document.get("id", String.class);
    }

}
