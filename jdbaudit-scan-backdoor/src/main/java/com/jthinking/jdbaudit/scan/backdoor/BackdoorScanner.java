package com.jthinking.jdbaudit.scan.backdoor;

import com.jthinking.jdbaudit.scan.audit.AuditScanner;

public class BackdoorScanner extends AuditScanner {

    @Override
    public String getId() {
        return "BACKDOOR";
    }
}
