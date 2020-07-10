package com.migu.service.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migu.base.HouseIndexKey;
import com.migu.entity.House;
import com.migu.entity.HouseDetail;
import com.migu.entity.HouseTag;
import com.migu.repository.HouseDetailRepository;
import com.migu.repository.HouseRepository;
import com.migu.repository.HouseTagRepository;

/**
 * Created by 瓦力.
 */
@Service
public class SearchServiceImpl implements ISearchService {
    private static final Logger logger = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME = "soufang";

    private static final String INDEX_TYPE = "house";

    private static final String INDEX_TOPIC = "house_build";
    
    @Autowired
    private RestHighLevelClient esClient;
    
    @Autowired
    private HouseRepository houseRepository;
    
    @Autowired
    private HouseDetailRepository detailRepository;
    
    @Autowired
    private HouseTagRepository tagRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ModelMapper modelMapper;

	@Override
	public void index(Long houseId) throws Exception {
		House house = houseRepository.findById(houseId).get();
		if(house == null) {
			logger.error("Index house {} dose not exist!", houseId);
			return;
		}
		HouseIndexTemplate indexTemplate = new HouseIndexTemplate();
		modelMapper.map(house, indexTemplate);
		
		HouseDetail detail = detailRepository.findByHouseId(houseId);
        if (detail == null) {
            // TODO 异常情况
        }
        modelMapper.map(detail, indexTemplate);
        List<HouseTag> tags = tagRepository.findAllByHouseId(houseId);
        if (tags != null && !tags.isEmpty()) {
            List<String> tagStrings = new ArrayList<>();
            tags.forEach(houseTag -> tagStrings.add(houseTag.getName()));
            indexTemplate.setTags(tagStrings);
        }

        //根据条件查询
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId));
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

        boolean success;
        long totalHit = searchResponse.getHits().getTotalHits().value;
        if (totalHit == 0) {
            success = create(indexTemplate);
        } else if (totalHit == 1) {
            String esId = searchResponse.getHits().getAt(0).getId();
            success = update(esId, indexTemplate);
        } else {
            success = deleteAndCreate(totalHit, indexTemplate);
        }
        
        if (!success) {
        	logger.debug("Index not success with house " + houseId);
        } else {
            logger.debug("Index success with house " + houseId);

        }
	}

	@Override
	public void remove(Long houseId) {
		// TODO Auto-generated method stub
		
	}

	private boolean create(HouseIndexTemplate indexTemplate) throws IOException {
//        if (!updateSuggest(indexTemplate)) {
//            return false;
//        }

        try {
        	IndexRequest request = new IndexRequest(INDEX_NAME); //索引
//        	request.id(indexTemplate.getHouseId()); //文档id
        	request.source(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON); //以字符串形式提供的文档源
        	IndexResponse indexResponse = esClient.index(request, RequestOptions.DEFAULT);
//            IndexResponse response = this.esClient.prepareIndex(INDEX_NAME, INDEX_TYPE)
//                    .setSource(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("Create index with house: " + indexTemplate.getHouseId());
            if (indexResponse.status() == RestStatus.CREATED) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    private boolean update(String esId, HouseIndexTemplate indexTemplate) throws Exception{
//        if (!updateSuggest(indexTemplate)) {
//            return false;
//        }

        try {
        	UpdateRequest request = new UpdateRequest(
        	        INDEX_NAME, //索引
        	        esId)//文档id
        			.doc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);   
        	UpdateResponse updateResponse = esClient.update(
        	        request, RequestOptions.DEFAULT);
//            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME, INDEX_TYPE, esId).setDoc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON).get();

            logger.debug("Update index with house: " + indexTemplate.getHouseId());
            if (updateResponse.status() == RestStatus.OK) {
                return true;
            } else {
                return false;
            }
        } catch (JsonProcessingException e) {
            logger.error("Error to index house " + indexTemplate.getHouseId(), e);
            return false;
        }
    }

    private boolean deleteAndCreate(long totalHit, HouseIndexTemplate indexTemplate) throws IOException {
//        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
//                .newRequestBuilder(esClient)
//                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId()))
//                .source(INDEX_NAME);
//
//        logger.debug("Delete by query for house: " + builder);
//
//        BulkByScrollResponse response = builder.get();
//        long deleted = response.getDeleted();
//        if (deleted != totalHit) {
//            logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
//            return false;
//        } else {
//            return create(indexTemplate);
//        }
        
        DeleteByQueryRequest request =
                new DeleteByQueryRequest("source1", "source2"); //在一组索引上创建DeleteByQueryRequest。
        request.setQuery(new TermQueryBuilder(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId())); //仅复制字段用户设置为kimchy的文档
        BulkByScrollResponse bulkResponse =
        		esClient.deleteByQuery(request, RequestOptions.DEFAULT);
        long deleted = bulkResponse.getDeleted();
        if (deleted != totalHit) {
            logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
            return false;
        } else {
            return create(indexTemplate);
        }
    
    }

}
