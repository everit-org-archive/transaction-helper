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

import java.util.Arrays;

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
@Service(TransactionHelperTest.class)
@Properties({
        @Property(name = "eosgi.testId", value = "transactionHelperTest"),
        @Property(name = "eosgi.testEngine", value = "junit4")
})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TransactionHelperTest {

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
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
        Assert.assertEquals(1, result);
    }

    @Test
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
            Assert.assertEquals(Status.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }

    }

    @Test
    public void _05_testRequiredOngoingTRCaughedFail() {
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
                    }
                    return 1;
                }
            });
            Assert.fail("IllegalstateException should have been thrown here");
        } catch (TransactionalException e) {
            Assert.assertTrue(e.getCause() instanceof RollbackException);
            Assert.assertEquals(Status.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }
    }

    @Test
    public void _06_testRequiresNewNoTransactionSuccess() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        Integer result = transactionHelper.requiresNew(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());
                return 1;
            }

        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, getStatus());
        Assert.assertEquals(1, result.intValue());
    }

    @Test
    public void _07_testRequiresNewOngoingTransactionSuccess() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                final RememberLastCallXAResource innerLastTrStatus = new RememberLastCallXAResource();
                enlistResource(lastTrStatus);
                Integer result = transactionHelper.requiresNew(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        enlistResource(innerLastTrStatus);
                        byte[] outerTrId = lastTrStatus.getXid().getGlobalTransactionId();
                        byte[] innerTrId = innerLastTrStatus.getXid().getGlobalTransactionId();
                        Assert.assertFalse(Arrays.equals(outerTrId, innerTrId));
                        return 1;
                    }
                });
                Assert.assertEquals(Status.STATUS_COMMITTED, innerLastTrStatus.getStatus());
                Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                return result;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _08_testRequiresNewOngoingTransactionFailAndCatch() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.requiresNew(new Callback<Integer>() {

            @Override
            public Integer execute() {
                final RememberLastCallXAResource innerLastTrStatus = new RememberLastCallXAResource();
                enlistResource(lastTrStatus);
                try {
                    transactionHelper.requiresNew(new Callback<Integer>() {

                        @Override
                        public Integer execute() {
                            enlistResource(innerLastTrStatus);
                            throw new NumberFormatException();
                        }
                    });
                    Assert.fail("Code part should not be accessible");
                } catch (NumberFormatException e) {
                    Assert.assertEquals(Status.STATUS_ROLLEDBACK, innerLastTrStatus.getStatus());
                    Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());
                    Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                }

                return 1;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _09_testMandatorySuccess() {
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
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _10_testMandatoryFailAsThereIsNoActiveTransaction() {
        System.out.println("4");
        try {
            transactionHelper.mandatory(null);
            Assert.fail("Should have thrown an exception");
        } catch (IllegalStateException e) {
            Assert.assertEquals("Allowed status: active; Current status: no_transaction", e.getMessage());
        }
    }

    @Test
    public void _11_testMandatoryFailDueToInnerException() {
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
    public void _12_testNeverSuccess() {
        Integer result = transactionHelper.never(new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_NO_TRANSACTION, getStatus());
                return 1;
            }
        });
        Assert.assertEquals(1, result.intValue());
    }

    @Test
    public void _13_testNeverFailDueToOngoingTransaction() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                try {
                    transactionHelper.never(new Callback<Integer>() {

                        @Override
                        public Integer execute() {
                            Assert.fail("Unreachable code");
                            return null;
                        }

                    });
                } catch (IllegalStateException e) {
                    Assert.assertEquals("Allowed status: no_transaction; Current status: active", e.getMessage());
                }
                return null;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _14_testSupportsWithSuccessNoTransaction() {
        Integer result = transactionHelper.supports(new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_NO_TRANSACTION, getStatus());
                return 1;
            }
        });
        Assert.assertEquals(1, result.intValue());
    }

    @Test
    public void _15_testSupportsWithSuccessOngoingTransaciton() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        final RememberLastCallXAResource outerLastTrStatus = new RememberLastCallXAResource();
        Integer result = transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(outerLastTrStatus);
                Integer result = transactionHelper.supports(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        enlistResource(lastTrStatus);
                        Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());
                        byte[] outerXID = outerLastTrStatus.getXid().getGlobalTransactionId();
                        byte[] innerXID = lastTrStatus.getXid().getGlobalTransactionId();
                        Assert.assertTrue(Arrays.equals(outerXID, innerXID));
                        return 1;
                    }
                });
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                return result;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, outerLastTrStatus.getStatus());
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
        Assert.assertEquals(1, result.intValue());
    }

    @Test
    public void _16_testSupportsWithExceptionNoTransaction() {
        try {
            transactionHelper.supports(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    throw new NumberFormatException();
                }
            });
            Assert.fail("Code should be unreachable");
        } catch (NumberFormatException e) {
            // Good
        }
    }

    @Test
    public void _17_testSupporstWithExceptionOngoingTransaction() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        try {
            transactionHelper.required(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    enlistResource(lastTrStatus);
                    try {
                        transactionHelper.supports(new Callback<Integer>() {

                            @Override
                            public Integer execute() {
                                throw new NumberFormatException();
                            }
                        });
                        Assert.fail("Code should be unreachable");
                    } catch (NumberFormatException e) {
                        Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK, getStatus());
                    }
                    return null;
                }
            });
            Assert.fail("Code should be unreachable");
        } catch (TransactionalException e) {
            Assert.assertEquals(Status.STATUS_ROLLEDBACK, lastTrStatus.getStatus());
        }
    }

    @Test
    public void _18_notSupportedWithSuccessNoTransaction() {
        Integer result = transactionHelper.notSupported(new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_NO_TRANSACTION, getStatus());
                return 1;
            }
        });
        Assert.assertEquals(1, result.intValue());
    }

    @Test
    public void _19_notSupportedWithSuccessOngoingTransaction() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        final RememberLastCallXAResource lastTrStatus2 = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                byte[] trIdBefore = lastTrStatus.getXid().getGlobalTransactionId();
                Integer result = transactionHelper.notSupported(new Callback<Integer>() {

                    @Override
                    public Integer execute() {
                        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, getStatus());
                        return 1;
                    }
                });
                Assert.assertEquals(1, result.intValue());
                Assert.assertEquals(XAResourceStatus.STATUS_START, lastTrStatus.getStatus());
                Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());

                enlistResource(lastTrStatus2);
                byte[] trIdAfter = lastTrStatus2.getXid().getGlobalTransactionId();
                Assert.assertTrue(Arrays.equals(trIdBefore, trIdAfter));
                return result;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus2.getStatus());
    }

    @Test
    public void _20_testNotSupportsWithFailOngoingTransaction() {
        final RememberLastCallXAResource lastTrStatus = new RememberLastCallXAResource();
        transactionHelper.required(new Callback<Integer>() {

            @Override
            public Integer execute() {
                enlistResource(lastTrStatus);
                try {
                    transactionHelper.notSupported(new Callback<Integer>() {

                        @Override
                        public Integer execute() {
                            throw new NumberFormatException();
                        }
                    });
                    Assert.fail("Should be unreachable code");
                } catch (NumberFormatException e) {
                    Assert.assertEquals(Status.STATUS_ACTIVE, getStatus());
                }
                return null;
            }
        });
        Assert.assertEquals(Status.STATUS_COMMITTED, lastTrStatus.getStatus());
    }

    @Test
    public void _21_testExceptionSuppression() {
        try {
            transactionHelper.required(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    try {
                        transactionManager.commit();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                    throw new NumberFormatException("Test exception");
                }
            });
            Assert.fail("Exception should have been thrown");
        } catch (NumberFormatException e) {
            Assert.assertEquals("Test exception", e.getMessage());
        }
    }

    @Test
    @TestDuringDevelopment
    public void _22_testExceptionDuringCommit() {
        try {
            transactionHelper.required(new Callback<Integer>() {

                @Override
                public Integer execute() {
                    try {
                        transactionManager.commit();
                    } catch (Exception e) {
                        Assert.fail(e.getMessage());
                    }
                    return 1;
                }
            });
            Assert.fail("Exception should have been thrown");
        } catch (Exception e) {
            Assert.assertEquals(IllegalStateException.class, e.getClass());
        }
    }

    private void assertTransactionStatus(final int expected) {
        int status = getStatus();
        Assert.assertEquals(expected, status);
    }

    private void enlistResource(final XAResource resource) {
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

    private int getStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    private Transaction getTransaction() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new RuntimeException(e);
        }
    }

    protected void setTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    protected void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }
}
