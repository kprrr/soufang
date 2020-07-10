package com.migu.config;

import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by 瓦力.
 */
@Configuration
public class ElasticSearchConfig {
//    @Value("${elasticsearch.host}")
//    private String esHost;
//
//    @Value("${elasticsearch.port}")
//    private int esPort;
//
//    @Value("${elasticsearch.cluster.name}")
//    private String esName;

    @Bean
    public RestHighLevelClient esClient() throws UnknownHostException {
    	RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.26.3", 9200, "http")));
        return client;
//        Settings settings = Settings.builder()
//                .put("cluster.name", this.esName)
////                .put("cluster.name", "elasticsearch")
//                .put("client.transport.sniff", true)
//                .build();
//
//        InetSocketTransportAddress master = new InetSocketTransportAddress(
//            InetAddress.getByName(esHost), esPort
////          InetAddress.getByName("10.99.207.76"), 8999
//        );
//
//        TransportClient client = new PreBuiltTransportClient(settings)
//                .addTransportAddress(master);
//
//        return client;
    }
}
