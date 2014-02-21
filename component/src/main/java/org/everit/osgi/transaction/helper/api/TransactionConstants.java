/**
 * This file is part of Everit - Transaction Helper.
 *
 * Everit - Transaction Helper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Transaction Helper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Transaction Helper.  If not, see <http://www.gnu.org/licenses/>.
 */
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
