/*
 * Copyright (c) 2021- Charles R. Severance
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

package org.sakaiproject.plus.impl.service;

import java.util.Map;
import java.util.TreeMap;
import java.util.Optional;

import java.io.InputStream;

import java.time.Instant;

import java.net.http.HttpResponse;  // Thanks Java 11

import java.security.Key;
import java.security.KeyPair;

import org.apache.commons.lang3.StringUtils;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.api.Event;

import org.sakaiproject.lti.api.LTIException;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.CommentDefinition;

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Link;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.repository.ContextRepository;
import org.sakaiproject.plus.api.repository.LineItemRepository;
import org.sakaiproject.plus.api.repository.LinkRepository;
import org.sakaiproject.plus.api.repository.ScoreRepository;
import org.sakaiproject.plus.api.repository.SubjectRepository;
import org.sakaiproject.plus.api.repository.TenantRepository;
import org.sakaiproject.plus.api.service.PlusService;
import org.springframework.beans.factory.annotation.Autowired;

import org.tsugi.http.HttpClientUtil;

import org.tsugi.nrps.objects.Member;
import org.tsugi.oauth2.objects.AccessToken;
import org.tsugi.lti13.objects.LaunchJWT;
import org.tsugi.ags2.objects.LineItem;
import org.tsugi.ags2.objects.Score;

import org.sakaiproject.basiclti.util.SakaiKeySetUtil;
import org.tsugi.lti13.LTI13AccessTokenUtil;
import org.tsugi.jackson.JacksonUtil;
import org.sakaiproject.lti13.util.SakaiLaunchJWT;
import org.tsugi.lti13.LTI13ConstantsUtil;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class PlusServiceImpl implements PlusService {

	public final String PLUS_PROVIDER_ENABLED = "plus.provider.enabled";
	public final String PLUS_PROVIDER_ENABLED_DEFAULT = "true";
	public final String PLUS_ROSTER_SYCHRONIZATION = "plus.roster.synchronization";
	public final boolean PLUS_ROSTER_SYCHRONIZATION_DEFAULT = false;

	@Autowired private TenantRepository tenantRepository;
	@Autowired private SubjectRepository subjectRepository;
	@Autowired private ContextRepository contextRepository;
	@Autowired private LinkRepository linkRepository;
	@Autowired private LineItemRepository lineItemRepository;
	@Autowired private ScoreRepository scoreRepository;
    @Autowired private GradebookService gradebookService;

	/*
	 * Indicate if plus is enabled on this system
	 */
	public boolean enabled()
	{
		String enabled = ServerConfigurationService.getString(PLUS_PROVIDER_ENABLED, PLUS_PROVIDER_ENABLED_DEFAULT);
		return !("false".equals(enabled));
	}

	/*
	 * Indicate if plus is enabled on a Site
	 */
	public boolean enabled(Site site)
	{
		String plus_property = site.getProperties().getProperty(PlusService.PLUS_PROPERTY);
		return "true".equals(plus_property);
	}

	/*
	 * Handle the initial launch - creating objects as needed (a.k.a. The BIG LEFT JOIN)
	 */
	public Launch updateAll(LaunchJWT launchJWT, Tenant tenant)
		throws LTIException
	{
		if ( launchJWT == null || tenant == null ) {
			throw new LTIException("plus.plusservice.null", null, null);
		}

		if ( tenant.getId() == null ) {
			throw new LTIException("plus.plusservice.tenant.persist", null, null);
		}

		String issuer = launchJWT.issuer;
		String clientId = launchJWT.audience;
		String deploymentId = launchJWT.deployment_id;

		String missing = "";
		if ( issuer == null ) {
			missing = missing + "issuer null ";
		} else if (! issuer.equals(tenant.getIssuer()) ) {
			missing = missing + "issuer mismatch ";
		}

		if ( clientId == null ) {
			missing = missing + "clientId null ";
		} else if (! clientId.equals(tenant.getClientId()) ) {
			missing = missing + "clientId mismatch ";
		}

		if ( deploymentId == null ) {
			missing = missing + "deploymentId null ";
		} else if (! deploymentId.equals(tenant.getDeploymentId()) ) {
			missing = missing + "deploymentId mismatch ";
		}

		if ( ! missing.equals("") ) {
			throw new LTIException("plus.plusservice.tenant.check", missing, null);
		}

		String contextId = launchJWT.context != null ? launchJWT.context.id : null;
		String subjectId = launchJWT.subject;
		String linkId =  launchJWT.resource_link != null ? launchJWT.resource_link.id : null;

		Launch launch = new Launch();
		launch.tenant = tenant;

		boolean changed = false;
		Subject subject = null;
		if ( subjectId != null ) {
			subject = subjectRepository.findBySubjectAndTenant(subjectId, tenant);
			if ( subject == null ) {
				subject = new Subject();
				subject.setSubject(subjectId);
				subject.setTenant(tenant);
				subject.setEmail(launchJWT.email);
				subject.setDisplayName(launchJWT.getDisplayName());
				changed = true;
			} else {
				if ( StringUtils.compare(subject.getEmail(), launchJWT.email) != 0 ) {
					subject.setEmail(launchJWT.email);
					changed = true;
				}
				if ( StringUtils.compare(subject.getDisplayName(), launchJWT.getDisplayName() ) != 0 ) {
					subject.setDisplayName(launchJWT.getDisplayName());
					changed = true;
				}
			}
			if ( changed ) {
				subjectRepository.save(subject);
			}
			launch.subject = subject;
		}

		Context context = null;
		if ( contextId != null ) {
			context = contextRepository.findByContextAndTenant(contextId, tenant);
			changed = false;
			if ( context == null ) {
				context = new Context();
				context.setContext(contextId);
				context.setTenant(tenant);
				context.setTitle(launchJWT.context.title);
				context.setLabel(launchJWT.context.label);
				if ( launchJWT.endpoint != null && launchJWT.endpoint.lineitems != null ) context.setLineItems(launchJWT.endpoint.lineitems);
				if ( launchJWT.names_and_roles != null && launchJWT.names_and_roles.context_memberships_url != null ) {
					context.setContextMemberships(launchJWT.names_and_roles.context_memberships_url);
				}
				changed = true;
			} else {
				if ( StringUtils.compare(context.getTitle(), launchJWT.context.title) != 0 ) {
					context.setTitle(launchJWT.context.title);
					changed = true;
				}
				if ( StringUtils.compare(context.getLabel(), launchJWT.context.label) != 0 ) {
					context.setLabel(launchJWT.context.label);
					changed = true;
				}

				if ( launchJWT.endpoint != null && launchJWT.endpoint.lineitems != null &&
					 StringUtils.compare(context.getLineItems(), launchJWT.endpoint.lineitems) != 0 ) {
					context.setLineItems(launchJWT.endpoint.lineitems);
					changed = true;
				}
				if ( launchJWT.names_and_roles != null && launchJWT.names_and_roles.context_memberships_url != null &&
					StringUtils.compare(context.getContextMemberships(), launchJWT.names_and_roles.context_memberships_url) != 0 ) {
					context.setContextMemberships(launchJWT.names_and_roles.context_memberships_url);
					changed = true;
				}
			}
			if ( changed) contextRepository.save(context);
			launch.context = context;
		}

		if ( linkId != null && context != null ) {
			Link link = linkRepository.findByLinkAndContext(linkId, context);
			changed = false;
			if ( link == null ) {
				link = new Link();
				link.setLink(linkId);
				link.setContext(context);
				link.setTitle(launchJWT.resource_link.title);
				link.setDescription(launchJWT.resource_link.description);
				changed = true;
			} else {
				if ( StringUtils.compare(link.getTitle(), launchJWT.resource_link.title) != 0 ) {
					link.setTitle(launchJWT.resource_link.title);
					changed = true;
				}
				if ( StringUtils.compare(link.getDescription(), launchJWT.resource_link.description) != 0 ) {
					link.setDescription(launchJWT.resource_link.description);
					changed = true;
				}
			}
			if ( changed) linkRepository.save(link);
			launch.link = link;
		}

		return launch;
	}

	/*
	 * Make sure the Subject knows about the chosen user
	 */
	public void connectSubjectAndUser(Subject subject, User user)
			throws LTIException
	{
		if ( subject == null || user == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "subject or user", null);
		}

		if ( StringUtils.isEmpty(subject.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "subject", null);
		}

		if ( StringUtils.isEmpty(user.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "user", null);
		}

		// After all that error checking, it is prety simple
		subject.setSakaiUserId(user.getId());
		subjectRepository.save(subject);
	}

	/*
	 * Make sure the Context knows about the chosen site
	 */
	public void connectContextAndSite(Context context, Site site)
			throws LTIException
	{
		if ( context == null || site == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "context or site", null);
		}

		if ( StringUtils.isEmpty(context.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "context", null);
		}

		if ( StringUtils.isEmpty(site.getId()) ) {
		  throw new LTIException( "plus.plusservice.site.not.persisted", "site", null);
		}

		// After all that error checking, it is prety simple
		context.setSakaiSiteId(site.getId());
		contextRepository.save(context);
	}

	/*
	 * Make sure the Link knows about the chosen placement
	 */
	public void connectLinkAndPlacement(Link link, String placementId)
			throws LTIException
	{
		if ( link == null ) {
		  throw new LTIException( "plus.plusservice.null.parameters", "link", null);
		}

		if ( StringUtils.isEmpty(link.getId()) ) {
		  throw new LTIException( "plus.plusservice.not.persisted", "link", null);
		}

		if ( StringUtils.isEmpty(placementId) ) {
		  throw new LTIException( "plus.plusservice.site.null.parameters", "placement", null);
		}

		// After all that error checking, it is prety simple
		link.setSakaiToolId(placementId);
		linkRepository.save(link);
	}

	/*
	 * Retrieve Context Memberships from calling LMS and update the site in Sakai
	 */
	public void syncSiteMemberships(String contextGuid, Site site) throws LTIException {

		log.debug("synchSiteMemberships");
System.out.println("synchSiteMemberships");

		if (!ServerConfigurationService.getBoolean(PLUS_ROSTER_SYCHRONIZATION, PLUS_ROSTER_SYCHRONIZATION_DEFAULT)) {
			log.info("LTI Memberships synchronization disabled.");
			return;
		}

		if (StringUtils.isEmpty(contextGuid) ) {
			log.info("Context GUID is required. Memberships will NOT be synchronized.");
			return;
		}

		Optional<Context> optContext = contextRepository.findById(contextGuid);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}

		if ( context == null ) {
			log.info("Context notfound {}", contextGuid);
			return;
		}

		String tenantGuid = context.getTenant().getId();
		String contextMemberships = context.getContextMemberships();

		if (StringUtils.isEmpty(tenantGuid)) {
			log.info("Context {} does not have a tenant.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		if (StringUtils.isEmpty(contextMemberships)) {
			log.info("Context {} does not have Memberships URL.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		// Load the Tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenantGuid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			log.info("Tenant notfound {}", tenantGuid);
			return;
		}

		String clientId = tenant.getClientId();
		String deploymentId = tenant.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( StringUtils.isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;
		boolean trustEmail = ! Boolean.FALSE.equals(tenant.getTrustEmail());

		if (StringUtils.isEmpty(clientId)) {
			log.info("Tenant {} does not have clientId.  Memberships will NOT be synchronized.", tenantGuid);
			return;
		}

		if (StringUtils.isEmpty(deploymentId)) {
			log.info("Tenant {} does not have deploymentId.  Memberships will NOT be synchronized.", tenantGuid);
			return;
		}

		if (StringUtils.isEmpty(oidcTokenUrl)) {
			log.info("Tenant {} does not have an OIDC Token URL.  Memberships will NOT be synchronized.", contextGuid);
			return;
		}

		// Looks like we have the requisite strings in variables :)
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		AccessToken nrpsAccessToken = LTI13AccessTokenUtil.getNRPSToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience);
		if ( nrpsAccessToken == null || StringUtils.isEmpty(nrpsAccessToken.access_token) ) {
			log.info("Could not retrieve NRPS (Names and Roles) token from {}.  Memberships will NOT be synchronized.", oidcTokenUrl);
			return;
		}

		String MEDIA_TYPE_MEMBERSHIPS = "application/vnd.ims.lti-nrps.v2.membershipcontainer+json";
		Map<String, String> headers = new TreeMap<String, String>();
		headers.put("Authorization", "Bearer "+nrpsAccessToken.access_token);
		headers.put("Accept", LTI13ConstantsUtil.MEDIA_TYPE_MEMBERSHIPS);
		headers.put("Content-Type", LTI13ConstantsUtil.MEDIA_TYPE_MEMBERSHIPS); // TODO: Remove when certification is fixed

		// Get ready
		context.setNrpsStart(Instant.now());
		context.setNrpsFinish(null);
		context.setNrpsCount(new Long(0));
		context.setNrpsStatus("Started");
		contextRepository.save(context);

		InputStream is = null;
		 try {
			HttpResponse<InputStream> response = HttpClientUtil.sendGetStream(contextMemberships, null, headers);
			is = response.body();
		} catch (Exception e) {
			log.error("Error retrieving NRPS (Names and Roles) data from {}", contextMemberships);
			return;
		}

		// https://cassiomolin.com/2019/08/19/combining-jackson-streaming-api-with-objectmapper-for-parsing-json/
		// Create and configure an ObjectMapper instance
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();

		Long count = new Long(0);
		// Create a JsonParser instance
		try {
			JsonParser jsonParser = mapper.getFactory().createParser(is);

			// Check the first token
			String lastText = null;
			JsonToken nextToken = null;
			while (true) {
				nextToken =  jsonParser.nextToken();
				if ( nextToken == null ) break;
				if ( nextToken == JsonToken.START_ARRAY && "members".equals(lastText) ) break;
				lastText = jsonParser.getText();
			}

			while (true) {
				nextToken =  jsonParser.nextToken();
				if ( nextToken == null ) break;
				if ( nextToken == JsonToken.END_ARRAY ) break;
				Member member = mapper.readValue(jsonParser, Member.class);
System.out.println("member="+member.email);
				count = count + 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Could not stream");
		}

		// Update the job status
		context.setNrpsFinish(Instant.now());
		context.setNrpsCount(count);
		context.setNrpsStatus("Done");
		contextRepository.save(context);

	}

/*
{
  "id" : "https://lms.example.com/sections/2923/memberships",
  "context": {
	"id": "2923-abc",
	"label": "CPS 435",
	"title": "CPS 435 Learning Analytics",
  },
  "members" : [
	{
	  "status" : "Active",
	  "name": "Jane Q. Public",
	  "picture" : "https://platform.example.edu/jane.jpg",
	  "given_name" : "Jane",
	  "family_name" : "Doe",
	  "middle_name" : "Marie",
	  "email": "jane@platform.example.edu",
	  "user_id" : "0ae836b9-7fc9-4060-006f-27b2066ac545",
	  "lis_person_sourcedid": "59254-6782-12ab",
	  "roles": [
		"http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor"
	  ]
	}
  ]
}
 */

	/*
	 * Create a lineItem for a gradebook Column
	 */
	public String createLineItem(Site site, Long assignmentId,
		final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition)
	{
		String contextGuid = site.getId();
System.out.println("createLineItem site="+site.getId());
		Optional<Context> optContext = contextRepository.findById(contextGuid);
		Context context = null;
		if ( optContext.isPresent() ) {
			context = optContext.get();
		}

		if ( context == null ) {
			log.info("Context notfound {}", contextGuid);
			return null;
		}

		String tenantGuid = context.getTenant().getId();
		String lineItemsUrl	= context.getLineItems();

		if (StringUtils.isEmpty(tenantGuid)) {
			log.info("Context {} does not have a tenant.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		if (StringUtils.isEmpty(lineItemsUrl)) {
			log.info("Context {} does not have LineItems URL.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		// Load the Tenant
		Optional<Tenant> optTenant = tenantRepository.findById(tenantGuid);
		Tenant tenant = null;
		if ( optTenant.isPresent() ) {
			tenant = optTenant.get();
		}

		if ( tenant == null ) {
			log.info("Tenant notfound {}", tenantGuid);
			return null;
		}

		String clientId = tenant.getClientId();
		String deploymentId = tenant.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( StringUtils.isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;
		boolean trustEmail = ! Boolean.FALSE.equals(tenant.getTrustEmail());

		if (StringUtils.isEmpty(clientId)) {
			log.info("Tenant {} does not have clientId.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		if (StringUtils.isEmpty(deploymentId)) {
			log.info("Tenant {} does not have deploymentId.  Scores will NOT be synchronized.", tenantGuid);
			return null;
		}

		if (StringUtils.isEmpty(oidcTokenUrl)) {
			log.info("Tenant {} does not have an OIDC Token URL.  Scores will NOT be synchronized.", contextGuid);
			return null;
		}

		// Looks like we have the requisite strings in variables :)
		// https://www.imsglobal.org/spec/lti-ags/v2p0/#creating-a-new-line-item
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		AccessToken lineItemsAccessToken = LTI13AccessTokenUtil.getLineItemsToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience);
		if ( lineItemsAccessToken == null || StringUtils.isEmpty(lineItemsAccessToken.access_token) ) {
			log.info("Could not retrieve lineItems token from {}.  Scores will NOT be synchronized.", oidcTokenUrl);
			return null;
		}

System.out.println("lineItemsAccessToken.access_token="+lineItemsAccessToken.access_token);

		// lineItem
		Map<String, String> headers = new TreeMap<String, String>();
		headers.put("Authorization", "Bearer "+lineItemsAccessToken.access_token);
		headers.put("Content-Type", LineItem.MIME_TYPE);

		LineItem li = new LineItem();
		li.scoreMaximum = assignmentDefinition.getPoints();
		li.label = assignmentDefinition.getName();
		li.tag = "42";
		li.resourceId = assignmentId.toString();
System.out.println("li.resourceId="+li.resourceId);
		// li.startDateTime
		// li.endDateTime =  assignmentDefinition.getDueDate();
		String body = li.prettyPrintLog();

		 try {
			HttpResponse<String> response = HttpClientUtil.sendPost(lineItemsUrl, body, headers);
			body = response.body();
		} catch (Exception e) {
			log.error("Error creating lineItem at {}", lineItemsUrl);
			return null;
		}

		// Create and configure an ObjectMapper instance
		ObjectMapper mapper = JacksonUtil.getLaxObjectMapper();
		try {
			LineItem returnedItem = mapper.readValue(body, LineItem.class);

			if ( returnedItem != null ) {
				String retval = returnedItem.id;
System.out.println("returning linkitem id="+retval);
				if ( StringUtils.isNotEmpty(retval) ) return retval;
			}
		} catch ( Exception e ) {
			log.error("Error parsing lineItem at {}", lineItemsUrl);
			return null;
		}
		return null;

	}

	/*
     * Send a score to the calling LMS
     */
	// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
	// https://www.imsglobal.org/spec/lti-ags/v2p0#comment-0
    @Transactional(readOnly = true)
    public void processGradeEvent(Event event)
	{
		String[] parts = StringUtils.split(event.getResource(), '/');
		if (parts.length < 5) return;
		final String source = parts[0];
System.out.println("source="+source);
		if ( ! "gradebookng".equals(source) ) return;

		System.out.println("processGradeEvent "+event.getResource());

		final String gradebookId = parts[1];
		final String itemId = parts[2];
		final String studentId = parts[3];
		final String scoreStr = parts[4];
		final String context = event.getContext();
		log.debug("Updating score for user {} for item {} with score {} in gradebook {} by {}", studentId, itemId, scoreStr, gradebookId, source);

		Subject subject = subjectRepository.findBySakaiUserId(studentId);

		org.sakaiproject.service.gradebook.shared.Assignment gradebookAssignment = null;
		try {
			gradebookAssignment = gradebookService.getAssignmentByNameOrId(event.getContext(), itemId);
		} catch (AssessmentNotFoundException anfe) {
			log.warn("Can't retrieve gradebook assignment for gradebook {} and item {}, {}", gradebookId, itemId, anfe.getMessage());
			return;
		}

		String lineItem = gradebookAssignment.getLineItem();

		CommentDefinition commentDef = gradebookService.getAssignmentScoreComment(context, gradebookAssignment.getId(), studentId);
		String comment = null;
		if ( commentDef != null ) comment = commentDef.getCommentText();

		Tenant tenant = subject.getTenant();
		String clientId = tenant.getClientId();
		String deploymentId = tenant.getDeploymentId();
		String oidcTokenUrl = tenant.getOidcToken();
		String oidcAudience = tenant.getOidcAudience();
		if ( StringUtils.isEmpty(oidcAudience) ) oidcAudience = oidcTokenUrl;

		// Looks like we have the requisite strings in variables :)
		KeyPair keyPair = SakaiKeySetUtil.getCurrent();
		AccessToken scoreAccessToken = LTI13AccessTokenUtil.getScoreToken(oidcTokenUrl, keyPair, clientId, deploymentId, oidcAudience);
		if ( scoreAccessToken == null || StringUtils.isEmpty(scoreAccessToken.access_token) ) {
			log.info("Could not retrieve lineItems token from {}.  Scores will NOT be synchronized.", oidcTokenUrl);
			return;
		}

		// Lets send a score
		// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
		// https://www.imsglobal.org/spec/lti-ags/v2p0#comment-0
		Map<String, String> headers = new TreeMap<String, String>();
		headers.put("Authorization", "Bearer "+scoreAccessToken.access_token);
		headers.put("Content-Type", Score.MIME_TYPE);

		Score score = new Score();
		score.scoreGiven = new Double(scoreStr);
		score.scoreMaximum = gradebookAssignment.getPoints();
		score.comment = comment;
		score.userId = subject.getSubject();
System.out.println("score="+score.prettyPrintLog());
		String body = score.prettyPrintLog();
		String scoreUrl = lineItem + "/scores";

		 try {
			HttpResponse<String> response = HttpClientUtil.sendPost(scoreUrl, body, headers);
			body = response.body();
System.out.println("body="+body);
		} catch (Exception e) {
			log.error("Error setting score at {}", scoreUrl);
			return;
		}


	}

/*

https://www.imsglobal.org/spec/lti-ags/v2p0#score-service-media-type-and-schema

POST lineitem URL/scores
Content-Type: application/vnd.ims.lis.v1.score+json
Authentication: Bearer 89042.hfkh84390xaw3m
{
  "timestamp": "2017-04-16T18:54:36.736+00:00",
  "scoreGiven" : 83,
  "scoreMaximum" : 100,
  "comment" : "This is exceptional work.",
  "activityProgress" : "Completed",
  "gradingProgress": "FullyGraded",
  "userId" : "5323497"
}
 *
*/

// vim: tabstop=4 noet

}

