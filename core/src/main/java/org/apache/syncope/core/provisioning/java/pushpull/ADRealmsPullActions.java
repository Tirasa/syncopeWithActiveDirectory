/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.provisioning.java.pushpull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.apache.syncope.common.lib.to.EntityTO;
import org.apache.syncope.common.lib.to.RealmTO;
import org.apache.syncope.core.persistence.api.dao.RealmDAO;
import org.apache.syncope.core.persistence.api.entity.Realm;
import org.apache.syncope.core.provisioning.api.AuditManager;
import org.apache.syncope.core.provisioning.api.notification.NotificationManager;
import org.apache.syncope.core.provisioning.api.pushpull.ProvisioningProfile;
import org.apache.syncope.core.provisioning.api.pushpull.ProvisioningReport;
import org.identityconnectors.framework.common.objects.SyncDelta;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

public class ADRealmsPullActions extends DefaultPullActions {

    @Autowired
    private RealmDAO realmDAO;

    @Autowired
    protected AuditManager auditManager;

    @Autowired
    protected NotificationManager notificationManager;

    @Override
    public void after(
            ProvisioningProfile<?, ?> profile,
            SyncDelta delta,
            EntityTO entity,
            ProvisioningReport result) throws JobExecutionException {

        if (entity instanceof RealmTO) {

            String name = delta.getObject().getName().getNameValue();
            String key = name.contains("ou") ? "ou" : "OU";

            if (StringUtils.countMatches(name, key) > 1) {
                String currentOu = StringUtils.substringBetween(name, key + "=", ",");

                String parent = "",
                        pattern1 = key + "=",
                        pattern2 = ",";
                Pattern p = Pattern.compile(Pattern.quote(pattern1) + "(?s)(.*?)" + Pattern.quote(pattern2));
                Matcher m = p.matcher(name);
                while (m.find()) {
                    String ou = m.group(1);
                    if (!ou.equals(currentOu)) {
                        parent = "/" + ou + parent;
                    }
                }

                if (StringUtils.isNotBlank(parent)) {
                    Realm parentRealm = realmDAO.findByFullPath(parent); // parent realm
                    Realm currentRealm = realmDAO.find(entity.getKey()); // current just created realm

                    currentRealm.setParent(parentRealm);
                    realmDAO.save(currentRealm);

                    result.setName(parentRealm.getFullPath() + "/" + currentRealm.getName());
                }
            }

        }

    }

}
