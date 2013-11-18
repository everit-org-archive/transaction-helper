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

import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.component.api.Callback;
import org.everit.osgi.transaction.helper.component.api.TransactionHelper;
import org.everit.osgi.transaction.helper.component.api.TransactionalException;

import org.junit.Test;
import org.junit.Assert;

/**
 * Implementation of the {@link TransactionHelperTest}.
 */
@Component
@Service(TransactionHelperTestImpl.class)
@Properties({
        @Property(name = "osgitest.testId", value = "transactionHelperTest"),
        @Property(name = "osgitest.testEngine", value = "junit4")
})
public class TransactionHelperTestImpl {

    /**
     * The {@link TransactionHelper} instance.
     */
    @Reference(bind = "setTransactionHelper", policy = ReferencePolicy.STATIC)
    private TransactionHelper transactionHelper;

    /**
     * The {@link TransactionManager} instance.
     */
    @Reference(bind = "setTransactionManager", policy = ReferencePolicy.STATIC)
    private TransactionManager transactionManager;

    /**
     * The {@link TransactionSynchronizationRegistry} instance.
     */
    @Reference(bind = "setTransactionSynchronizationRegistry", policy = ReferencePolicy.STATIC)
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    /**
     * The T1 transaction resource. The resource is {@link RememberLastCallXAResource} and contains information of the
     * transaction status. For example {@link Status#STATUS_COMMITTED} or {@link Status#STATUS_ROLLEDBACK}.
     */
    private static AtomicReference<RememberLastCallXAResource> t1Resource =
            new AtomicReference<RememberLastCallXAResource>();

    /**
     * The T2 transaction resource. The resource is {@link RememberLastCallXAResource} and contains information of the
     * transaction status. For example {@link Status#STATUS_COMMITTED} or {@link Status#STATUS_ROLLEDBACK}.
     */
    private static AtomicReference<RememberLastCallXAResource> t2Resource =
            new AtomicReference<RememberLastCallXAResource>();

    /**
     * The T3 transaction resource. The resource is {@link RememberLastCallXAResource} and contains information of the
     * transaction status. For example {@link Status#STATUS_COMMITTED} or {@link Status#STATUS_ROLLEDBACK}.
     */
    private static AtomicReference<RememberLastCallXAResource> t3Resource =
            new AtomicReference<RememberLastCallXAResource>();

    /**
     * Asserting the xaResource status is {@link Status#STATUS_COMMITTED} or not.
     * 
     * @param xaResource
     *            the resource which asserting.
     */
    private void assertStatusCommitted(final AtomicReference<RememberLastCallXAResource> xaResource) {
        Assert.assertEquals(Status.STATUS_COMMITTED, xaResource.get().getStatus());
    }

    /**
     * Asserting the xaResource status is {@link XAResourceStatus#STATUS_END} or not.
     * 
     * @param xaResource
     *            the resource which asserting.
     */
    private void assertStatusEnd(final AtomicReference<RememberLastCallXAResource> xaResource) {
        Assert.assertEquals(XAResourceStatus.STATUS_END, xaResource.get().getStatus());
    }

    /**
     * Asserting the xaResource status is {@link Status#STATUS_ROLLEDBACK} or not.
     * 
     * @param xaResource
     *            the resource which asserting.
     */
    private void assertStatusRollback(final AtomicReference<RememberLastCallXAResource> xaResource) {
        Assert.assertEquals(Status.STATUS_ROLLEDBACK, xaResource.get().getStatus());
    }

    /**
     * Asserting the xaResource status is {@link XAResourceStatus#STATUS_START} or not.
     * 
     * @param xaResource
     *            the resource which asserting.
     */
    private void assertStatusStart(final AtomicReference<RememberLastCallXAResource> xaResource) {
        Assert.assertEquals(XAResourceStatus.STATUS_START, xaResource.get().getStatus());
    }

