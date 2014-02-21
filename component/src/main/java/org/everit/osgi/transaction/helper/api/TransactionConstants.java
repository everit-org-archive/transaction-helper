package org.everit.osgi.transaction.helper.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Status;

public final class TransactionConstants {

    public static final Map<Integer, String> STATUS_NAME_BY_CODE;

    static {
        Map<Integer, String> statusNameByCode = new HashMap<Integer, String>();
        statusNameByCode = new HashMap<Integer, String>();
        statusNameByCode.put(Status.STATUS_ACTIVE, "active");
        statusNameByCode.put(Status.STATUS_COMMITTED, "commited");
        statusNameByCode.put(Status.STATUS_COMMITTING, "commiting");
        statusNameByCode.put(Status.STATUS_MARKED_ROLLBACK, "marked_rollback");
        statusNameByCode.put(Status.STATUS_NO_TRANSACTION, "no_transaction");
        statusNameByCode.put(Status.STATUS_PREPARED, "prepared");
        statusNameByCode.put(Status.STATUS_PREPARING, "preparing");
        statusNameByCode.put(Status.STATUS_ROLLEDBACK, "rolledback");
        statusNameByCode.put(Status.STATUS_ROLLING_BACK, "rollingback");
        statusNameByCode.put(Status.STATUS_UNKNOWN, "unknown");
        STATUS_NAME_BY_CODE = Collections.unmodifiableMap(statusNameByCode);
    }

    private TransactionConstants() {
    }
}
