package com.migu.service.search;

import java.io.IOException;
import java.util.List;

import com.migu.service.ServiceMultiResult;
import com.migu.service.ServiceResult;
import com.migu.web.form.MapSearch;
import com.migu.web.form.RentSearch;

/**
 * 检索接口
 * Created by 瓦力.
 */
public interface ISearchService {
    /**
     * 索引目标房源
     * @param houseId
     * @throws IOException 
     * @throws Exception 
     */
    void index(Long houseId) throws IOException, Exception;

    /**
     * 移除房源索引
     * @param houseId
     */
    void remove(Long houseId);


    /**
     * 查询房源接口
     * @param rentSearch
     * @return
     */
    ServiceMultiResult<Long> query(RentSearch rentSearch);

    /**
     * 获取补全建议关键词
     */
    ServiceResult<List<String>> suggest(String prefix);

    /**
     * 聚合特定小区的房间数
     */
    ServiceResult<Long> aggregateDistrictHouse(String cityEnName, String regionEnName, String district);

    /**
     * 聚合城市数据
     * @param cityEnName
     * @return
     */
    ServiceMultiResult<HouseBucketDTO> mapAggregate(String cityEnName);

    /**
     * 城市级别查询
     * @return
     */
    ServiceMultiResult<Long> mapQuery(String cityEnName, String orderBy,
                                      String orderDirection, int start, int size);
    /**
     * 精确范围数据查询
     * @param mapSearch
     * @return
     */
    ServiceMultiResult<Long> mapQuery(MapSearch mapSearch);
}
