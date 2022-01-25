/**
 * Copyright (c) 2003-2020 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.plus.impl;

import java.util.Observable;
import java.util.Observer;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.service.gradebook.shared.AssessmentNotFoundException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.plus.api.PlusService;

import org.springframework.beans.factory.annotation.Autowired;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlusEventObserver implements Observer {

    @Autowired private EventTrackingService eventTrackingService;
    @Autowired private UserDirectoryService userDirectoryService;
    @Autowired private PlusService plusService;

    public void init() {
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy() {
        eventTrackingService.deleteObserver(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Event) {
            Event event = (Event) arg;
            if (event.getModify() && StringUtils.isNoneBlank(event.getEvent(), event.getContext())) {
                switch (event.getEvent()) {
                    case "gradebook.updateItemScore": // grade updated in gradebook lets attempt to update the submission
						plusService.processGradeEvent(event);
                        break;
                    default:
                        log.debug("This observer is not interested in event [{}]", event);
                        break;
                }
            }
        }
    }
}
