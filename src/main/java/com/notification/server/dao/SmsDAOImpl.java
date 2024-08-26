package com.notification.server.dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.notification.server.constants.GetMessagePhoneBody;
import com.notification.server.constants.GetMessageTextBody;
import com.notification.server.entity.Sms_requests;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SmsDAOImpl implements SmsDAO{

    //Entity manager is automatically created by spring boot and we inject it here
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    private static final String KEY = "SMS:";
    public SmsDAOImpl() {

    }

    @Override
    public Sms_requests createSmsRecord(Sms_requests sms_request) {

        // Create a query for a sms record
        // save record
        // return the dbRequest
        return entityManager.merge(sms_request);
    }
    @Override
    public Sms_requests findById(String id) {
        return entityManager.find(Sms_requests.class, Integer.parseInt(id));
    }

    @Override
    public String addBlacklist(String[] phoneNumbers) {
        for (String phoneNumber : phoneNumbers) {
            redisTemplate.opsForValue().set(KEY+phoneNumber, "Blacklisted");
        }
        return "DataSaved";
    }

    @Override
    public String removeBlacklist(String[] phoneNumbers) {
        for (String phoneNumber : phoneNumbers) {
            redisTemplate.delete(KEY+phoneNumber);
        }
        return "DataRemoved";
    }

    @Override
    public List<String> getBlacklist() {
        List<String> keys = new ArrayList<>();
        Cursor<String> cursor = redisTemplate.scan(ScanOptions.scanOptions().match(KEY+'*').build());
        while (cursor.hasNext()) {
            String key = cursor.next();
            keys.add(key);
        }
        return keys;
    }

    @Override
    public Boolean checkBlacklist(String phoneNumber) {
        return redisTemplate.hasKey(KEY+phoneNumber);
    }

    @Override
    public Sms_requests updateSmsRecord(Sms_requests smsRequest) {
        entityManager.merge(smsRequest);
        return findById(String.valueOf(smsRequest.getId()));
    }

    @Override
    public List<Object> getAllMessagesWithPhone(GetMessagePhoneBody body) throws IOException {

        SearchResponse<ObjectNode> response = elasticsearchClient.search(s -> s
                        .index("requests")
                        .query(q -> q
                                .bool(b -> b
                                        .filter(f -> f
                                                .range(r -> r
                                                        .term(v -> v
                                                                .field("created_at")
                                                                .gte(body.startTime())   // start of the time range
                                                                .lte(body.endTime())     // end of the time range
                                                        )
                                                )
                                        )
                                        .must(m -> m
                                                .match(t -> t
                                                        .field("phone_number")
                                                        .query(body.phoneNumber())
                                                )
                                        )
                                )
                        )
                        .from(body.from() * body.size())  // pagination: starting index
                        .size(body.size()),                // pagination: number of results per page
                ObjectNode.class
        );
        return getObjects(response);
    }

    @Override
    public List<Object> getAllMessagesWithText(GetMessageTextBody body) throws IOException {
        SearchResponse<ObjectNode> response = elasticsearchClient.search(s -> s
                        .index("requests")
                        .query(q -> q
                                .bool(b -> b
                                        .must(m -> m
                                                .match(t -> t
                                                        .field("message")
                                                        .query(body.text())
                                                )
                                        )
                                )
                        )
                        .from(body.from() * body.size())  // pagination: starting index
                        .size(body.size()),                // pagination: number of results per page
                ObjectNode.class
        );
        return getObjects(response);
    }

    private List<Object> getObjects(SearchResponse<ObjectNode> response) {
        List<Hit<ObjectNode>> hits = response.hits().hits();
        System.out.println(response);
        List<Object> resultHits = new ArrayList<>();
        for (Hit<ObjectNode> hit: hits) {
            resultHits.add(hit.source());
        }
        return resultHits;
    }
}
