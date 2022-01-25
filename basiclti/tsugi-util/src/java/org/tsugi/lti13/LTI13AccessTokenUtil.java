/*
 * $URL$
 * $Id$
 *
 * Copyright (c) 2022- Charles R. Severance
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
 */
package org.tsugi.lti13;

import java.util.Map;
import java.util.TreeMap;

import java.net.http.HttpResponse;  // Thanks Java 11

import java.security.Key;
import java.security.KeyPair;

import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Jwts;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.tsugi.http.HttpClientUtil;
import org.tsugi.jackson.JacksonUtil;
import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.oauth2.objects.ClientAssertion;

import lombok.extern.slf4j.Slf4j;

// https://www.imsglobal.org/spec/security/v1p0/

@Slf4j
public class LTI13AccessTokenUtil {

	/**
	 * Return a Map of values ready for posting
	 * 
	 *   grant_type=client_credentials
	 *   client_assertion_type=urn:ietf:params:oauth:client-assertion-type:jwt-bearer
	 *   client_assertion=eyJ0eXAiOiJKV1QiLCJhbG....qEZtDgBgMMsneNePfMrifOvvFLkxnpefA
	 *   scope=http://imsglobal.org/ags/lineitem http://imsglobal.org/ags/result/read
	 *
	 *   https://www.imsglobal.org/spec/security/v1p0/#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
	 */
	public static Map getClientAssertion(String[] scopes, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		Map retval = new TreeMap();
		retval.put(ClientAssertion.GRANT_TYPE, ClientAssertion.GRANT_TYPE_CLIENT_CREDENTIALS);
		retval.put(ClientAssertion.CLIENT_ASSERTION_TYPE, ClientAssertion.CLIENT_ASSERTION_TYPE_JWT);
		if ( scopes != null && scopes.length > 0 ) retval.put(ClientAssertion.SCOPE, String.join(" ", scopes));

		ClientAssertion ca = new ClientAssertion();
		ca.issuer = clientId;  // This is our server
		ca.subject = clientId;
		ca.deployment_id = deploymentId;
		if ( !StringUtils.isEmpty(tokenAudience) ) ca.audience = tokenAudience;

		String cas = JacksonUtil.toString(ca);

		Key privateKey = keyPair.getPrivate();
		Key publicKey = keyPair.getPublic();

		String kid = LTI13KeySetUtil.getPublicKID(publicKey);

		log.debug("getClientAssertion kid={} token={}", kid, cas);
		String jws = Jwts.builder().setHeaderParam("kid", kid).
					setPayload(cas).signWith(privateKey).compact();
		retval.put(ClientAssertion.CLIENT_ASSERTION, jws);

		return retval;
	}

	public static AccessToken getScoreToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		Map assertion = getScoreAssertion(keyPair, clientId, deploymentId, tokenAudience);
		return retrieveToken(url, assertion);
	}

	public static Map getScoreAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_LINEITEM,
					LTI13ConstantsUtil.SCOPE_SCORE,
					LTI13ConstantsUtil.SCOPE_RESULT_READONLY
				},
			keyPair, clientId, deploymentId, tokenAudience);
	}

	public static AccessToken getNRPSToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		Map assertion = getNRPSAssertion(keyPair, clientId, deploymentId, tokenAudience);
		return retrieveToken(url, assertion);
	}

	public static Map getNRPSAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_NAMES_AND_ROLES
				},
			keyPair, clientId, deploymentId, tokenAudience);
	}

	public static AccessToken getLineItemsToken(String url, KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		Map assertion = getLineItemsAssertion(keyPair, clientId, deploymentId, tokenAudience);
		return retrieveToken(url, assertion);
	}


	public static Map getLineItemsAssertion(KeyPair keyPair,
		String clientId, String deploymentId, String tokenAudience)
	{
		return getClientAssertion(
				new String[] {
					LTI13ConstantsUtil.SCOPE_LINEITEM
				},
			keyPair, clientId, deploymentId, tokenAudience);
	}

	protected static AccessToken retrieveToken(String url, Map assertion)
	{
		try {
			// TODO: Should this be form encoded per IMS or JSON per Canvas?
			// https://www.imsglobal.org/spec/security/v1p0/#using-json-web-tokens-with-oauth-2-0-client-credentials-grant
			// https://canvas.instructure.com/doc/api/file.oauth_endpoints.html#post-login-oauth2-token
System.out.println("Getting access token url="+url);
System.out.println("Assertion Map\n"+assertion);

			HttpResponse<String> response = HttpClientUtil.sendPost(url, assertion, null);
			String responseStr = response.body();
System.out.println("responseStr="+responseStr);
			ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
			AccessToken accessToken = mapper.readValue(responseStr, AccessToken.class);
System.out.println("accessToken="+accessToken);
			if ( accessToken == null || StringUtils.isEmpty(accessToken.access_token) ) {
				log.info("Failed to retrieve access token url={} sent={} received={}",url, assertion, responseStr);
System.out.println("Failed to retrieve access token url="+url+" sent="+assertion+" received="+responseStr);
			}
			return accessToken;
		} catch (Exception e) {
e.printStackTrace();
			log.error("Error retrieving token from {}", url, e);
			return null;
		}
	}

}