    /**
     * Registering resource the transaction. Important must be existing active transaction, otherwise throw exception.
     * 
     * @return the registered resource.
     * @throws RuntimeException
     *             if not registering the resource.
     */
    private RememberLastCallXAResource registerXAResource() {
        final RememberLastCallXAResource xaResource = new RememberLastCallXAResource();
        boolean enlistResource = false;
        try {
            enlistResource = transactionManager.getTransaction().enlistResource(xaResource);
        } catch (IllegalStateException e) {
            Assert.fail();
        } catch (RollbackException e) {
            Assert.fail();
        } catch (SystemException e) {
            Assert.fail();
        }
        if (enlistResource) {
            return xaResource;
        } else {
            throw new RuntimeException();
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

    /**
     * T1 required. T1 transaction execute and committed.
     */
    @Test
    public void testT1RequiredCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 transaction
        Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 0;
            }

        }, false);
        // chech T1 transaction is committed.
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(0), zero);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 required. T2 transaction execute but not committed yet. T1 transaction execute and committed.
     * So T1 and T2 transaction is committed at the end.
     */
    @Test
    public void testT1RequiredCommittedT2RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 Transaction
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());

                // T2 transaction
                Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        t2Resource.set(registerXAResource());
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        return 0;
                    }

                }, false);
                // check T2 transaction is started.
                assertStatusStart(t2Resource);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, false);
        // check T2 transaction is ended.
        assertStatusEnd(t2Resource);
        // check T1 transaction is committed
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    /**
     * T1 required -> T2 requiresNew. T2 transaction execute committed immediately. T1 transaction execute and
     * committed.
     */
    @Test
    public void testT1RequiredCommittedT2RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 transaction
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());

                // T2 transaction
                Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        t2Resource.set(registerXAResource());
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        return 0;
                    }

                }, true);
                // check T2 transaction is committed
                assertStatusCommitted(t2Resource);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, false);
        // check T2, T1 transaction is committed
        assertStatusCommitted(t2Resource);
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 requiresNew. T2 throw IllegalArgumentException so rollbacked. T1 transaction commited, despite
     * T2 throw RuntimeException. T1 transaction handling IllegalArgumentException so committed.
     */
    @Test
    public void testT1RequiredCommittedT2RequiresNewRollbackHandlingExceptionInTransaction() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 transaction (catch IllegalArgumentException which T2 throw), committing T1.
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                try {
                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, true);
                    Assert.fail("Expect IllegalArgumentException. T2 transaction must be rollbacked "
                            + "and forwarding exception.");
                } catch (IllegalArgumentException e) {
                    // check T2 is rollbacked.
                    assertStatusRollback(t2Resource);
                    // check the actual transaction (T1) is active
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    Assert.assertNotNull(e);
                }
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, false);
        // check T2 is rollbacked.
        assertStatusRollback(t2Resource);
        // check T1 is committed
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required. T1 transaction execute and rollbacked.
     */
    @Test
    public void testT1RequiredRollback() {
        try {
            // T2 Transaction
            transactionHelper.doInTransaction(new Callback<Void>() {

                @Override
                public Void execute() {
                    t1Resource.set(registerXAResource());
                    throw new IllegalArgumentException("Test IllegalArgumentException");
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T1 transaction is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 required. T2 transaction throw IllegalArgumentException setrollbackonly active transaction
     * (which is T1). T1 transaction rollbacked, because T2 throw RuntimeExcetpion and marked as rollbacked.
     */
    @Test
    public void testT1RequiredRollbackT2RequiredSetRollbackonly() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, false);
                    Assert.fail("Expect IllegalArgumentException. T2 transaction must be "
                            + "setrollbackedonly the active transaction which is T1.");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T2 transaction is started. Because the main transaction is T1.
        assertStatusEnd(t2Resource);
        // check T1 transaction is rollbacked
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 required. T2 throw IllegalArgumentException so setrollbackonly active transaction (T1). T1
     * transaction rollbacked, despite T2 throw RuntimeException in vain T1 handling IllegalArgumentException.
     */
    @Test
    public void testT1RequiredRollbackT2RequiredSetRollbackonlyHandlingExceptionInTransaction() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 transaction handling T2 IllegalArgumentException, despite rollbacking T1 transaction, beacause T1
        // transaction is marked rollback.
        try {
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    try {
                        // T2 transaction
                        transactionHelper.doInTransaction(new Callback<Void>() {

                            @Override
                            public Void execute() {
                                t2Resource.set(registerXAResource());
                                throw new IllegalArgumentException("Test IllegalArgumentException");
                            }
                        }, false);
                        Assert.fail("Expect IllegalArgumentException. T2 transaction must be "
                                + "setrollbackonly the active transaction (which is T1).");
                    } catch (IllegalArgumentException e) {
                        // check T2 is started.
                        assertStatusStart(t2Resource);
                        // check the actual transaction (T1) is marked rollbacked or not.
                        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Assert.assertNotNull(e);
                    }
                    Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    return 1;
                }
            }, false);
            Assert.fail("Expect TransactionException, because has RollbackException");
        } catch (TransactionalException e) {
            Assert.assertNotNull(e);
        }
        // check T2 is ended.
        assertStatusEnd(t2Resource);
        // check T1 is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 required -> T3 required. T3 transaction not committed, because exist active transaction (T2).
     * T2 throw IllegalArgumentException setrollbackonly active transaction (which is T1). T1 transaction rollbacked,
     * because T2 throw unhandled RuntimeException and marked as rollback.
     */
    @Test
    public void testT1RequiredRollbackT2RequiredSetRollbackonlyT3RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }
                            }, false);
                            // check T3 transaction is started.
                            assertStatusStart(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, false);
                    Assert.fail("T2 transaction setRollbackOnly actual transaction (which is T1).");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T3, T2 transaction is ended. Because the main transaction is T1.
        assertStatusEnd(t3Resource);
        assertStatusEnd(t2Resource);
        // check T1 is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 required -> T3 requiresNew. T3 transaction committed. T2 throw IllegalArgumentException
     * setrollbackonly active transaction (which is T1). T1 transaction rollbacked, because T2 throw unhandled
     * RuntimeException and marked as rollback.
     */
    @Test
    public void testT1RequiredRollbackT2RequiredSetRollbackonlyT3RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }
                            }, true);
                            // check T3 is committed
                            assertStatusCommitted(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, false);
                    Assert.fail("T2 transaction setRollbackOnly actual transaction (which is T1).");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T3 is committed
        assertStatusCommitted(t3Resource);
        // check T2 is ended.
        assertStatusEnd(t2Resource);
        // check T1 is rollbacked
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 requiresNew. T2 transaction rollbacked. T1 transaction rollbacked, because T2 throw
     * RuntimeExcetpion.
     */
    @Test
    public void testT1RequiredRollbackT2RequiresNewRollback() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T2, T1 transaction is rollbacked.
        assertStatusRollback(t2Resource);
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    /**
     * T1 required -> T2 requiresNew -> T3 required. T3 transaction not committed, because exist active transaction
     * (T2). T2 throw IllegalArgumentException rollback T2 transaction. T1 transaction rollbacked, because T2 throw
     * unhandled RuntimeException.
     */
    @Test
    public void testT1RequiredRollbackT2RequiresNewRollbackT3RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }

                            }, false);
                            // check T3 is started.
                            assertStatusStart(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T3 is ended.
        assertStatusEnd(t3Resource);
        // check T2, T1 is rollbacked.
        assertStatusRollback(t2Resource);
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 required -> T2 requiresNew -> T3 requiresNew. T3 transaction committed. T2 throw IllegalArgumentException ->
     * Rollback. T1 transaction rollbacked, because T2 throw unhandled RuntimeException.
     */
    @Test
    public void testT1RequiredRollbackT2RequiresNewRollbackT3RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }

                            }, true);
                            // check T3 is committed
                            assertStatusCommitted(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, false);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T3 is committed
        assertStatusCommitted(t3Resource);
        // check T2, T1 is rollbacked
        assertStatusRollback(t2Resource);
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    /**
     * T1 requiresNew. T1 transaction execute and committed.
     */
    @Test
    public void testT1RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 Transaction
        Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 0;
            }

        }, true);
        // check T1 transaction is committed
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(0), zero);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 required. T2 transaction execute but not committed yet, because previous transaction
     * continue (T1). T1 transaction execute and committed.
     */
    @Test
    public void testT1RequiresNewCommittedT2RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 Transaction
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());

                // T2 Transaction
                Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        t2Resource.set(registerXAResource());
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        return 0;
                    }

                }, false);
                // check T2 transaction is started
                assertStatusStart(t2Resource);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, true);
        // check T2 transaction is ended
        assertStatusEnd(t2Resource);
        // check T1 transaction is committed
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 requiresNew. T2 throw IllegalArgumentException so rollbacked. T1 transaction commited,
     * despite T2 throw RuntimeException. T1 transaction handling IllegalArgumentException so committed.
     */
    @Test
    public void testT1RequiresNewCommittedT2RequiredRollbackHandlingExceptionInTransaction() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 transaction (catch IllegalArgumentException which throw T2), committing T1.
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                try {
                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, true);
                    Assert.fail("Expect IllegalArgumentException. T2 transaction must be rollbacked "
                            + "and forwarding exception.");
                } catch (IllegalArgumentException e) {
                    // check T2 is rollbacked.
                    assertStatusRollback(t2Resource);
                    // check the actual transaction (T1) is active
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    Assert.assertNotNull(e);
                }
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, true);
        // check T2 is rollbacked.
        assertStatusRollback(t2Resource);
        // check T1 is committed
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 requiresNew. T2 transaction execute and committed. T1 transaction execute and committed.
     */
    @Test
    public void testT1RequiresNewCommittedT2RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        // T1 Transaction
        Integer one = transactionHelper.doInTransaction(new Callback<Integer>() {

            @Override
            public Integer execute() {
                t1Resource.set(registerXAResource());
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());

                // T2 Transaction
                Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        t2Resource.set(registerXAResource());
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        return 0;
                    }

                }, true);
                // check T2 transaction is committed
                assertStatusCommitted(t2Resource);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        }, true);
        // check T2, T1 transaction is committed
        assertStatusCommitted(t2Resource);
        assertStatusCommitted(t1Resource);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew. T1 transaction execute and rollbacked.
     */
    @Test
    public void testT1RequiresNewRollback() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 Transaction
            transactionHelper.doInTransaction(new Callback<Void>() {

                @Override
                public Void execute() {
                    t1Resource.set(registerXAResource());
                    throw new IllegalArgumentException("Test IllegalArgumentException");
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T1 transaction is rollbacked
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 required. T2 transaction execute and throw IllegalArgumentException. Not rollbacked just
     * setRollbackOnly, because previous transaction is continue (T1). T1 transaction rollback, because catch
     * RuntimeException and the the transaction is marked to rollback.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiredSetRollbackonly() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 Transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T1 Transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, false);
                    Assert.fail("Must be setRollbackOnly active transaction (which is T1).");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T2 transaction is ended.
        assertStatusEnd(t2Resource);
        // check T1 transaction is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 required. T2 throw IllegalArgumentException so setrollbackonly active transaction (T1). T1
     * transaction rollbacked, despite T2 throw RuntimeException in vain T1 handling IllegalArgumentException.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiredSetRollbackonlyHandlingExceptionInTransaction() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction handling T2 IllegalArgumentException, despite rollbacking T1 transaction, beacause T1
            // transaction is marked rollback.
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    try {
                        // T2 transaction
                        transactionHelper.doInTransaction(new Callback<Void>() {

                            @Override
                            public Void execute() {
                                t2Resource.set(registerXAResource());
                                throw new IllegalArgumentException("Test IllegalArgumentException");
                            }
                        }, false);
                        Assert.fail("Expect IllegalArgumentException. T2 transaction must be "
                                + "setrollbackonly the active transaction (which is T1).");
                    } catch (IllegalArgumentException e) {
                        // check T2 is started.
                        assertStatusStart(t2Resource);
                        // check the actual transaction (T1) is marked rollbacked or not.
                        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Assert.assertNotNull(e);
                    }
                    Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    return 1;
                }
            }, true);
            Assert.fail("Expect TransactionException, because has RollbackException");
        } catch (TransactionalException e) {
            Assert.assertNotNull(e);
        }
        // check T2 is ended.
        assertStatusEnd(t2Resource);
        // check T1 is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 required -> T3 required. T3 transaction execute, but not committed, because required and
     * exist active transaction. T2 throw IllegalArgumentException so setRollbackOnly the active transaction (which is
     * T1). T1 transaction rollbacked, because T2 throw unhandled RuntimeException and marked rollback.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiredSetRollbackonlyT3RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }

                            }, false);
                            // check T3 transaction is start and not rollbacked or committed.
                            assertStatusStart(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, false);
                    Assert.fail("T2 transaction setRollbackOnly actual transaction (which is T1).");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // Check T3, T2 transaction is end-ed.
        assertStatusEnd(t3Resource);
        assertStatusEnd(t2Resource);
        // Check T1 transaction is rollbacked.
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 required -> T3 requiresNew. T3 transaction committed. T2 throw IllegalArgumentException so
     * setRollbackonly actual transaction (which is T1). T1 transaction rollbacked, because T2 throw unhandled
     * RuntimeException and marked as rollback.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiredSetRollbackonlyT3RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }

                            }, true);
                            // check T3 is committed.
                            assertStatusCommitted(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, false);
                    Assert.fail("T2 transaction setRollbackOnly actual transaction (which is T1).");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // check T3 is committed.
        assertStatusCommitted(t3Resource);
        // Check T2 is ended.
        assertStatusEnd(t2Resource);
        // check T1 is rollbacked
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 requiresNew. T2 transaction execute and rollbacked. T1 transaction rollbacked.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiresNewRollback() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 Transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());
                    // T2 Transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            throw new IllegalArgumentException("Test IllegalArgumentException");
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // chech T2, T1 transaction is rollbacked.
        assertStatusRollback(t2Resource);
        assertStatusRollback(t2Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 requiresNew -> T3 required. T3 transaction execute, but not committed, because required and
     * exist active transaction. T2 throw IllegalArgumentException so rollbacked. T1 transaction rollbacked, because T2
     * throw unhandled RuntimeException.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiresNewRollbackT3RequiredNotCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }
                            }, false);
                            // check T3 transaction is start and not rollbacked or committed.
                            assertStatusStart(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }

        // check T3 transaction is ended or not.
        assertStatusEnd(t3Resource);
        // Check T2, T1 transaction is rollbacked or not.
        assertStatusRollback(t2Resource);
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    /**
     * T1 requiresNew -> T2 requiresNew -> T3 requiresNew. T3 transaction committed. T2 throw IllegalArgumentException
     * so rollbacked. T1 transaction rollbacked because T2 throw unhandled RuntimeException.
     */
    @Test
    public void testT1RequiresNewRollbackT2RequiresNewRollbackT3RequiresNewCommitted() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            // T1 transaction
            transactionHelper.doInTransaction(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    t1Resource.set(registerXAResource());
                    Assert.assertEquals(Status.STATUS_ACTIVE,
                            transactionSynchronizationRegistry.getTransactionStatus());

                    // T2 transaction
                    transactionHelper.doInTransaction(new Callback<Void>() {

                        @Override
                        public Void execute() {
                            t2Resource.set(registerXAResource());
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());

                            // T3 transaction
                            Integer zero = transactionHelper.doInTransaction(new Callback<Integer>() {

                                @Override
                                public Integer execute() {
                                    t3Resource.set(registerXAResource());
                                    Assert.assertEquals(Status.STATUS_ACTIVE,
                                            transactionSynchronizationRegistry.getTransactionStatus());
                                    return 0;
                                }
                            }, true);
                            // Check T3 transaction is committed.
                            assertStatusCommitted(t3Resource);
                            Assert.assertEquals(Integer.valueOf(0), zero);
                            Assert.assertEquals(Status.STATUS_ACTIVE,
                                    transactionSynchronizationRegistry.getTransactionStatus());
                            throw new IllegalArgumentException();
                        }
                    }, true);
                    Assert.fail("Must be rollbacking T2 transaction.");
                    return 1;
                }
            }, true);
            Assert.fail("Must be rollbacking T1 transaction.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        // Check T3 transaction is committed or not.
        assertStatusCommitted(t3Resource);
        // Check T2, T1 transaction is rollbacked.
        assertStatusRollback(t2Resource);
        assertStatusRollback(t1Resource);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }
}
