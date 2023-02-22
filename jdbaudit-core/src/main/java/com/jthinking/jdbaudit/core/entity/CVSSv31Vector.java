/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jthinking.jdbaudit.core.entity;

import com.jthinking.jdbaudit.core.entity.cvss31.*;

public class CVSSv31Vector {
    
    private AttackVector attackVector;
    private AttackComplexity attackComplexity;
    private PrivilegesRequired privilegesRequired;
    private UserInteraction userInteraction;
    private Scope scope;
    private ConfidentialityImpact confidentialityImpact;
    private IntegrityImpact integrityImpact;
    private AvailabilityImpact availabilityImpact;

    public AttackVector getAttackVector() {
        return attackVector;
    }
    public void setAttackVector(AttackVector attackVector) {
        this.attackVector = attackVector;
    }
    public AttackComplexity getAttackComplexity() {
        return attackComplexity;
    }
    public void setAttackComplexity(AttackComplexity attackComplexity) {
        this.attackComplexity = attackComplexity;
    }
    public PrivilegesRequired getPrivilegesRequired() {
        return privilegesRequired;
    }
    public void setPrivilegesRequired(PrivilegesRequired privilegesRequired) {
        this.privilegesRequired = privilegesRequired;
    }
    public UserInteraction getUserInteraction() {
        return userInteraction;
    }
    public void setUserInteraction(UserInteraction userInteraction) {
        this.userInteraction = userInteraction;
    }
    public Scope getScope() {
        return scope;
    }
    public void setScope(Scope scope) {
        this.scope = scope;
    }
    public ConfidentialityImpact getConfidentialityImpact() {
        return confidentialityImpact;
    }
    public void setConfidentialityImpact(ConfidentialityImpact confidentialityImpact) {
        this.confidentialityImpact = confidentialityImpact;
    }
    public IntegrityImpact getIntegrityImpact() {
        return integrityImpact;
    }
    public void setIntegrityImpact(IntegrityImpact integrityImpact) {
        this.integrityImpact = integrityImpact;
    }
    public AvailabilityImpact getAvailabilityImpact() {
        return availabilityImpact;
    }
    public void setAvailabilityImpact(AvailabilityImpact availabilityImpact) {
        this.availabilityImpact = availabilityImpact;
    }

    
}
