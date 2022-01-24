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

package org.sakaiproject.plus.api.service;

import java.util.Map;

import org.sakaiproject.lti.api.LTIException;

import org.sakaiproject.plus.api.Launch;
import org.sakaiproject.plus.api.model.Tenant;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.model.Context;
import org.sakaiproject.plus.api.model.Link;

import org.tsugi.lti13.objects.LaunchJWT;

import org.sakaiproject.user.api.User;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.event.api.Event;

public interface PlusService {

	static final String PLUS_PROPERTY = "plus_site";

	/*
	 * Note whether or not this system has Plus enabled
	 */
	boolean enabled();

	/*
	 * Note whether or not a Site has Plus enabled
	 */
	boolean enabled(Site site);

	/*
	 * Get a payload map from a LaunchJWT
	 */
	Map<String,String> getPayloadFromLaunchJWT(Tenant tenant, LaunchJWT launchJWT);

	/*
	 * Handle the initial launch - creating objects as needed (a.k.a. The BIG LEFT JOIN)
	 */
	Launch updateAll(LaunchJWT tokenBody, Tenant tenant)
			throws LTIException;

	/*
	 * Make sure the Subject knows about the chosen user
	 */
	void connectSubjectAndUser(Subject subject, User user)
			throws LTIException;

	/*
	 * Make sure the Context knows about the chosen site
	 */
	void connectContextAndSite(Context context, Site site)
			throws LTIException;

	/*
	 * Make sure the Link knows about the chosen placement
	 */
	void connectLinkAndPlacement(Link link, String placementId)
			throws LTIException;

	/*
	 * Retrieve Context Memberships from calling LMS and update the site
	 */
	void syncSiteMemberships(String contextGuid, Site site)
			throws LTIException;

	/*
	 * Create a lineItem for a gradebook Column
	 */
	String createLineItem(Site site, Long assignmentId,
		final org.sakaiproject.service.gradebook.shared.Assignment assignmentDefinition);

	/*
	 * Send a score to the calling LMS
	 */
	// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
	void processGradeEvent(Event event);

	// vim: tabstop=4 noet
}
