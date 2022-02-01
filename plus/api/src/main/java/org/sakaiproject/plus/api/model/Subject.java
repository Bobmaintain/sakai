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
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PLUS_SUBJECT",
  indexes = { @Index(columnList = "SUBJECT, TENNANT_GUID, SAKAI_USER_ID") },
  uniqueConstraints = { @UniqueConstraint(columnNames = { "SUBJECT", "TENNANT_GUID" }) }
)
@Getter
@Setter
public class Subject extends Upstream implements PersistableEntity<String> {

	@Id
	@Column(name = "SUBJECT_GUID", length = BaseLTI.LENGTH_GUID, nullable = false)
	@GeneratedValue(generator = "uuid")
	@GenericGenerator(name = "uuid", strategy = "uuid2")
	private String id;

	@Column(name = "SAKAI_USER_ID", length = BaseLTI.LENGTH_SAKAI_ID, nullable = true)
	private String sakaiUserId;

	@Column(name = "SUBJECT", length = BaseLTI.LENGTH_URI, nullable = false)
	private String subject;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "TENNANT_GUID", nullable = false)
	private Tenant tenant;

	@Column(name = "DISPLAYNAME", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String displayName;

	@Column(name = "EMAIL", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String email;

	@Column(name = "LOCALE", length = BaseLTI.LENGTH_TITLE, nullable = true)
	private String locale;

	// vim: tabstop=4 noet
}
