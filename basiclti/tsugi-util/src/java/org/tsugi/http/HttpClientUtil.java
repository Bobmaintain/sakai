/*
 * Copyright (c) 2020- Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 */
package org.tsugi.http;

import java.io.InputStream;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Cookie;

import org.tsugi.http.HttpUtil;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

// A Java-11 HTTP utility based on
// https://mkyong.com/java/how-to-send-http-request-getpost-in-java/
// https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpResponse.BodyHandlers.html

@SuppressWarnings("deprecation")
@Slf4j
public class HttpClientUtil {

	/*
		// print status code
		System.out.println(response.statusCode());

		// print response body
		System.out.println(response.body());
	*/

	public static HttpRequest setupGet(String url, Map<String, String> parameters, Map<String, String> headers) throws Exception {

		String getUrl = HttpUtil.augmentGetURL(url, parameters);
		HttpRequest.Builder builder = HttpRequest.newBuilder()
				.GET()
				.uri(URI.create(getUrl))
				// .timeout(10)
				.header("User-Agent", "org.tsugi.http.HttpClientUtil web service request");

		if ( headers != null ) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.setHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		HttpRequest request = builder.build();
		return request;
	}

	public static HttpClient getClient() {

		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.build();

		return httpClient;
	}

	public static HttpResponse<String> sendGet(String url, Map<String, String> parameters, Map<String, String> headers) throws Exception {
		HttpRequest request = setupGet(url, parameters, headers);
		HttpResponse<String> response = getClient().send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	public static HttpResponse<InputStream> sendGetStream(String url, Map<String, String> parameters, Map<String, String> headers) throws Exception {
		HttpRequest request = setupGet(url, parameters, headers);
		HttpResponse<InputStream> response = getClient().send(request, HttpResponse.BodyHandlers.ofInputStream());
		return response;
	}

	public static HttpResponse<String> sendPost(String url, Map<String, String> data, Map<String, String> headers) throws Exception {
		HttpRequest.BodyPublisher body = buildFormDataFromMap(data);
		if ( headers == null ) headers = new HashMap<String, String>();
		if ( headers.get("Content-Type") == null ) headers.put("Content-Type", "application/x-www-form-urlencoded");
		return sendPost(url, body, headers);
	}

	public static HttpResponse<String> sendPost(String url, String data, Map<String, String> headers) throws Exception {
		HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(data);
		if ( headers == null ) headers = new HashMap<String, String>();
		return sendPost(url, body, headers);
	}

	public static HttpResponse<String> sendPost(String url, HttpRequest.BodyPublisher body, Map<String, String> headers) throws Exception {

		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.POST(body)
			// .timeout(10)
			.uri(URI.create(url))
			.header("User-Agent", "org.tsugi.http.HttpClientUtil web service request");

System.out.println("sendPost headers="+headers);

		if ( headers != null ) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
System.out.println(entry.getKey().toString()+" : "+entry.getValue().toString());
				builder.setHeader(entry.getKey().toString(), entry.getValue().toString());
			}
		}

		HttpRequest request = builder.build();

		HttpClient httpClient = HttpClient.newBuilder()
			.version(HttpClient.Version.HTTP_1_1)
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		return response;
	}

	private static HttpRequest.BodyPublisher buildFormDataFromMap(Map<String, String> data) {
		if ( data == null || data.size() < 1 ) return null;
		var builder = new StringBuilder();
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (builder.length() > 0) {
				builder.append("&");
			}
			builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
			builder.append("=");
			builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
		}
System.out.println("builder: "+builder.toString());
		return HttpRequest.BodyPublishers.ofString(builder.toString());
	}

}
