package org.everit.osgi.transaction.helper.tests;

/*
 * Copyright (c) 2011, Everit Kft.
 *
 * All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */

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
    private int status;

    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        status = Status.STATUS_COMMITTED;
    }

    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        status = XAResourceStatus.STATUS_END;
    }

    @Override
    public void forget(final Xid xid) throws XAException {
        status = DEFAULT_STATUS;
    }

    public int getStatus() {
        return status;
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        status = DEFAULT_STATUS;
        return 0;
    }

    @Override
    public boolean isSameRM(final XAResource xares) throws XAException {
        status = DEFAULT_STATUS;
        return true;
    }

    @Override
    public int prepare(final Xid xid) throws XAException {
        status = DEFAULT_STATUS;
        return 0;
    }

    @Override
    public Xid[] recover(final int flag) throws XAException {
        status = DEFAULT_STATUS;
        return new Xid[0];
    }

    @Override
    public void rollback(final Xid xid) throws XAException {
        status = Status.STATUS_ROLLEDBACK;
    }

    @Override
    public boolean setTransactionTimeout(final int seconds) throws XAException {
        status = DEFAULT_STATUS;
        return true;
    }

    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        status = XAResourceStatus.STATUS_START;
    }

}
