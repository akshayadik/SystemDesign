package com.nf;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.nf.service.NewsFeedService;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
class NewsfeedApplicationTests {

	@Autowired
	public NewsFeedService newsFeedService;

	private static WireMockServer wireMockServer;

	@BeforeAll
	public static void beforeAll(){
		wireMockServer = new WireMockServer(8083);
		wireMockServer.start();
		try {
			NewsFeedMock.setupMockResponse(wireMockServer);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@AfterAll
	public static void afterAll(){
		wireMockServer.stop();
	}

	@Test
	void testNewsFeedService() {
		Assert.assertFalse(newsFeedService.getNewsFeed(4724036282881647131L).isEmpty());
	}

}
