package com.migu.service.house;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.migu.base.LoginUserUtil;
import com.migu.entity.House;
import com.migu.entity.HouseDetail;
import com.migu.entity.HousePicture;
import com.migu.entity.HouseTag;
import com.migu.entity.Subway;
import com.migu.entity.SubwayStation;
import com.migu.repository.HouseDetailRepository;
import com.migu.repository.HousePictureRepository;
import com.migu.repository.HouseRepository;
import com.migu.repository.HouseSubscribeRespository;
import com.migu.repository.HouseTagRepository;
import com.migu.repository.SubwayRepository;
import com.migu.repository.SubwayStationRepository;
import com.migu.service.IHouseService;
import com.migu.service.ServiceResult;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.HouseDetailDTO;
import com.migu.web.dto.HousePictureDTO;
import com.migu.web.form.HouseForm;

public class HouseServiceImpl implements IHouseService{
	 @Autowired
	    private ModelMapper modelMapper;

	    @Autowired
	    private HouseRepository houseRepository;

	    @Autowired
	    private HouseDetailRepository houseDetailRepository;

	    @Autowired
	    private HousePictureRepository housePictureRepository;

	    @Autowired
	    private HouseTagRepository houseTagRepository;

	    @Autowired
	    private SubwayRepository subwayRepository;

	    @Autowired
	    private SubwayStationRepository subwayStationRepository;

	    @Autowired
	    private HouseSubscribeRespository subscribeRespository;
	
	@Override
    public ServiceResult<HouseDTO> save(HouseForm houseForm) {
        HouseDetail detail = new HouseDetail();
        ServiceResult<HouseDTO> subwayValidtionResult = wrapperDetailInfo(detail, houseForm);
        if (subwayValidtionResult != null) {
            return subwayValidtionResult;
        }

        House house = new House();
        modelMapper.map(houseForm, house);

        Date now = new Date();
        house.setCreateTime(now);
        house.setLastUpdateTime(now);
        house.setAdminId(LoginUserUtil.getLoginUserId());
        house = houseRepository.save(house);

        detail.setHouseId(house.getId());
        detail = houseDetailRepository.save(detail);

        List<HousePicture> pictures = generatePictures(houseForm, house.getId());
        Iterable<HousePicture> housePictures = housePictureRepository.save(pictures);

        HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
        HouseDetailDTO houseDetailDTO = modelMapper.map(detail, HouseDetailDTO.class);

        houseDTO.setHouseDetail(houseDetailDTO);

        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        housePictures.forEach(housePicture -> pictureDTOS.add(modelMapper.map(housePicture, HousePictureDTO.class)));
        houseDTO.setPictures(pictureDTOS);
        houseDTO.setCover(this.cdnPrefix + houseDTO.getCover());

        List<String> tags = houseForm.getTags();
        if (tags != null && !tags.isEmpty()) {
            List<HouseTag> houseTags = new ArrayList<>();
            for (String tag : tags) {
                houseTags.add(new HouseTag(house.getId(), tag));
            }
            houseTagRepository.save(houseTags);
            houseDTO.setTags(tags);
        }

        return new ServiceResult<HouseDTO>(true, null, houseDTO);
    }
	
	/**
     * 房源详细信息对象填充
     * @param houseDetail
     * @param houseForm
     * @return
     */
    private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
        Subway subway = subwayRepository.findOne(houseForm.getSubwayLineId());
        if (subway == null) {
            return new ServiceResult<>(false, "Not valid subway line!");
        }

        SubwayStation subwayStation = subwayStationRepository.findOne(houseForm.getSubwayStationId());
        if (subwayStation == null || subway.getId() != subwayStation.getSubwayId()) {
            return new ServiceResult<>(false, "Not valid subway station!");
        }

        houseDetail.setSubwayLineId(subway.getId());
        houseDetail.setSubwayLineName(subway.getName());

        houseDetail.setSubwayStationId(subwayStation.getId());
        houseDetail.setSubwayStationName(subwayStation.getName());

        houseDetail.setDescription(houseForm.getDescription());
        houseDetail.setDetailAddress(houseForm.getDetailAddress());
        houseDetail.setLayoutDesc(houseForm.getLayoutDesc());
        houseDetail.setRentWay(houseForm.getRentWay());
        houseDetail.setRoundService(houseForm.getRoundService());
        houseDetail.setTraffic(houseForm.getTraffic());
        return null;

    }

}
