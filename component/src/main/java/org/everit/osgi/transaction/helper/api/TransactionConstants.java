/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.transaction.helper.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.Status;

/**
 * Constants for Transaction Helper.
 */
public final class TransactionConstants {

  public static final Map<Integer, String> STATUS_NAME_BY_CODE;

  static {
    Map<Integer, String> statusNameByCode = new HashMap<Integer, String>();
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
