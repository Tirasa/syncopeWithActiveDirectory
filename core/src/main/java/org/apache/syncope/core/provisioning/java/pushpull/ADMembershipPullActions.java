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

import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.syncope.common.lib.types.ConnConfProperty;
import org.apache.syncope.core.provisioning.api.Connector;
import org.apache.syncope.core.provisioning.java.pushpull.LDAPMembershipPullActions;

public class ADMembershipPullActions extends LDAPMembershipPullActions {

    @Override
    protected String getGroupMembershipAttrName(final Connector connector) {
        ConnConfProperty groupMembership = IterableUtils.find(connector.getConnInstance().getConf(),
                new Predicate<ConnConfProperty>() {

            @Override
            public boolean evaluate(final ConnConfProperty property) {
                return "groupMemberReferenceAttribute".equals(property.getSchema().getName())
                        && property.getValues() != null && !property.getValues().isEmpty();
            }
        });

        return groupMembership == null
                ? "member"
                : (String) groupMembership.getValues().get(0);
    }

}
