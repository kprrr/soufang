package com.migu.service;

import java.util.List;

import lombok.Data;

/**
 * 通用多结果Service返回结构
 * @author Lee
 * @param <T>
 *
 */

@Data
public class ServiceMultiResult<T> {
	private long total;
	private List<T> result;
	public ServiceMultiResult(long total, List<T> result) {
		super();
		this.total = total;
		this.result = result;
	}
	
	public int getResultSize() {
		if(this.result == null) {
			return 0;
		}
		return this.result.size();
	}
	
	
}
