package com.notification.server.dao;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.notification.server.constants.GetMessagePhoneBody;
import com.notification.server.constants.GetMessageTextBody;
import com.notification.server.entity.Sms_requests;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class SmsDAOImplTest {

    @InjectMocks
    private SmsDAOImpl smsDAO;

    @Mock
    private EntityManager entityManager;

    @MockBean
    private JedisConnectionFactory jedisConnectionFactory;

    @MockBean
    private ValueOperations<String, String> valueOperations;
    @MockBean
    RedisTemplate<String, String> redisTemplate;
    @MockBean
    StringRedisSerializer stringRedisSerializer;
    @Mock
    private ElasticsearchClient elasticsearchClient;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(stringRedisSerializer);
        redisTemplate.setEnableTransactionSupport(true);
        redisTemplate.afterPropertiesSet();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testCreateSmsRecord() {
        Sms_requests smsRequest = new Sms_requests();
        when(entityManager.merge(smsRequest)).thenReturn(smsRequest);

        Sms_requests result = smsDAO.createSmsRecord(smsRequest);

        assertNotNull(result);
        verify(entityManager, times(1)).merge(smsRequest);
    }

    @Test
    void testFindById() {
        Sms_requests smsRequest = new Sms_requests("PENDING", "12345678", "Hey", new Date());
        smsRequest.setId(1);
        when(entityManager.find(Sms_requests.class, 1)).thenReturn(smsRequest);
        Sms_requests result = smsDAO.findById("1");
        System.out.println(result);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(entityManager, times(1)).find(Sms_requests.class, 1);
    }

    @Test
    void testAddBlacklist() {
        String[] phoneNumbers = {"1234567890", "0987654321"};
        String result = smsDAO.addBlacklist(phoneNumbers);

        assertEquals("DataSaved", result);
        // Verify interactions
        verify(valueOperations, times(2)).set(
                argThat(key -> key.equals("SMS:" + "1234567890") || key.equals("SMS:" + "0987654321")),
                eq("Blacklisted")
        );
    }

    @Test
    void testRemoveBlacklist() {
        String[] phoneNumbers = {"1234567890", "0987654321"};
        when(redisTemplate.delete(anyString())).thenReturn(true);

        String result = smsDAO.removeBlacklist(phoneNumbers);

        assertEquals("DataRemoved", result);
        verify(redisTemplate, times(2)).delete(anyString());
    }

    @Test
    void testGetBlacklist() {
        List<String> keys = List.of("SMS:1234567890", "SMS:0987654321");
        Cursor<String> cursor = mock(Cursor.class);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(keys.get(0), keys.get(1));
        when(redisTemplate.scan(any(ScanOptions.class))).thenReturn(cursor);

        List<String> result = smsDAO.getBlacklist();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("SMS:1234567890"));
        assertTrue(result.contains("SMS:0987654321"));
    }

    @Test
    void testCheckBlacklist() {
        when(redisTemplate.hasKey("SMS:1234567890")).thenReturn(true);

        Boolean result = smsDAO.checkBlacklist("1234567890");

        assertTrue(result);
        verify(redisTemplate, times(1)).hasKey("SMS:1234567890");
    }

    @Test
    void testUpdateSmsRecord() {
        Sms_requests smsRequest = new Sms_requests();
        smsRequest.setId(1);
        when(entityManager.merge(smsRequest)).thenReturn(smsRequest);
        when(entityManager.find(Sms_requests.class, 1)).thenReturn(smsRequest);

        Sms_requests result = smsDAO.updateSmsRecord(smsRequest);

        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(entityManager, times(1)).merge(smsRequest);
        verify(entityManager, times(1)).find(Sms_requests.class, 1);
    }

    @Test
    void testGetAllMessagesWithPhone() throws IOException {
        GetMessagePhoneBody body = new GetMessagePhoneBody("1234567890","2024-01-01T00:00:00","2024-01-01T23:59:59",0,10);
        SearchResponse<ObjectNode> response = mock(SearchResponse.class);
        List<Hit<ObjectNode>> hits = new ArrayList<>();
        HitsMetadata hitsMetadata = mock(HitsMetadata.class);
        Hit<ObjectNode> hit = mock(Hit.class);
        ObjectNode source = mock(ObjectNode.class);
        SearchRequest request = new SearchRequest.Builder().build();
        hits.add(hit);
        when(hit.source()).thenReturn(source);
        when(response.hits()).thenReturn(hitsMetadata);
        when(response.hits().hits()).thenReturn(hits);
        when(elasticsearchClient.search(request,ObjectNode.class)).thenReturn(response);

        List<Object> result = smsDAO.getAllMessagesWithPhone(body);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(elasticsearchClient, times(1)).search(request,ObjectNode.class);
    }

    @Test
    void testGetAllMessagesWithText() throws IOException {
        GetMessageTextBody body = new GetMessageTextBody("text",0,10);
        SearchResponse<ObjectNode> response = mock(SearchResponse.class);
        List<Hit<ObjectNode>> hits = new ArrayList<>();
        HitsMetadata hitsMetadata = mock(HitsMetadata.class);
        Hit<ObjectNode> hit = mock(Hit.class);
        ObjectNode source = mock(ObjectNode.class);
        SearchRequest request = new SearchRequest.Builder().build();
        hits.add(hit);
        when(hit.source()).thenReturn(source);
        when(response.hits()).thenReturn(hitsMetadata);
        when(response.hits().hits()).thenReturn(hits);
        when(elasticsearchClient.search(request,ObjectNode.class)).thenReturn(response);

        List<Object> result = smsDAO.getAllMessagesWithText(body);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(elasticsearchClient, times(1)).search(request,ObjectNode.class);
    }
}

