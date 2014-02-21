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

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.dev.testrunner.TestDuringDevelopment;
import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.api.TransactionalException;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * Implementation of the {@link TransactionHelperTest}.
 */
@Component(name = "TransactionHelperTest", immediate = true)
@Service(TransactionHelperTestImpl.class)
@Properties({
        @Property(name = "eosgi.testId", value = "transactionHelperTest"),
        @Property(name = "eosgi.testEngine", value = "junit4")
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionHelperTestImpl {

    /**
     * The {@link TransactionHelper} instance.
     */
    @Reference(bind = "setTransactionHelper")
    private TransactionHelper transactionHelper;

    /**
     * The {@link TransactionManager} instance.
     */
    @Reference(bind = "setTransactionManager")
    private TransactionManager transactionManager;

    protected void setTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    protected void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Test
    public void _04_testMandatoryFailAsThereIsNoActiveTransaction() {
        System.out.println("4");
        try {
            transactionHelper.mandatory(null);
            Assert.fail("Should have thrown an exception");
        } catch (TransactionalException e) {
            Assert.assertEquals("Allowed status: active; Current status: no_transaction", e.getMessage());
        }
    }

    @Test
    public void _05_testMandatoryFailDueToInnerException() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        try {
            transactionHelper.required(new Callback<Object>() {

                @Override
                public Object execute() {
                    enlistResource(lastTrStatus);
                    try {
                        return transactionHelper.mandatory(new Callback<Object>() {

                            @Override
                            public Object execute() {
                                assertTransactionStatus(Status.STATUS_ACTIVE);
                                throw new NumberFormatException();
                            }
                        });
                    } catch (NumberFormatException e) {
                        assertTransactionStatus(Status.STATUS_MARKED_ROLLBACK);
                        throw e;
                    }
                }
            });
            Assert.fail("Exception should be thrown");
        } catch (NumberFormatException e) {
            Assert.assertEquals(Status.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }
    }

    @Test
    public void _03_testMandatorySuccess() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                Integer result = transactionHelper.mandatory(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        assertTransactionStatus(Status.STATUS_ACTIVE);
                        return 1;
                    }
                });
                assertTransactionStatus(Status.STATUS_ACTIVE);
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                return result;
            }
        });
        Assert.assertEquals(XAResourceStatus.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _01_testRequiredNoTransactionBeforeSucess() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                return 1;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    @TestDuringDevelopment
    public void _02_testRequiredNoTransactionBeforeFail() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        try {
            transactionHelper.required(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    enlistResource(lastTrStatus);
                    throw new NumberFormatException();
                }
            });
            Assert.fail("Exception should be thrown");
        } catch (NumberFormatException e) {
            Assert.assertEquals(Status.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }
    }

    @Test
    public void _03_testRequiredOngoingTRSuccess() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        int result = transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                int result = transactionHelper.required(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        return 1;
                    }
                });
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                return result;
            }
        });
        Assert.assertEquals(XAResourceStatus.STATUS_COMMITTED, lastTrStatus.getStatus());
        Assert.assertEquals(1, result);
    }

    @Test
    @TestDuringDevelopment
    public void _04_testRequiredOngoingTRFail() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        try {
            transactionHelper.required(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    enlistResource(lastTrStatus);
                    try {
                        return transactionHelper.required(new Callback<Integer>() {

                            @Override
                            public Integer execute() {
                                throw new NumberFormatException();
                            }
                        });
                    } catch (NumberFormatException e) {
                        Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                        assertTransactionStatus(Status.STATUS_MARKED_ROLLBACK);
                        throw e;
                    }
                }
            });
            Assert.fail("Exception should be thrown here");
        } catch (NumberFormatException e) {
            Assert.assertEquals(XAResourceStatus.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }

    }

    private void enlistResource(XAResource resource) {
        Transaction transaction = getTransaction();
        try {
            transaction.enlistResource(resource);
        } catch (IllegalStateException e) {
            throw new RuntimeException(e);
        } catch (RollbackException e) {
            throw new RuntimeException(e);
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertTransactionStatus(int expected) {
        int status = getStatus();
        Assert.assertEquals(expected, status);
    }

    private Transaction getTransaction() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private int getStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }
}
