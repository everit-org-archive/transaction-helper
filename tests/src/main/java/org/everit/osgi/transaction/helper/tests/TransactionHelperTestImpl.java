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
import javax.transaction.TransactionSynchronizationRegistry;

import junit.framework.Assert;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
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
    @Reference(bind = "setTransactionHelper", policy = ReferencePolicy.STATIC)
    private TransactionHelper transactionHelper;

    /**
     * The {@link TransactionSynchronizationRegistry} instance.
     */
    @Reference(bind = "setTransactionSynchronizationRegistry", policy = ReferencePolicy.STATIC)
    private TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    /**
     * The basic Callback transaction which throw new RuntimeException.
     */
    private final Callback<Void> cbIllegalArgumentException = new Callback<Void>() {

        @Override
        public Void execute() {
            throw new IllegalArgumentException("Test IllegalArgumentException");
        }
    };

    /**
     * The basic CallBack transaction which getting the zero number.
     */
    private final Callback<Integer> cbGetZero = new Callback<Integer>() {

        @Override
        public Integer execute() {
            Assert.assertEquals(Status.STATUS_ACTIVE, transactionSynchronizationRegistry.getTransactionStatus());
            return 0;
        }
    };

    protected void setTransactionHelper(final TransactionHelper transactionHelper) {
        this.transactionHelper = transactionHelper;
    }

    protected void setTransactionSynchronizationRegistry(
            final TransactionSynchronizationRegistry transactionSynchronizationRegistry) {
        this.transactionSynchronizationRegistry = transactionSynchronizationRegistry;
    }

    @Override
    public void test13() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, true);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    @Override
    public void test14() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, true);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void test15() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void test16() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void test17() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, true);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    @Override
    public void test18() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, true);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void test19() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void test20() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(new Callback<Void>() {

                    @Override
                    public Void execute() {
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                        Assert.assertEquals(Integer.valueOf(0), zero);
                        Assert.assertEquals(Status.STATUS_ACTIVE,
                                transactionSynchronizationRegistry.getTransactionStatus());
                        throw new IllegalArgumentException();
                    }
                }, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testExistTransactionNotRequiresNewTransactionInsideGetZeroCBWithFalse() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        Integer one = transactionHelper.doInTransaction(cb, false);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    @Override
    public void testExistTransactionNotRequiresNewTransactionInsideGetZeroCBWithTrue() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        Integer one = transactionHelper.doInTransaction(cb, false);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testExistTransactionNotRequiresNewTransactionInsideIllegalArgumentExceptionCBWithFalse() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(cbIllegalArgumentException, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException, beacuse the inside tranzakción is throwing.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testExistTransactionNotRequiresNewTransactionInsideIllegalArgumentExceptionCBWithTrue() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(cbIllegalArgumentException, true);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, false);
            Assert.fail("Expect IllegalArgumentException, because inside method throwing that.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    @Override
    public void testExistTransactionRequiresNewTransactionInsideGetZeroCBWithFalse() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        Integer one = transactionHelper.doInTransaction(cb, true);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testExistTransactionRequiresNewTransactionInsideGetZeroCBWithTrue() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
                Assert.assertEquals(Integer.valueOf(0), zero);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        Integer one = transactionHelper.doInTransaction(cb, true);
        Assert.assertEquals(Integer.valueOf(1), one);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testExistTransactionRequiresNewTransactionInsideIllegalArgumentExceptionCBWithFalse() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(cbIllegalArgumentException, false);
                Assert.assertEquals(Status.STATUS_MARKED_ROLLBACK,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException, beacuse the inside tranzakción is throwing.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());

    }

    @Override
    public void testExistTransactionRequiresNewTransactionInsideIllegalArgumentExceptionCBWithTrue() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Callback<Integer> cb = new Callback<Integer>() {

            @Override
            public Integer execute() {
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                transactionHelper.doInTransaction(cbIllegalArgumentException, true);
                Assert.assertEquals(Status.STATUS_ACTIVE,
                        transactionSynchronizationRegistry.getTransactionStatus());
                return 1;
            }
        };
        try {
            transactionHelper.doInTransaction(cb, true);
            Assert.fail("Expect IllegalArgumentException, because inside method throwing that.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testNoTransactionNotRequiresNewTransactionGetZeroCB() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Integer zero = transactionHelper.doInTransaction(cbGetZero, false);
        Assert.assertEquals(Integer.valueOf(0), zero);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testNoTransactionNotRequiresNewTransactionIllegalArgumentExceptionCB() {
        try {
            transactionHelper.doInTransaction(cbIllegalArgumentException, false);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testNoTransactionRequiresNewTransactionGetZeroCB() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        Integer zero = transactionHelper.doInTransaction(cbGetZero, true);
        Assert.assertEquals(Integer.valueOf(0), zero);
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

    @Override
    public void testNoTransactionRequiresNewTransactionIllegalArgumentExceptionCB() {
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
        try {
            transactionHelper.doInTransaction(cbIllegalArgumentException, true);
            Assert.fail("Expect IllegalArgumentException.");
        } catch (IllegalArgumentException e) {
            Assert.assertNotNull(e);
        }
        Assert.assertEquals(Status.STATUS_NO_TRANSACTION, transactionSynchronizationRegistry.getTransactionStatus());
    }

}
