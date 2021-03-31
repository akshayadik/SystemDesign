package com.nf;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static java.nio.charset.Charset.defaultCharset;
import static org.springframework.util.StreamUtils.copyToString;

public class NewsFeedMock {

    public static void setupMockResponse(WireMockServer mockService) throws IOException {
        mockService.stubFor(WireMock.get(WireMock.urlEqualTo("/newsfeed?userId=4724036282881647131"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                copyToString(
                                        NewsFeedMock.class.getClassLoader().getResourceAsStream("newsfeed.json"),
                                        defaultCharset()))));

        mockService.stubFor(WireMock.get(WireMock.urlEqualTo("/findpost?userId=4724036282881647131"))
                .willReturn(WireMock.aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                copyToString(
                                        NewsFeedMock.class.getClassLoader().getResourceAsStream("newsfeed.json"),
                                        defaultCharset()))));
    }
}
