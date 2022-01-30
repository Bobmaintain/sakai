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

package org.sakaiproject.plus.impl.repository;

import java.util.List;

// TODO: Earle - Shouldn't this be a JPA thing?
import org.hibernate.criterion.Restrictions;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.Root;

import org.sakaiproject.plus.api.model.Score;
import org.sakaiproject.plus.api.model.Subject;
import org.sakaiproject.plus.api.repository.ScoreRepository;
import org.sakaiproject.springframework.data.SpringCrudRepositoryImpl;

import org.springframework.transaction.annotation.Transactional;

public class ScoreRepositoryImpl extends SpringCrudRepositoryImpl<Score, String>  implements ScoreRepository {

	// We are mostly loading individual Scores to Update them if they exist
	@Transactional
	public Score findBySubjectAndColumn(Subject subject, Long gradeBookColumn)
	{
        // TODO: Figure out LIMIT 1 without using a list hack - sheesh it is a uniqueness constraint
        List<Score> list = (List<Score>) sessionFactory.getCurrentSession().createCriteria(Score.class)
            .add(Restrictions.eq("subject", subject))
            .add(Restrictions.eq("gradeBookColumnId", gradeBookColumn))
            .list();
        if ( list == null || list.size() < 1 ) return null;
        return list.get(0);
    }

	// TODO: DO THIS THE JPA WAY - borrowed from conversations..PostRepositoryImpl.java
	// https://www.baeldung.com/hibernate-criteria-queries
	public Integer deleteBySubjectAndColumn(Subject subject, Long gradeBookColumn)
	{
		return sessionFactory.getCurrentSession()
            .createQuery("delete from org.sakaiproject.plus.api.model.Score where SAKAI_GRADABLE_OBJECT_ID = :gradeBookColumn and SUBJECT_GUID = :subject")
            .setString("subject", subject.getId())
            .setLong("gradeBookColumn", gradeBookColumn)
			.executeUpdate();
    }
}
