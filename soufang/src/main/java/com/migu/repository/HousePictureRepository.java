package com.migu.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.migu.entity.HousePicture;

/**
 * Created by 瓦力.
 */
public interface HousePictureRepository extends CrudRepository<HousePicture, Long> {
    List<HousePicture> findAllByHouseId(Long id);
}
