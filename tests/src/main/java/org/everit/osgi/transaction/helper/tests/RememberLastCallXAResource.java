/**
 * This file is part of Everit - Transaction Helper Tests.
 *
 * Everit - Transaction Helper Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Transaction Helper Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Transaction Helper Tests.  If not, see <http://www.gnu.org/licenses/>.
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
