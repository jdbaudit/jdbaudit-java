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

import com.jthinking.jdbaudit.core.entity.CVSSv31Vector;
import com.jthinking.jdbaudit.core.entity.Severity;
import com.jthinking.jdbaudit.core.entity.cvss31.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * CVSSv3.1计算器
 * @author Jia Bochao
 */
public class CVSSv31Calculator {

    public static Severity getSeverity(CVSSv31Vector vector) {
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

    public static String vectorString(CVSSv31Vector cvssv31Vector) {
        return vectorString(cvssv31Vector.getAttackVector(), cvssv31Vector.getAttackComplexity(), cvssv31Vector.getPrivilegesRequired(),
                        cvssv31Vector.getUserInteraction(), cvssv31Vector.getScope(), cvssv31Vector.getConfidentialityImpact(), 
                        cvssv31Vector.getIntegrityImpact(), cvssv31Vector.getAvailabilityImpact());
    }
    
    public static String vectorString(AttackVector attackVector,
                                      AttackComplexity attackComplexity,
                                      PrivilegesRequired privilegesRequired,
                                      UserInteraction userInteraction,
                                      Scope scope,
                                      ConfidentialityImpact confidentialityImpact,
                                      IntegrityImpact integrityImpact,
                                      AvailabilityImpact availabilityImpact) {
        return String.format("CVSS:3.1/AV:%s/AC:%s/PR:%s/UI:%s/S:%s/C:%s/I:%s/A:%s",
                attackVector.getShortName(),
                attackComplexity.getShortName(),
                privilegesRequired.getShortName(),
                userInteraction.getShortName(),
                scope.getShortName(),
                confidentialityImpact.getShortName(),
                integrityImpact.getShortName(),
                availabilityImpact.getShortName());
    }

    public static double baseScore(CVSSv31Vector cvssv31Vector) {
        return baseScore(cvssv31Vector.getAttackVector(), cvssv31Vector.getAttackComplexity(), cvssv31Vector.getPrivilegesRequired(),
                        cvssv31Vector.getUserInteraction(), cvssv31Vector.getScope(), cvssv31Vector.getConfidentialityImpact(), 
                        cvssv31Vector.getIntegrityImpact(), cvssv31Vector.getAvailabilityImpact());
    }

    public static double baseScore(AttackVector attackVector,
                                   AttackComplexity attackComplexity,
                                   PrivilegesRequired privilegesRequired,
                                   UserInteraction userInteraction,
                                   Scope scope,
                                   ConfidentialityImpact confidentialityImpact,
                                   IntegrityImpact integrityImpact,
                                   AvailabilityImpact availabilityImpact) {
        double exploitability = exploitability(attackVector, attackComplexity, privilegesRequired, userInteraction, scope);
        double impact = impact(scope, confidentialityImpact, integrityImpact, availabilityImpact);
        return baseScore(scope, exploitability, impact);
    }

    private static double baseScore(Scope scope, double exploitability, double impact) {
        if (impact <= 0) {
            return 0;
        } else {
            switch (scope) {
                case Unchanged:
                    return BigDecimal.valueOf(Math.min((impact + exploitability), 10)).setScale(1, RoundingMode.CEILING).doubleValue();
                case Changed:
                    return BigDecimal.valueOf(Math.min(1.08 * (impact + exploitability), 10)).setScale(1, RoundingMode.CEILING).doubleValue();
                default:
                    throw new RuntimeException("Scope unsupported");
            }
        }
    }

    private static double iss(double confidentiality, double integrity, double availability) {
        return 1 - ( (1 - confidentiality) * (1 - integrity) * (1 - availability) );
    }

    private static double impact(Scope scope, double confidentialityImpact,
                                double integrityImpact,
                                double availabilityImpact) {
        double iss = iss(confidentialityImpact, integrityImpact, availabilityImpact);
        switch (scope) {
            case Unchanged:
                return 6.42 * iss;
            case Changed:
                return 7.52 * (iss - 0.029) - 3.25 * Math.pow(iss - 0.02, 15);
            default:
                throw new RuntimeException("Scope unsupported");
        }
    }

    private static double impact(Scope scope, ConfidentialityImpact confidentialityImpact,
                                IntegrityImpact integrityImpact,
                                AvailabilityImpact availabilityImpact) {
        return impact(scope, confidentialityImpact.getScore(), integrityImpact.getScore(), availabilityImpact.getScore());
    }

    private static double exploitability(double attackVector,
                                         double attackComplexity,
                                         double privilegesRequired,
                                         double userInteraction) {
        return 8.22 * attackVector * attackComplexity * privilegesRequired * userInteraction;
    }

    private static double exploitability(AttackVector attackVector,
                                         AttackComplexity attackComplexity,
                                         PrivilegesRequired privilegesRequired,
                                         UserInteraction userInteraction,
                                         Scope scope) {

        double score = privilegesRequired.getScore();

        switch (privilegesRequired) {
            case High:
                if (scope.equals(Scope.Changed)) {
                    score = 0.5;
                }
                break;
            case Low:
                if (scope.equals(Scope.Changed)) {
                    score = 0.68;
                }
                break;
            default:
                break;
        }

        return exploitability(attackVector.getScore(),
                attackComplexity.getScore(),
                score,
                userInteraction.getScore());
    }

}
