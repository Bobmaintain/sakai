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

package org.sakaiproject.plus.api.model;

import java.util.Map;
import java.util.HashMap;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.UniqueConstraint;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PLUS_TENANT",
  indexes = { @Index(columnList = "ISSUER, CLIENT_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "ISSUER", "CLIENT_ID" }) }
)
@Getter
@Setter
public class Tenant extends Upstream implements PersistableEntity<String> {

	@Id
	@Column(name = "TENNANT_GUID", length = BaseLTI.LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "TITLE", length = BaseLTI.LENGTH_TITLE, nullable = false)
	private String title;

	@Column(name = "DESCRIPTION", length = BaseLTI.LENGTH_MEDIUMTEXT, nullable = true)
	private String description;

	// Issuer and client_id can be null while a key is being built but a key is not usable
	// until both fields are defined and the other values are present
	@Column(name = "ISSUER", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	protected String issuer;

	@Column(name = "CLIENT_ID", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	private String clientId;

	@Column(name = "DEPLOYMENT_ID", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	private String deploymentId;

	// Default this to true - it is the most common approach
	@Column(name = "TRUST_EMAIL")
	private Boolean trustEmail = Boolean.TRUE;

	@Column(name = "TIMEZONE", length = 100, nullable = true)
	private String timeZone;

	@Column(name = "ALLOWED_TOOLS", length = 500, nullable = true)
	private String allowedTools;

	@Column(name = "VERBOSE")
	private Boolean verbose = Boolean.FALSE;

	@Column(name = "OIDC_AUTH", length = BaseLTI.LENGTH_URI, nullable = true)
	private String oidcAuth;

	@Column(name = "OIDC_KEYSET", length = BaseLTI.LENGTH_URI, nullable = true)
	private String oidcKeySet;

	@Column(name = "OIDC_TOKEN", length = BaseLTI.LENGTH_URI, nullable = true)
	private String oidcToken;

	// This is usually optional except for D2L
	@Column(name = "OIDC_AUDIENCE", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	private String oidcAudience;

	@Column(name = "CACHE_KEYSET", length = BaseLTI.LENGTH_MEDIUMTEXT, nullable = true)
	private String cacheKeySet;

	public boolean isDraft()
	{
		if ( issuer == null || clientId == null || deploymentId == null ||
				oidcAuth == null || oidcKeySet == null || oidcToken == null ) return true;

		if ( issuer.length() < 1 || clientId.length() < 1 ||
				deploymentId.length() < 1 || oidcAuth.length() < 1 ||
				oidcKeySet.length() < 1 || oidcToken.length() < 1 ) return true;
		return false;
	}

	// vim: tabstop=4 noet

}
