/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
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
package org.everit.osgi.transaction.helper.tests;

import javax.transaction.Status;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * The simple {@link XAResource} implementations to the testing.
 */
public class RememberLastCallXAResource implements XAResource {

  /**
   * The resource default statuses.
   */
  private static final int DEFAULT_STATUS = 100;

  /**
   * The resource status.
   */
  private int status = DEFAULT_STATUS;

  private Xid xid = null;

  @Override
  public void commit(final Xid xid, final boolean onePhase) throws XAException {
    status = Status.STATUS_COMMITTED;
    this.xid = xid;
  }

  @Override
  public void end(final Xid xid, final int flags) throws XAException {
    status = XAResourceStatus.STATUS_END;
    this.xid = xid;
  }

  @Override
  public void forget(final Xid xid) throws XAException {
    this.xid = xid;
  }

  public int getStatus() {
    return status;
  }

  @Override
  public int getTransactionTimeout() throws XAException {
    return 0;
  }

  public Xid getXid() {
    return xid;
  }

  @Override
  public boolean isSameRM(final XAResource xares) throws XAException {
    return xares == this;
  }

  @Override
  public int prepare(final Xid xid) throws XAException {
    status = Status.STATUS_PREPARING;
    this.xid = xid;
    return 0;
  }

  @Override
  public Xid[] recover(final int flag) throws XAException {
    return new Xid[0];
  }

  @Override
  public void rollback(final Xid xid) throws XAException {
    status = Status.STATUS_ROLLEDBACK;
    this.xid = xid;
  }

  @Override
  public boolean setTransactionTimeout(final int seconds) throws XAException {
    return true;
  }

  @Override
  public void start(final Xid xid, final int flags) throws XAException {
    status = XAResourceStatus.STATUS_START;
    this.xid = xid;
  }
}
