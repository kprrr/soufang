package com.migu.web.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.migu.base.ApiDataTableResponse;
import com.migu.base.ApiResponse;
import com.migu.base.HouseOperation;
import com.migu.base.HouseStatus;
import com.migu.entity.SupportAddress;
import com.migu.service.IAddressService;
import com.migu.service.IHouseService;
import com.migu.service.ServiceMultiResult;
import com.migu.service.ServiceResult;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.HouseDetailDTO;
import com.migu.web.dto.SubwayDTO;
import com.migu.web.dto.SubwayStationDTO;
import com.migu.web.dto.SupportAddressDTO;
import com.migu.web.form.DatatableSearch;
import com.migu.web.form.HouseForm;

@Controller
public class AdminController {

	@Autowired
	private IHouseService houseService;

	@Autowired
	private IAddressService addressService;

	@GetMapping("/admin/center")
	public String adminCenterPage() {
		return "admin/center";
	}

	@GetMapping("/admin/welcome")
	public String welcomePage() {
		return "admin/welcome";
	}

	@GetMapping("/admin/login")
	public String adminLoginPage() {
		return "admin/login";
	}

	/**
	 * 房源列表页
	 * 
	 * @return
	 */
	@GetMapping("admin/house/list")
	public String houseListPage() {
		return "admin/house-list";
	}

	@PostMapping("admin/houses")
	@ResponseBody
	public ApiDataTableResponse houses(@ModelAttribute DatatableSearch searchBody) {
		ServiceMultiResult<HouseDTO> result = houseService.adminQuery(searchBody);

		ApiDataTableResponse response = new ApiDataTableResponse(ApiResponse.Status.SUCCESS);
		response.setData(result.getResult());
		response.setRecordsFiltered(result.getTotal());
		response.setRecordsTotal(result.getTotal());

		response.setDraw(searchBody.getDraw());
		return response;
	}

	@PostMapping(value = "/admin/upload/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseBody
	public ApiResponse uploadPhoto(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
		}
		String fileName = file.getOriginalFilename();
		File target = new File("E:/" + fileName);
		try {
			file.transferTo(target);
		} catch (IllegalStateException | IOException e) {
			e.printStackTrace();
			return ApiResponse.ofStatus(ApiResponse.Status.INTERNAL_SERVER_ERROR);
		}

		return ApiResponse.ofSuccess(null);
	}

	/**
	 * 新增房源功能页
	 * 
	 * @return
	 */
	@GetMapping("admin/add/house")
	public String addHousePage() {
		return "admin/house-add";
	}

	/**
	 * 新增房源接口
	 * 
	 * @param houseForm
	 * @param bindingResult
	 * @return
	 */
	@PostMapping("admin/add/house")
	@ResponseBody
	public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add") HouseForm houseForm,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return new ApiResponse(HttpStatus.BAD_REQUEST.value(),
					bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
		}

//		if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
//			return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
//		}

		Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService
				.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
		if (addressMap.keySet().size() != 2) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
		}

		ServiceResult<HouseDTO> result = houseService.save(houseForm);
		if (result.isSuccess()) {
			return ApiResponse.ofSuccess(result.getResult());
		}

		return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
	}

	/**
	 * 房源信息编辑页
	 * 
	 * @return
	 */
	@GetMapping("admin/house/edit")
	public String houseEditPage(@RequestParam(value = "id") Long id, Model model) {

		if (id == null || id < 1) {
			return "404";
		}

		ServiceResult<HouseDTO> serviceResult = houseService.findCompleteOne(id);
		if (!serviceResult.isSuccess()) {
			return "404";
		}

		HouseDTO result = serviceResult.getResult();
		model.addAttribute("house", result);

		Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService
				.findCityAndRegion(result.getCityEnName(), result.getRegionEnName());
		model.addAttribute("city", addressMap.get(SupportAddress.Level.CITY));
		model.addAttribute("region", addressMap.get(SupportAddress.Level.REGION));

		HouseDetailDTO detailDTO = result.getHouseDetail();
		ServiceResult<SubwayDTO> subwayServiceResult = addressService.findSubway(detailDTO.getSubwayLineId());
		if (subwayServiceResult.isSuccess()) {
			model.addAttribute("subway", subwayServiceResult.getResult());
		}

		ServiceResult<SubwayStationDTO> subwayStationServiceResult = addressService
				.findSubwayStation(detailDTO.getSubwayStationId());
		if (subwayStationServiceResult.isSuccess()) {
			model.addAttribute("station", subwayStationServiceResult.getResult());
		}

		return "admin/house-edit";
	}

	/**
	 * 审核接口
	 * 
	 * @param id
	 * @param operation
	 * @return
	 */
	@PutMapping("admin/house/operate/{id}/{operation}")
	@ResponseBody
	public ApiResponse operateHouse(@PathVariable(value = "id") Long id,
			@PathVariable(value = "operation") int operation) {
		if (id <= 0) {
			return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
		}
		ServiceResult result;

		switch (operation) {
		case HouseOperation.PASS:
			result = this.houseService.updateStatus(id, HouseStatus.PASSES.getValue());
			break;
		case HouseOperation.PULL_OUT:
			result = this.houseService.updateStatus(id, HouseStatus.NOT_AUDITED.getValue());
			break;
		case HouseOperation.DELETE:
			result = this.houseService.updateStatus(id, HouseStatus.DELETED.getValue());
			break;
		case HouseOperation.RENT:
			result = this.houseService.updateStatus(id, HouseStatus.RENTED.getValue());
			break;
		default:
			return ApiResponse.ofStatus(ApiResponse.Status.BAD_REQUEST);
		}

		if (result.isSuccess()) {
			return ApiResponse.ofSuccess(null);
		}
		return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), result.getMessage());
	}
}
