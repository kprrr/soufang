package com.migu.web.controller.admin;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.migu.base.ApiResponse;
import com.migu.entity.SupportAddress;
import com.migu.service.IHouseService;
import com.migu.service.ServiceResult;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.SupportAddressDTO;
import com.migu.web.form.HouseForm;

@Controller
public class AdminController {

	@Autowired
	private IHouseService houseService;
	
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
	
	public ApiResponse addHouse(@Valid @ModelAttribute("form-house-add")HouseForm houseForm,BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
            return new ApiResponse(HttpStatus.BAD_REQUEST.value(), bindingResult.getAllErrors().get(0).getDefaultMessage(), null);
        }

        if (houseForm.getPhotos() == null || houseForm.getCover() == null) {
            return ApiResponse.ofMessage(HttpStatus.BAD_REQUEST.value(), "必须上传图片");
        }

        Map<SupportAddress.Level, SupportAddressDTO> addressMap = addressService.findCityAndRegion(houseForm.getCityEnName(), houseForm.getRegionEnName());
        if (addressMap.keySet().size() != 2) {
            return ApiResponse.ofStatus(ApiResponse.Status.NOT_VALID_PARAM);
        }

        ServiceResult<HouseDTO> result = houseService.save(houseForm);
        if (result.isSuccess()) {
            return ApiResponse.ofSuccess(result.getResult());
        }

        return ApiResponse.ofSuccess(ApiResponse.Status.NOT_VALID_PARAM);
	}
}
