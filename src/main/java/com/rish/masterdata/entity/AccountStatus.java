package com.rish.masterdata.entity;

public enum AccountStatus {
    PENDING_VERIFICATION, //→ prevents login before email confirmed
    ACTIVE,               //→ normal state
    INACTIVE,             //→ admin can deactivate without deleting
    LOCKED,               //→ security — brute force protection
    SUSPENDED,            //→ business rule — non payment etc
    DELETED,              //→ soft delete — never hard delete users legal/audit requirements
}
