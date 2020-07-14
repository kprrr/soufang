package com.migu.service.search;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.migu.ApplicationTests;
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
}
