package com.migu.web.controller.houser;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.migu.base.ApiResponse;
import com.migu.entity.SupportAddress;
import com.migu.service.IAddressService;
import com.migu.service.IHouseService;
import com.migu.service.IUserService;
import com.migu.service.ServiceMultiResult;
import com.migu.service.ServiceResult;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.SubwayDTO;
import com.migu.web.dto.SubwayStationDTO;
import com.migu.web.dto.SupportAddressDTO;
import com.migu.web.dto.UserDTO;

@Controller
public class HouseController {

	@Autowired
	private IAddressService addressService;
	
	@Autowired
	private IHouseService houseService;

	@Autowired
	private IUserService userService;
	
	
	/**
	 * 获取城市列表
	 * 
	 * @return
	 */
	@GetMapping("address/support/cities")
	@ResponseBody
	public ApiResponse getSupportCities() {
		ServiceMultiResult<SupportAddressDTO> result = addressService.findAllCities();
		if (result.getResultSize() == 0) {
			return ApiResponse.ofSuccess(ApiResponse.Status.NOT_FOUND);
		}
		return ApiResponse.ofSuccess(result.getResult());
	}

	/**
	 * 获取对应城市支持区域列表
	 * 
	 * @param cityEnName
	 * @return
	 */
	@GetMapping("address/support/regions")
	@ResponseBody
	public ApiResponse getSupportRegions(@RequestParam(name = "city_name") String cityEnName) {
		ServiceMultiResult<SupportAddressDTO> addressResult = addressService.findAllRegionsByCityName(cityEnName);
		if (addressResult.getResult() == null || addressResult.getTotal() < 1) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
		}
		return ApiResponse.ofSuccess(addressResult.getResult());
	}

	/**
	 * 获取具体城市所支持的地铁线路
	 * 
	 * @param cityEnName
	 * @return
	 */
	@GetMapping("address/support/subway/line")
	@ResponseBody
	public ApiResponse getSupportSubwayLine(@RequestParam(name = "city_name") String cityEnName) {
		List<SubwayDTO> subways = addressService.findAllSubwayByCity(cityEnName);
		if (subways.isEmpty()) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
		}

		return ApiResponse.ofSuccess(subways);
	}

	/**
	 * 获取对应地铁线路所支持的地铁站点
	 * 
	 * @param subwayId
	 * @return
	 */
	@GetMapping("address/support/subway/station")
	@ResponseBody
	public ApiResponse getSupportSubwayStation(@RequestParam(name = "subway_id") Long subwayId) {
		List<SubwayStationDTO> stationDTOS = addressService.findAllStationBySubway(subwayId);
		if (stationDTOS.isEmpty()) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_FOUND);
		}

		return ApiResponse.ofSuccess(stationDTOS);
	}
	
	@GetMapping("rent/house/show/{id}")
    public String show(@PathVariable(value = "id") Long houseId,
                       Model model) {
        if (houseId <= 0) {
            return "404";
        }

        ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(houseId);
        if (!serviceResult.isSuccess()) {
            return "404";
        }

        HouseDTO houseDTO = serviceResult.getResult();
        Map<SupportAddress.Level, SupportAddressDTO>
                addressMap = addressService.findCityAndRegion(houseDTO.getCityEnName(), houseDTO.getRegionEnName());

        SupportAddressDTO city = addressMap.get(SupportAddress.Level.CITY);
        SupportAddressDTO region = addressMap.get(SupportAddress.Level.REGION);

        model.addAttribute("city", city);
        model.addAttribute("region", region);

        ServiceResult<UserDTO> userDTOServiceResult = userService.findById(houseDTO.getAdminId());
        model.addAttribute("agent", userDTOServiceResult.getResult());
        model.addAttribute("house", houseDTO);

//        ServiceResult<Long> aggResult = searchService.aggregateDistrictHouse(city.getEnName(), region.getEnName(), houseDTO.getDistrict());
//        model.addAttribute("houseCountInDistrict", aggResult.getResult());

        return "house-detail";
    }
}
