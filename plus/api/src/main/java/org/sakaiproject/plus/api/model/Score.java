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

import java.lang.Enum;
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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.hibernate.annotations.GenericGenerator;

import org.sakaiproject.springframework.data.PersistableEntity;

import org.apache.commons.lang3.EnumUtils;

import lombok.Getter;
import lombok.Setter;

// https://www.imsglobal.org/spec/lti-ags/v2p0#score-publish-service
@Entity
@Table(name = "PLUS_SCORE",
  indexes = { @Index(columnList = "SUBJECT_GUID, SAKAI_GRADABLE_OBJECT_ID") },
  // The "logical key" (i.e. when to update versus insert new is subject / column) 
  uniqueConstraints = { @UniqueConstraint(columnNames = { "SUBJECT_GUID", "SAKAI_GRADABLE_OBJECT_ID" }) }
)
@Getter
@Setter
public class Score extends Upstream implements PersistableEntity<String> {

	// These enums *names* must match the values in the spec as they are matched with strings at times
	public enum ACTIVITY_PROGRESS {
		Initialized, Started, InProgress, Submitted, Completed;
	};

	public enum GRADING_PROGRESS {
		FullyGraded, Pending, PendingManual, Failed;
	};

    @Id
    @Column(name = "SCORE_GUID", length = BaseLTI.LENGTH_GUID, nullable = false)
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    private String id;

	// We have the line item string from SAKAI_GRADE_RECORD_ID - do we need anything else?
	// GB_GRADE_RECORD contains the lineitem String we are to use
	// so we don't need to link to the PLUS LineItem instance here
	@Column(name = "SAKAI_GRADABLE_OBJECT_ID", nullable = false)
	private Long gradeBookColumnId;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "SUBJECT_GUID", nullable = false)
	private Subject subject;

	@Column(name = "ACTIVITY_PROGRESS", nullable = true)
	@Enumerated(EnumType.ORDINAL)
	private ACTIVITY_PROGRESS activityProgress;

	@Column(name = "GRADING_PROGRESS", nullable = true)
	@Enumerated(EnumType.ORDINAL)
	private GRADING_PROGRESS gradingProgress;

	@Column(name = "SCORE_GIVEN", nullable = true)
	private Double scoreGiven;

	@Column(name = "SCORE_MAXIMUM", nullable = true)
	private Double scoreMaximum;

	@Column(name = "COMMENT", length=200, nullable = true)
	private String comment;

	// TODO: Get a look at this from Earle
	// The valueOf() call will throw a runtime exception if the string is not valid
	// This is expected to be used only with the predefined constants in
	// org.tsugi.ags2.objects.Score
	public void setGradingProgress(String newStatus)
	{
		GRADING_PROGRESS gp = GRADING_PROGRESS.valueOf(newStatus);
		gradingProgress = gp;
	}

	public void setActivityProgress(String newStatus)
	{
		ACTIVITY_PROGRESS ap = ACTIVITY_PROGRESS.valueOf(newStatus);
		activityProgress = ap;
	}

	// vim: tabstop=4 noet

}
