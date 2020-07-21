package com.migu.service;

import java.util.Date;

import org.springframework.data.util.Pair;

import com.migu.base.HouseSubscribeStatus;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.HouseSubscribeDTO;
import com.migu.web.form.DatatableSearch;
import com.migu.web.form.HouseForm;
import com.migu.web.form.MapSearch;
import com.migu.web.form.RentSearch;

/**
 * 房屋管理服务接口
 * 
 * @author Lee
 *
 */
public interface IHouseService {
	/**
	 * 新增
	 * 
	 * @param houseForm
	 * @return
	 */
	ServiceResult<HouseDTO> save(HouseForm houseForm);

	/**
	 * 查询完整房屋信息
	 * 
	 * @param id
	 * @return
	 */
	ServiceResult<HouseDTO> findCompleteOne(Long id);

//    ServiceResult update(HouseForm houseForm);
//
	ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

	ServiceResult updateStatus(Long id, int status);

	ServiceMultiResult<HouseDTO> query(RentSearch rentSearch);

	/**
	 * 全地图查询
	 * 
	 * @param mapSearch
	 * @return
	 */
	ServiceMultiResult<HouseDTO> wholeMapQuery(MapSearch mapSearch);

	/**
	 * 精确范围数据查询
	 * 
	 * @param mapSearch
	 * @return
	 */
	ServiceMultiResult<HouseDTO> boundMapQuery(MapSearch mapSearch);

	/**
	 * 加入预约清单
	 * 
	 * @param houseId
	 * @return
	 */
	ServiceResult addSubscribeOrder(Long houseId);

	/**
	 * 获取对应状态的预约列表
	 */
	ServiceMultiResult<Pair<HouseDTO, HouseSubscribeDTO>> querySubscribeList(HouseSubscribeStatus status, int start,
			int size);

	/**
	 * 预约看房时间
	 * 
	 * @param houseId
	 * @param orderTime
	 * @param telephone
	 * @param desc
	 * @return
	 */
	ServiceResult subscribe(Long houseId, Date orderTime, String telephone, String desc);

	/**
	 * 取消预约
	 * 
	 * @param houseId
	 * @return
	 */
	ServiceResult cancelSubscribe(Long houseId);
}
