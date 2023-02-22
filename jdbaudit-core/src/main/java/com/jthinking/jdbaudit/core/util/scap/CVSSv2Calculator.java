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
package com.jthinking.jdbaudit.core.util.scap;

import com.jthinking.jdbaudit.core.entity.CVSSv2Vector;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.core.entity.cvss2.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CVSSv2计算器
 * @author Jia Bochao
 */
public class CVSSv2Calculator {
    
    public static Severity getSeverity(CVSSv2Vector vector) {
        double baseScore = baseScore(vector);
        if (baseScore == 0.0) {
            return Severity.NONE;
        } else if (baseScore >= 0.1 && baseScore <= 3.9) {
            return Severity.LOW;
        } else if (baseScore >= 4.0 && baseScore <= 6.9) {
            return Severity.MEDIUM;
        } else if (baseScore >= 7.0 && baseScore <= 8.9) {
            return Severity.HIGH;
        } else if (baseScore >= 9.0 && baseScore <= 10.0) {
            return Severity.CRITICAL;
        } else {
            return Severity.NONE;
        }
    }
    
    public static String vectorString(CVSSv2Vector cvssv2Vector) {
        return vectorString(cvssv2Vector.getAccessVector(), cvssv2Vector.getAccessComplexity(), cvssv2Vector.getAuthentication(), 
        cvssv2Vector.getConfidentialityImpact(), cvssv2Vector.getIntegrityImpact(), cvssv2Vector.getAvailabilityImpact());
    }
    
    public static String vectorString(AccessVector accessVectorEnum,
                                      AccessComplexity accessComplexityEnum,
                                      Authentication authenticationEnum,
                                      ConfidentialityImpact confidentialityImpactEnum,
                                      IntegrityImpact integrityImpact,
                                      AvailabilityImpact availabilityImpact) {
        return String.format("AV:%s/AC:%s/Au:%s/C:%s/I:%s/A:%s",
                accessVectorEnum.getShortName(),
                accessComplexityEnum.getShortName(),
                authenticationEnum.getShortName(),
                confidentialityImpactEnum.getShortName(),
                integrityImpact.getShortName(),
                availabilityImpact.getShortName());
    }

    public static double baseScore(CVSSv2Vector cvssv2Vector) {
        return baseScore(cvssv2Vector.getAccessVector(), cvssv2Vector.getAccessComplexity(), cvssv2Vector.getAuthentication(), 
        cvssv2Vector.getConfidentialityImpact(), cvssv2Vector.getIntegrityImpact(), cvssv2Vector.getAvailabilityImpact());
    }
    
    public static double baseScore(AccessVector accessVectorEnum,
                                   AccessComplexity accessComplexityEnum,
                                   Authentication authenticationEnum,
                                   ConfidentialityImpact confidentialityImpactEnum,
                                   IntegrityImpact integrityImpactEnum,
                                   AvailabilityImpact availabilityImpactEnum) {
        double exploitability = exploitability(accessVectorEnum, accessComplexityEnum, authenticationEnum);
        double impact = impact(confidentialityImpactEnum, integrityImpactEnum, availabilityImpactEnum);
        return baseScore(exploitability, impact);
    }

    private static double baseScore(double exploitability, double impact) {
        double baseScore = ( ( 0.6 * impact) + ( 0.4 * exploitability ) - 1.5 ) * fImpact(impact);
        return new BigDecimal(baseScore).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }


    private static double impact(double confidentialityImpact,
                                double integrityImpact,
                                double availabilityImpact) {
        return 10.41 * ( 1 - ( 1 - confidentialityImpact ) * ( 1 - integrityImpact ) * ( 1 - availabilityImpact ) );
    }


    private static double impact(ConfidentialityImpact confidentialityImpact,
                                IntegrityImpact integrityImpact,
                                AvailabilityImpact availabilityImpact) {
        return impact(confidentialityImpact.getScore(), integrityImpact.getScore(), availabilityImpact.getScore());
    }

    private static double fImpact(double impact) {
        if (impact == .0) {
            return 0;
        } else {
            return 1.176;
        }
    }


    private static double exploitability(double accessVector, double accessComplexity, double authentication) {
        return 20 * accessVector * authentication * accessComplexity;
    }

    private static double exploitability(
            AccessVector accessVector,
            AccessComplexity accessComplexity,
            Authentication authentication) {
        return exploitability(accessVector.getScore(), accessComplexity.getScore(), authentication.getScore());
    }

}
