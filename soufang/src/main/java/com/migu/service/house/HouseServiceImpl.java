package com.migu.service.house;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.Predicate;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.migu.base.HouseSort;
import com.migu.base.HouseStatus;
import com.migu.base.LoginUserUtil;
import com.migu.entity.House;
import com.migu.entity.HouseDetail;
import com.migu.entity.HousePicture;
import com.migu.entity.HouseSubscribe;
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
import com.migu.service.ServiceMultiResult;
import com.migu.service.ServiceResult;
import com.migu.service.search.ISearchService;
import com.migu.web.dto.HouseDTO;
import com.migu.web.dto.HouseDetailDTO;
import com.migu.web.dto.HousePictureDTO;
import com.migu.web.form.DatatableSearch;
import com.migu.web.form.HouseForm;
import com.migu.web.form.PhotoForm;
import com.migu.web.form.RentSearch;

@Service
public class HouseServiceImpl implements IHouseService {
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
	
	@Autowired
	private ISearchService searchService;
	

	@Value("cdnUrl")
	private String cdnPrefix;

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
		Iterable<HousePicture> housePictures = housePictureRepository.saveAll(pictures);

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
			houseTagRepository.saveAll(houseTags);
			houseDTO.setTags(tags);
		}

		return new ServiceResult<HouseDTO>(true, null, houseDTO);
	}

	/**
	 * 房源详细信息对象填充
	 * 
	 * @param houseDetail
	 * @param houseForm
	 * @return
	 */
	private ServiceResult<HouseDTO> wrapperDetailInfo(HouseDetail houseDetail, HouseForm houseForm) {
		Subway subway = subwayRepository.findById(houseForm.getSubwayLineId()).get();
		if (subway == null) {
			return new ServiceResult<>(false, "Not valid subway line!");
		}

		SubwayStation subwayStation = subwayStationRepository.findById(houseForm.getSubwayStationId()).get();
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

	/**
	 * 图片对象列表信息填充
	 * 
	 * @param form
	 * @param houseId
	 * @return
	 */
	private List<HousePicture> generatePictures(HouseForm form, Long houseId) {
		List<HousePicture> pictures = new ArrayList<>();
		if (form.getPhotos() == null || form.getPhotos().isEmpty()) {
			return pictures;
		}

		for (PhotoForm photoForm : form.getPhotos()) {
			HousePicture picture = new HousePicture();
			picture.setHouseId(houseId);
			picture.setCdnPrefix(cdnPrefix);
			picture.setPath(photoForm.getPath());
			picture.setWidth(photoForm.getWidth());
			picture.setHeight(photoForm.getHeight());
			pictures.add(picture);
		}
		return pictures;
	}

	@Override
	public ServiceResult<HouseDTO> findCompleteOne(Long id) {
		House house = houseRepository.findById(id).get();
        if (house == null) {
            return ServiceResult.notFound();
        }

        HouseDetail detail = houseDetailRepository.findByHouseId(id);
        List<HousePicture> pictures = housePictureRepository.findAllByHouseId(id);

        HouseDetailDTO detailDTO = modelMapper.map(detail, HouseDetailDTO.class);
        List<HousePictureDTO> pictureDTOS = new ArrayList<>();
        for (HousePicture picture : pictures) {
            HousePictureDTO pictureDTO = modelMapper.map(picture, HousePictureDTO.class);
            pictureDTOS.add(pictureDTO);
        }


        List<HouseTag> tags = houseTagRepository.findAllByHouseId(id);
        List<String> tagList = new ArrayList<>();
        for (HouseTag tag : tags) {
            tagList.add(tag.getName());
        }

        HouseDTO result = modelMapper.map(house, HouseDTO.class);
        result.setHouseDetail(detailDTO);
        result.setPictures(pictureDTOS);
        result.setTags(tagList);

        if (LoginUserUtil.getLoginUserId() > 0) { // 已登录用户
            HouseSubscribe subscribe = subscribeRespository.findByHouseIdAndUserId(house.getId(), LoginUserUtil.getLoginUserId());
            if (subscribe != null) {
                result.setSubscribeStatus(subscribe.getStatus());
            }
        }

        return ServiceResult.of(result);
	}

	@Override
	public ServiceMultiResult<HouseDTO> adminQuery(DatatableSearch searchBody) {
		List<HouseDTO> houseDTOS = new ArrayList<>();

//        Sort sort = new Sort(Sort.Direction.fromString(searchBody.getDirection()), searchBody.getOrderBy());
        
        int page = searchBody.getStart() / searchBody.getLength();

//        Pageable pageable = new PageRequest(page, searchBody.getLength(), sort);
        
        // Sort.by(Sort.Direction.DESC,"createdAt")
        Pageable pageable = PageRequest.of(page, searchBody.getLength(), Sort.by(Sort.Direction.DESC, searchBody.getOrderBy()));

        Specification<House> specification = (root, query, cb) -> {
            Predicate predicate = cb.equal(root.get("adminId"), LoginUserUtil.getLoginUserId());
            predicate = cb.and(predicate, cb.notEqual(root.get("status"), HouseStatus.DELETED.getValue()));

            if (searchBody.getCity() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("cityEnName"), searchBody.getCity()));
            }

            if (searchBody.getStatus() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("status"), searchBody.getStatus()));
            }

            if (searchBody.getCreateTimeMin() != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMin()));
            }

            if (searchBody.getCreateTimeMax() != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("createTime"), searchBody.getCreateTimeMax()));
            }

            if (searchBody.getTitle() != null) {
                predicate = cb.and(predicate, cb.like(root.get("title"), "%" + searchBody.getTitle() + "%"));
            }

            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);
        });

        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
	}
	
	@Override
    @Transactional
    public ServiceResult updateStatus(Long id, int status) {
        House house = houseRepository.findById(id).get();
        if (house == null) {
            return ServiceResult.notFound();
        }

        if (house.getStatus() == status) {
            return new ServiceResult(false, "状态没有发生变化");
        }

        if (house.getStatus() == HouseStatus.RENTED.getValue()) {
            return new ServiceResult(false, "已出租的房源不允许修改状态");
        }

        if (house.getStatus() == HouseStatus.DELETED.getValue()) {
            return new ServiceResult(false, "已删除的资源不允许操作");
        }

        houseRepository.updateStatus(id, status);

        // 上架更新索引 其他情况都要删除索引
        if (status == HouseStatus.PASSES.getValue()) {
            try {
				searchService.index(id);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        } else {
            searchService.remove(id);
        }
        return ServiceResult.success();
    }

	@Override
	public ServiceMultiResult<HouseDTO> query(RentSearch rentSearch) {
		if (rentSearch.getKeywords() != null && !rentSearch.getKeywords().isEmpty()) {
            ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);//使用es查询
            if (serviceResult.getTotal() == 0) {
                return new ServiceMultiResult<>(0, new ArrayList<>());
            }

            return new ServiceMultiResult<>(serviceResult.getTotal(), wrapperHouseResult(serviceResult.getResult()));
        }

        return simpleQuery(rentSearch);
//        return null;
	}
	
	/**
	 * 使用 es查询结果houseIds 去数据库查询实体信息
	 * @return
	 */
	private List<HouseDTO> wrapperHouseResult(List<Long> houseIds) {
		List<HouseDTO> result = new ArrayList<>();

        Map<Long, HouseDTO> idToHouseMap = new HashMap<>();
        Iterable<House> houses = houseRepository.findAllById(houseIds);
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            idToHouseMap.put(house.getId(), houseDTO);
        });
        
        wrapperHouseList(houseIds, idToHouseMap);
        
        
        // 矫正顺序
        for (Long houseId : houseIds) {
            result.add(idToHouseMap.get(houseId));
        }
        return result;
        
	}
	
    /**
     * 渲染详细信息 及 标签
     * @param houseIds
     * @param idToHouseMap
     */
    private void wrapperHouseList(List<Long> houseIds, Map<Long, HouseDTO> idToHouseMap) {
        List<HouseDetail> details = houseDetailRepository.findAllByHouseIdIn(houseIds);
        details.forEach(houseDetail -> {
            HouseDTO houseDTO = idToHouseMap.get(houseDetail.getHouseId());
            HouseDetailDTO detailDTO = modelMapper.map(houseDetail, HouseDetailDTO.class);
            houseDTO.setHouseDetail(detailDTO);
        });

        List<HouseTag> houseTags = houseTagRepository.findAllByHouseIdIn(houseIds);
        houseTags.forEach(houseTag -> {
            HouseDTO house = idToHouseMap.get(houseTag.getHouseId());
            house.getTags().add(houseTag.getName());
        });
    }
    
    /**
     * 查询数据库
     * @param rentSearch
     * @return
     */
    private ServiceMultiResult<HouseDTO> simpleQuery(RentSearch rentSearch) {
        Sort sort = HouseSort.generateSort(rentSearch.getOrderBy(), rentSearch.getOrderDirection());
        int page = rentSearch.getStart() / rentSearch.getSize();

        Pageable pageable = PageRequest.of(page, rentSearch.getSize(), sort);
//        		new PageRequest(page, rentSearch.getSize(), sort);

        Specification<House> specification = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.equal(root.get("status"), HouseStatus.PASSES.getValue());

            predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("cityEnName"), rentSearch.getCityEnName()));

            if (HouseSort.DISTANCE_TO_SUBWAY_KEY.equals(rentSearch.getOrderBy())) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.gt(root.get(HouseSort.DISTANCE_TO_SUBWAY_KEY), -1));
            }
            return predicate;
        };

        Page<House> houses = houseRepository.findAll(specification, pageable);
        List<HouseDTO> houseDTOS = new ArrayList<>();


        List<Long> houseIds = new ArrayList<>();
        Map<Long, HouseDTO> idToHouseMap = Maps.newHashMap();
        houses.forEach(house -> {
            HouseDTO houseDTO = modelMapper.map(house, HouseDTO.class);
            houseDTO.setCover(this.cdnPrefix + house.getCover());
            houseDTOS.add(houseDTO);

            houseIds.add(house.getId());
            idToHouseMap.put(house.getId(), houseDTO);
        });


        wrapperHouseList(houseIds, idToHouseMap);
        return new ServiceMultiResult<>(houses.getTotalElements(), houseDTOS);
    }

}
