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

import java.time.Instant;

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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PLUS_CONTEXT",
  indexes = { @Index(columnList = "CONTEXT, TENNANT_GUID, SAKAI_SITE_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "CONTEXT", "TENNANT_GUID" }) }
)
@Getter
@Setter
public class Context extends Upstream implements PersistableEntity<String> {

	@Id
	@Column(name = "CONTEXT_GUID", length = BaseLTI.LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "CONTEXT", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = false)
	private String context;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENNANT_GUID", nullable = false)
	private Tenant tenant;

	@Column(name = "SAKAI_SITE_ID", length = BaseLTI.LENGTH_SAKAI_ID, nullable = true)
	private String sakaiSiteId;

	@Column(name = "TITLE", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String title;

	@Column(name = "LABEL", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String label;

	// launchjwt.endpoint.lineitems
	@Column(name = "LINEITEMS", length = BaseLTI.LENGTH_URI, nullable = true)
	private String lineItems;

	@Column(name = "LINEITEMS_TOKEN", length = BaseLTI.LENGTH_URI, nullable = true)
	private String lineItemsToken;

	@Column(name = "GRADE_TOKEN", length = BaseLTI.LENGTH_URI, nullable = true)
	private String gradeToken;

	// launchjwt.names_and_roles.context_memberships_url
	@Column(name = "CONTEXT_MEMBERSHIPS", length = BaseLTI.LENGTH_URI, nullable = true)
	private String contextMemberships;

	@Column(name = "NRPS_TOKEN", length = BaseLTI.LENGTH_URI, nullable = true)
	private String nrpsToken;

	@Column(name = "NRPS_JOB_START", nullable = true)
	private Instant nrpsStart;

	@Column(name = "NRPS_JOB_FINISH", nullable = true)
	private Instant nrpsFinish;

	@Column(name = "NRPS_JOB_STATUS", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String nrpsStatus;

	@Column(name = "NRPS_JOB_COUNT", nullable = true)
	private Long nrpsCount;

	// vim: tabstop=4 noet
}
