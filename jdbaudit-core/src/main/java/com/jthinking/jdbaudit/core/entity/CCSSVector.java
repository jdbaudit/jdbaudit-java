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

import com.jthinking.jdbaudit.core.entity.ccss.*;

public class CCSSVector {
    
    private AccessVector accessVector;

    private AccessComplexity accessComplexity;

    private Authentication authentication;

    private ConfidentialityImpact confidentialityImpact;

    private IntegrityImpact integrityImpact;

    private AvailabilityImpact availabilityImpact;

    private PrivilegeLevel privilegeLevel;

    private ExploitabilityMethod exploitabilityMethod;


    public AccessVector getAccessVector() {
        return accessVector;
    }

    public void setAccessVector(AccessVector accessVector) {
        this.accessVector = accessVector;
    }

    public AccessComplexity getAccessComplexity() {
        return accessComplexity;
    }

    public void setAccessComplexity(AccessComplexity accessComplexity) {
        this.accessComplexity = accessComplexity;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
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

    public PrivilegeLevel getPrivilegeLevel() {
        return privilegeLevel;
    }

    public void setPrivilegeLevel(PrivilegeLevel privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    public ExploitabilityMethod getExploitabilityMethod() {
        return exploitabilityMethod;
    }

    public void setExploitabilityMethod(ExploitabilityMethod exploitabilityMethod) {
        this.exploitabilityMethod = exploitabilityMethod;
    }
}
