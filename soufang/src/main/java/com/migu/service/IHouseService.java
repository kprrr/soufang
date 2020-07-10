package com.migu.service;

import com.migu.web.dto.HouseDTO;
import com.migu.web.form.DatatableSearch;
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

    /**
     * 查询完整房屋信息
     * @param id
     * @return
     */
    ServiceResult<HouseDTO> findCompleteOne(Long id);
    
//    ServiceResult update(HouseForm houseForm);
//
    ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody);

	ServiceResult updateStatus(Long id, int status);
}
