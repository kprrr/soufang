package com.migu.service;

import com.migu.web.dto.HouseDTO;
import com.migu.web.form.HouseForm;

/**
 * 房屋管理服务接口
 * @author Lee
 *
 */
public interface IHouseService {
	  /**
     * 新增
     * @param houseForm
     * @return
     */
    ServiceResult<HouseDTO> save(HouseForm houseForm);

//    ServiceResult update(HouseForm houseForm);
//
//    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);
}
