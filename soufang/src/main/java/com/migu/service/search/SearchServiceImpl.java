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
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.fielddata.ScriptDocValues.Longs;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.migu.base.HouseIndexKey;
import com.migu.base.HouseSort;
import com.migu.base.RentValueBlock;
import com.migu.entity.House;
import com.migu.entity.HouseDetail;
import com.migu.entity.HouseTag;
import com.migu.repository.HouseDetailRepository;
import com.migu.repository.HouseRepository;
import com.migu.repository.HouseTagRepository;
import com.migu.service.ServiceMultiResult;
import com.migu.service.ServiceResult;
import com.migu.web.form.MapSearch;
import com.migu.web.form.RentSearch;

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
		if (house == null) {
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

		// 根据条件查询
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
//		DeleteRequest request = new DeleteRequest(
//				INDEX_NAME,    //索引
//				String.valueOf(houseId));       //文档id
		DeleteByQueryRequest request = new DeleteByQueryRequest(INDEX_NAME); // 文档id
		request.setQuery(new TermQueryBuilder(HouseIndexKey.HOUSE_ID, String.valueOf(houseId)));
//		DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
//                .newRequestBuilder(esClient)
//                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID, houseId))
//                .source(INDEX_NAME);

		BulkByScrollResponse deleteResponse;
		try {
			deleteResponse = esClient.deleteByQuery(request, RequestOptions.DEFAULT);
			long deletedDocs = deleteResponse.getDeleted(); // 已删除的文档数
			if (deletedDocs == 0l) {
				logger.debug("Index not found with house: " + houseId);
			}
			logger.debug("Delete index with house: " + houseId);
//			if(deleteResponse.getStatus() == Status.NOT_FOUND) {
//				logger.debug("Index not found with house: " + houseId);
//			}
//			if (deleteResponse.status() == RestStatus.OK) {
//				logger.debug("Delete index with house: " + houseId);
//	        }
		} catch (IOException e) {
			logger.error("Error to index house " + houseId, e);
//			e.printStackTrace();
		}

	}

	/**
	 * 创建文档
	 * 
	 * @param indexTemplate
	 * @return
	 * @throws IOException
	 */
	private boolean create(HouseIndexTemplate indexTemplate) throws IOException {
//        if (!updateSuggest(indexTemplate)) {
//            return false;
//        }

		try {
			IndexRequest request = new IndexRequest(INDEX_NAME); // 索引
//        	request.id(indexTemplate.getHouseId()); //文档id
			request.source(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON); // 以字符串形式提供的文档源
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

	private boolean update(String esId, HouseIndexTemplate indexTemplate) throws Exception {
//        if (!updateSuggest(indexTemplate)) {
//            return false;
//        }

		try {
			UpdateRequest request = new UpdateRequest(INDEX_NAME, // 索引
					esId)// 文档id
							.doc(objectMapper.writeValueAsBytes(indexTemplate), XContentType.JSON);
			UpdateResponse updateResponse = esClient.update(request, RequestOptions.DEFAULT);
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

		DeleteByQueryRequest request = new DeleteByQueryRequest("source1", "source2"); // 在一组索引上创建DeleteByQueryRequest。
		request.setQuery(new TermQueryBuilder(HouseIndexKey.HOUSE_ID, indexTemplate.getHouseId())); // 仅复制字段用户设置为kimchy的文档
		BulkByScrollResponse bulkResponse = esClient.deleteByQuery(request, RequestOptions.DEFAULT);
		long deleted = bulkResponse.getDeleted();
		if (deleted != totalHit) {
			logger.warn("Need delete {}, but {} was deleted!", totalHit, deleted);
			return false;
		} else {
			return create(indexTemplate);
		}

	}

	@Override
	public ServiceMultiResult<Long> query(RentSearch rentSearch) {
		BoolQueryBuilder boolQuery = new BoolQueryBuilder();
		boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, rentSearch.getCityEnName()));

		if (rentSearch.getRegionEnName() != null && !"*".equals(rentSearch.getRegionEnName())) {
			boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, rentSearch.getRegionEnName()));
		}

		// 面积条件
		RentValueBlock area = RentValueBlock.matchArea(rentSearch.getAreaBlock());
		if (!RentValueBlock.ALL.equals(area)) {
			RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(HouseIndexKey.AREA);
			if (area.getMax() > 0) {
				rangeQueryBuilder.lte(area.getMax());
			}
			if (area.getMin() > 0) {
				rangeQueryBuilder.gte(area.getMin());
			}
			boolQuery.filter(rangeQueryBuilder);
		}

		// 价格条件
		RentValueBlock price = RentValueBlock.matchPrice(rentSearch.getPriceBlock());
		if (!RentValueBlock.ALL.equals(price)) {
			RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery(HouseIndexKey.PRICE);
			if (price.getMax() > 0) {
				rangeQuery.lte(price.getMax());
			}
			if (price.getMin() > 0) {
				rangeQuery.gte(price.getMin());
			}
			boolQuery.filter(rangeQuery);
		}

		// 朝向条件
		if (rentSearch.getDirection() > 0) {
			boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.DIRECTION, rentSearch.getDirection()));
		}

		// 租用方式
		if (rentSearch.getRentWay() > -1) {
			boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.RENT_WAY, rentSearch.getRentWay()));
		}

		// 关键词搜索
		boolQuery.must(QueryBuilders.multiMatchQuery(rentSearch.getKeywords(), HouseIndexKey.TITLE,
				HouseIndexKey.TRAFFIC, HouseIndexKey.DISTRICT, HouseIndexKey.ROUND_SERVICE,
				HouseIndexKey.SUBWAY_LINE_NAME, HouseIndexKey.SUBWAY_STATION_NAME));

		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		// 此处为es7新写法
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery)// 设置查询条件
				.sort(HouseSort.getSortKey(rentSearch.getOrderBy()), // 排序条件
						SortOrder.fromString(rentSearch.getOrderDirection()))
				.from(rentSearch.getStart()).size(rentSearch.getSize());
		searchRequest.source(searchSourceBuilder);
		long totalHits = 0;
		List<Long> houseIds = new ArrayList();
		try {
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);// 通过发送初始搜索请求来初始化搜索上下文
			if (searchResponse.status() != RestStatus.OK) {
				logger.warn("Search status is no ok for " + searchRequest.toString());
				return new ServiceMultiResult<>(0, houseIds);
			}

			totalHits = searchResponse.getHits().getTotalHits().value;

			// source:HouseIndexTemplate
			searchResponse.getHits().forEach(x -> {
//				String house = x.getSourceAsString();

//				System.out.println(x.getSourceAsMap());
				houseIds.add(((Number) x.getSourceAsMap().get(HouseIndexKey.HOUSE_ID)).longValue());
			});

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new ServiceMultiResult<>(totalHits, houseIds);
	}

	@Override
	public ServiceResult<List<String>> suggest(String prefix) {
//		CompletionSuggestionBuilder suggestion = SuggestBuilders.completionSuggestion("suggest").prefix(prefix).size(5);
//
//        SuggestBuilder suggestBuilder = new SuggestBuilder();
//        suggestBuilder.addSuggestion("autocomplete", suggestion);
//
//        SearchRequestBuilder requestBuilder = this.esClient.prepareSearch(INDEX_NAME)
//                .setTypes(INDEX_TYPE)
//                .suggest(suggestBuilder);
//        logger.debug(requestBuilder.toString());
//
//        SearchResponse response = requestBuilder.get();
//        Suggest suggest = response.getSuggest();
//        if (suggest == null) {
//            return ServiceResult.of(new ArrayList<>());
//        }
//        Suggest.Suggestion result = suggest.getSuggestion("autocomplete");
//
//        int maxSuggest = 0;
//        Set<String> suggestSet = new HashSet<>();
//
//        for (Object term : result.getEntries()) {
//            if (term instanceof CompletionSuggestion.Entry) {
//                CompletionSuggestion.Entry item = (CompletionSuggestion.Entry) term;
//
//                if (item.getOptions().isEmpty()) {
//                    continue;
//                }
//
//                for (CompletionSuggestion.Entry.Option option : item.getOptions()) {
//                    String tip = option.getText().string();
//                    if (suggestSet.contains(tip)) {
//                        continue;
//                    }
//                    suggestSet.add(tip);
//                    maxSuggest++;
//                }
//            }
//
//            if (maxSuggest > 5) {
//                break;
//            }
//        }
//        List<String> suggests = Lists.newArrayList(suggestSet.toArray(new String[]{}));
//        return ServiceResult.of(suggests);
		return null;
	}

	/**
	 * 更新suggest
	 * 
	 * @param indexTemplate
	 * @return
	 */
	private boolean updateSuggest(HouseIndexTemplate indexTemplate) {
//        AnalyzeRequestBuilder requestBuilder = new AnalyzeRequestBuilder(
//                this.esClient, AnalyzeAction.INSTANCE, INDEX_NAME, indexTemplate.getTitle(),
//                indexTemplate.getLayoutDesc(), indexTemplate.getRoundService(),
//                indexTemplate.getDescription(), indexTemplate.getSubwayLineName(),
//                indexTemplate.getSubwayStationName());
//
//        AnalyzeRequestBuilder rb = new AnalyzeRequestBuilder(, AnalyzeAction.INSTANCE,
//        		INDEX_NAME,"aa");
//        
//        
//        
//        requestBuilder.setAnalyzer("ik_smart");
//
//        
//        AnalyzeResponse response = requestBuilder.get();
//        List<AnalyzeResponse.AnalyzeToken> tokens = response.getTokens();
//        if (tokens == null) {
//            logger.warn("Can not analyze token for house: " + indexTemplate.getHouseId());
//            return false;
//        }
//
//        List<HouseSuggest> suggests = new ArrayList<>();
//        for (AnalyzeResponse.AnalyzeToken token : tokens) {
//            // 排序数字类型 & 小于2个字符的分词结果
//            if ("<NUM>".equals(token.getType()) || token.getTerm().length() < 2) {
//                continue;
//            }
//
//            HouseSuggest suggest = new HouseSuggest();
//            suggest.setInput(token.getTerm());
//            suggests.add(suggest);
//        }
//
//        // 定制化小区自动补全
//        HouseSuggest suggest = new HouseSuggest();
//        suggest.setInput(indexTemplate.getDistrict());
//        suggests.add(suggest);
//
//        indexTemplate.setSuggest(suggests);
		return true;
	}

	@Override
	public ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName))
				.filter(QueryBuilders.termQuery(HouseIndexKey.REGION_EN_NAME, regionEnName))
				.filter(QueryBuilders.termQuery(HouseIndexKey.DISTRICT, district));

		// es7新写法
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery)
				.aggregation(AggregationBuilders.terms(HouseIndexKey.AGG_DISTRICT).field(HouseIndexKey.DISTRICT))
				.size(0);
		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);
		try {
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
//			logger.debug(searchSourceBuilder.toString());

			if (searchResponse.status() == RestStatus.OK) {
				Terms terms = searchResponse.getAggregations().get(HouseIndexKey.AGG_DISTRICT);
				if (terms.getBuckets() != null && !terms.getBuckets().isEmpty()) {
					return ServiceResult.of(terms.getBucketByKey(district).getDocCount());
				}
			} else {
				logger.warn("Failed to Aggregate for " + HouseIndexKey.AGG_DISTRICT);

			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return ServiceResult.of(0L);
	}

	@Override
	public ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

		AggregationBuilder aggBuilder = AggregationBuilders.terms(HouseIndexKey.AGG_REGION)
				.field(HouseIndexKey.REGION_EN_NAME);
		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery).aggregation(aggBuilder);
		SearchRequest searchRequest = new SearchRequest();
		searchRequest.source(searchSourceBuilder);
		List<HouseBucketDTO> buckets = new ArrayList<>();
		try {
			SearchResponse searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);

			if (searchResponse.status() != RestStatus.OK) {
				logger.warn("Aggregate status is not ok for " + searchSourceBuilder);
				return new ServiceMultiResult<>(0, buckets);
			} else {
				Terms terms = searchResponse.getAggregations().get(HouseIndexKey.AGG_REGION);
				for (Terms.Bucket bucket : terms.getBuckets()) {
					buckets.add(new HouseBucketDTO(bucket.getKeyAsString(), bucket.getDocCount()));
				}

				return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits().value, buckets);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	@Override
	public ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy, String orderDirection, int start,
			int size) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, cityEnName));

		SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery)
						.sort(HouseSort.getSortKey(orderBy), SortOrder.fromString(orderDirection))
						.from(start)
						.size(size);
		SearchRequest searchRequest = new SearchRequest();
		List<Long> houseIds = new ArrayList<>();
		searchRequest.source(searchSourceBuilder);
		SearchResponse response=null;
		try {
			response = esClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (response.status() != RestStatus.OK) {
			logger.warn("Search status is not ok for " + searchSourceBuilder);
			return new ServiceMultiResult<>(0, houseIds);
		}
		response.getHits().forEach(x->{
			houseIds.add((Long) x.getSourceAsMap().get(HouseIndexKey.HOUSE_ID));
		});
		
		return new ServiceMultiResult<>(response.getHits().getTotalHits().value, houseIds);
	}
	
	@Override
	public ServiceMultiResult<Long> mapQuery(MapSearch mapSearch) {
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.filter(QueryBuilders.termQuery(HouseIndexKey.CITY_EN_NAME, mapSearch.getCityEnName()));

        boolQuery.filter(
            QueryBuilders.geoBoundingBoxQuery("location")
                .setCorners(
                        new GeoPoint(mapSearch.getLeftLatitude(), mapSearch.getLeftLongitude()),
                        new GeoPoint(mapSearch.getRightLatitude(), mapSearch.getRightLongitude())
                ));
        
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
		searchSourceBuilder.query(boolQuery)
						.sort(HouseSort.getSortKey(mapSearch.getOrderBy()), SortOrder.fromString(mapSearch.getOrderDirection()))
						.from(mapSearch.getStart())
						.size(mapSearch.getSize());
		SearchRequest searchRequest = new SearchRequest(INDEX_NAME);
		searchRequest.source(searchSourceBuilder);
		List<Long> houseIds = new ArrayList<>();
		SearchResponse searchResponse = null;
		try {
			searchResponse = esClient.search(searchRequest, RequestOptions.DEFAULT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if (RestStatus.OK != searchResponse.status()) {
            logger.warn("Search status is not ok for " + searchSourceBuilder);
            return new ServiceMultiResult<>(0, houseIds);
        }
        searchResponse.getHits().forEach(x->{
			houseIds.add((Long) x.getSourceAsMap().get(HouseIndexKey.HOUSE_ID));
		});
        return new ServiceMultiResult<>(searchResponse.getHits().getTotalHits().value, houseIds);
	}
}
