package com.migu.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.migu.entity.Subway;

/**
 * Created by 瓦力.
 */
public interface SubwayRepository extends CrudRepository<Subway, Long>{
    List<Subway> findAllByCityEnName(String cityEnName);
}
