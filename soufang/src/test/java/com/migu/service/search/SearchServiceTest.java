package com.migu.service.search;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.migu.ApplicationTests;
import com.migu.base.JSONUtils;
import com.migu.service.ServiceMultiResult;
import com.migu.web.form.RentSearch;

public class SearchServiceTest extends ApplicationTests{

	@Autowired
	private ISearchService searchService;
	
	@Test
	public void testIndex() throws IOException, Exception {
		Long targetHouseId = 15L;
		searchService.index(targetHouseId);
	}
	
	@Test
	public void testQuery() {
		 RentSearch rentSearch = new RentSearch();
	        rentSearch.setCityEnName("bj");
	        rentSearch.setStart(0);
	        rentSearch.setSize(10);
//	        rentSearch.setKeywords("国贸");
	        ServiceMultiResult<Long> serviceResult = searchService.query(rentSearch);
	        Assert.assertTrue(serviceResult.getTotal() > 0);
	    }
	
	@Test
	public void testAgg() {
//		ServiceResult<Long> r = searchService.aggregateDistrictHouse("bj", "hdq", "2");
//		System.out.println(r.getMessage()+"total:"+r.getResult());
		ServiceMultiResult<HouseBucketDTO> r = searchService.mapAggregate("bj");
		System.out.println("聚合数量："+r.getResultSize());
		r.getResult().forEach(x->{
			System.out.println("聚合结果："+JSONUtils.toString(x));
			
		});
		
//		new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue)
		Executors.newFixedThreadPool(5, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
					
					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
						
					}
				});
				return null;
			}
		});
	}
	
	
}
