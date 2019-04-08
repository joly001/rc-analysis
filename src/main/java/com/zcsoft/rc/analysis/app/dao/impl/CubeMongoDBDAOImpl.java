package com.zcsoft.rc.analysis.app.dao.impl;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zcsoft.rc.analysis.app.dao.ICubeMongoDBDAO;
import org.bson.Document;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;

/**
 * CubeMongoDBDAOImpl
 *
 * @author Joly
 * @version v1.0
 * @since v1.0
 * 2017/9/29 下午5:23
 */
public abstract class CubeMongoDBDAOImpl implements ICubeMongoDBDAO, InitializingBean {

    private String host;
    private int port;
    private String databaseName;
    private MongoDatabase mongoDatabase;

    @Value("${dataSource.mongodb.host}")
    public void setHost(String host) {
        this.host = host;
    }
    @Value("${dataSource.mongodb.port}")
    public void setPort(int port) {
        this.port = port;
    }
    @Value("${dataSource.mongodb.databaseName}")
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }

    abstract protected MongoCollection<Document> getCollection();

    protected void initMongoDB() {
        MongoClient mongoClient = new MongoClient(host , port);
        mongoDatabase = mongoClient.getDatabase(databaseName);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initMongoDB();
    }

}
