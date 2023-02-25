package com.jthinking.jdbaudit.scan.privilege;

import com.jthinking.jdbaudit.scan.audit.AuditScanner;

public class PrivilegeScanner extends AuditScanner {

    @Override
    public String getId() {
        return "PRIVILEGE";
    }


}
