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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class BaseLTI implements Serializable {

	public static final int LENGTH_GUID = 36;
	public static final int LENGTH_URI = 500;
	public static final int LENGTH_TITLE = 500;
	public static final int LENGTH_EXTERNAL_ID = 200;
	public static final int LENGTH_MEDIUMTEXT = 4000;  // Less than 4096 because Oracle
	public static final int LENGTH_SAKAI_ID = 99;

    @Column(name = "UPDATED_AT", nullable = true)
    private Instant updatedAt;

    @Column(name = "SENT_AT", nullable = true)
    private Instant sentAt;

    @Column(name = "SUCCESS", length=200)
    private Boolean success = Boolean.TRUE;

    @Column(name = "STATUS", length=200, nullable = true)
    private String status;

    @Lob
    @Column(name = "DEBUG_LOG")
    private String debugLog;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = true)
    private Instant created_at;

    @Column(name = "MODIFIER", length = LENGTH_SAKAI_ID)
    private String modifier;

	@LastModifiedDate
    @Column(name = "MODIFIED_AT")
    private Instant modified_at;

    @Column(name = "DELETED")
    private Boolean deleted = Boolean.FALSE;

    @Column(name = "DELETOR", length = LENGTH_SAKAI_ID)
    private String deletor;

    @Column(name = "DELETED_AT")
    private Instant deleted_at;

    @Column(name = "LOGIN_COUNT")
    private Integer login_count;

    @Column(name = "LOGIN_IP", length=64)
    private String login_ip;

    @Column(name = "LOGIN_USER", length = LENGTH_SAKAI_ID)
    private String login_user;

    @Column(name = "LOGIN_AT")
    private Instant login_at;

    @Lob
    @Column(name = "JSON")
    private String json;

}
