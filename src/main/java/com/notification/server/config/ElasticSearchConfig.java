package com.notification.server.config;


import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig  {

    private static final String SERVER_URL = "https://016aa1b70bb64d1eb82865b342b4bd78.us-central1.gcp.cloud.es.io:443";
    private static final String API_KEY = "UzROYmFwRUJHcnZkVGdsOF8ybks6MGwzbHo5ZU1RRUdwbjJPMTFacXR1Zw==";
    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient restClient = RestClient
                .builder(HttpHost.create(SERVER_URL))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + API_KEY)
                })
                .build();

        // Create the transport with a Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        // And create the API client
        return new ElasticsearchClient(transport);
    }
}
