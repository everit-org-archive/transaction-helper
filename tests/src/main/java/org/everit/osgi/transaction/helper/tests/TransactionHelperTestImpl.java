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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import junit.framework.Assert;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.component.api.Callback;
import org.everit.osgi.transaction.helper.component.api.TransactionHelper;

/**
 * Implementation of the {@link TransactionHelperTest}.
 */
@Component
@Service(serviceFactory = false)
@Property(name = "osgitest", value = "junit4")
public class TransactionHelperTestImpl implements TransactionHelperTest {

    /**
     * The {@link TransactionHelper} instance.
     */
    @Reference(bind = "setTransactionHelper", unbind = "unSetTransactionHelper")
    private TransactionHelper transactionHelper;

    /**
     * The {@link TransactionSynchronizationRegistry} instance.
     */
    @Reference(bind = "setTransactionSynchronizationRegistry", unbind = "unSetTransactionSynchronizationRegistry")
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    /**
     * The {@link TransactionManager} instance.
     */
    @Reference(bind = "setTransactionManager", unbind = "unSetTransactionManager")
    private TransactionManager transactionManager;

    /**
     * The basic Callback transaction which throw new RuntimeException.
     */
    private static final Callback<Void> RUNTIMEEXCEPTION_CB = new Callback<Void>() {

        @Override
        public Void execute() {
            throw new IllegalArgumentException("Test IllegalArgumentException");
        }
    };

    /**
     * The basis transactions when exist active transaction.
     */
    private void basicTransActionWhenExistActiveTransaction() {
        final int b = 2;
        final Callback<Integer> szumCB = new Callback<Integer>() {

            @Override
            public Integer execute() {
                int a = 0;
                Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
                return a + b;
            }
        };

        try {
            transactionManager.begin();
        } catch (NotSupportedException e) {
            Assert.assertNull(e);
        } catch (SystemException e) {
            Assert.assertNull(e);
        }

        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
        Integer sum = transactionHelper.doInTransaction(szumCB, true);
        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
        Assert.assertEquals(Integer.valueOf(2), sum);

        try {
            transactionHelper.doInTransaction(RUNTIMEEXCEPTION_CB, true);
            Assert.fail("expect runtimeexception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());

        sum = transactionHelper.doInTransaction(szumCB, false);
        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
        Assert.assertEquals(Integer.valueOf(2), sum);
        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());

        try {
            transactionHelper.doInTransaction(RUNTIMEEXCEPTION_CB, false);
            Assert.fail("expect runtimeexception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());

        try {
            transactionManager.commit();
        } catch (IllegalStateException e) {
            Assert.assertNull(e);
        } catch (SecurityException e) {
            Assert.assertNull(e);
        } catch (HeuristicMixedException e) {
            Assert.assertNull(e);
        } catch (HeuristicRollbackException e) {
            Assert.assertNull(e);
        } catch (RollbackException e) {
            Assert.assertNull(e);
        } catch (SystemException e) {
            Assert.assertNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * The basic transaction. Not exist transaction and test the requiredNew true and false.
     */
    private void basicTransactionWhenNoTransaction() {
        final int b = 2;
        final Callback<Integer> szumCB = new Callback<Integer>() {

            @Override
            public Integer execute() {
                int a = 0;
                Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
                return a + b;
            }
        };

        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Integer sum = transactionHelper.doInTransaction(szumCB, false);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Assert.assertEquals(Integer.valueOf(2), sum);

        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        sum = transactionHelper.doInTransaction(szumCB, true);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Assert.assertEquals(Integer.valueOf(2), sum);

        try {
            transactionHelper.doInTransaction(RUNTIMEEXCEPTION_CB, false);
            Assert.fail("expect runtimeexception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }

        try {
            transactionHelper.doInTransaction(RUNTIMEEXCEPTION_CB, true);
            Assert.fail("expect runtimeexception");
        } catch (RuntimeException e) {
            Assert.assertNotNull(e);
        }
    }

    protected void setTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    protected void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    protected void setTransactionSynchronizationRegistry(
            final TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    @Override
    public void testSuccessTransaction() {

        basicTransactionWhenNoTransaction();

        basicTransActionWhenExistActiveTransaction();

    }

    protected void unSetTransactionHelper(final TransactionHelper th) {
        transactionHelper = null;
    }

    /**
     * Unset (unbind) the transaction manager.
     * 
     * @param tm
     *            the transaction manager.
     */
    protected void unSetTransactionManager(final TransactionManager tm) {
        transactionManager = null;
    }

    protected void unSetTransactionSynchronizationRegistry(final TransactionSynchronizationRegistry tsr) {
        transactionSynchronizationRegistry = null;
    }
}
