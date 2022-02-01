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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PLUS_LINEITEM",
  indexes = { @Index(columnList = "RESOURCE_ID, CONTEXT_GUID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "RESOURCE_ID", "CONTEXT_GUID" }) }
)

// https://www.imsglobal.org/spec/lti-ags/v2p0#line-item-service-scope-and-allowed-http-methods
@Getter
@Setter
public class LineItem extends Upstream implements PersistableEntity<Long> {

	// This is in effect a 1-to-1 with GB_GRADABLE_OBJECT_T.ID
	@Id
	@Column(name = "SAKAI_GRADABLE_OBJECT_ID", unique=true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "CONTEXT_GUID", nullable = false)
	private Context context;

	// Can optionally belong to a link
	@OneToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "LINK_GUID", nullable = true)
	private Link link;

	// The AGS resourceId - recommended
	@Column(name = "RESOURCE_ID", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	private String resourceId;

	@Column(name = "TAG", length = BaseLTI.LENGTH_EXTERNAL_ID, nullable = true)
	private String tag;

	@Column(name = "LABEL", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String label;

	@Column(name = "SCOREMAXIMUM")
	private Double scoreMaximum;

	@Column(name = "STARTDATETIME")
	private Instant startDateTime;

	@Column(name = "ENDDATETIME")
	private Instant endDateTime;

	// vim: tabstop=4 noet
}

/*
{
  "id" : "https://lms.example.com/context/2923/lineitems/1",
  "scoreMaximum" : 60,
  "label" : "Chapter 5 Test",
  "resourceId" : "a-9334df-33",
  "tag" : "grade",
  "resourceLinkId" : "1g3k4dlk49fk",
  "startDateTime": "2018-03-06T20:05:02Z",
  "endDateTime": "2018-04-06T22:05:03Z"
}
*/
