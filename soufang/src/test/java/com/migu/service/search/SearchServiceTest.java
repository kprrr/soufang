package com.migu.service.search;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.migu.ApplicationTests;

public class SearchServiceTest extends ApplicationTests{

	@Autowired
	private ISearchService searchService;
	
	@Test
	public void testIndex() throws IOException, Exception {
		Long targetHouseId = 15L;
		searchService.index(targetHouseId);
	}
}
