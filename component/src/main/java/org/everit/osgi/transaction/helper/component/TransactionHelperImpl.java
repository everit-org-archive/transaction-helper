package org.everit.osgi.transaction.helper.component;

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
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.component.api.Callback;
import org.everit.osgi.transaction.helper.component.api.TransactionHelper;
import org.everit.osgi.transaction.helper.component.api.TransactionHelperException;

/**
 * Implementation of {@link TransactionHelper}.
 */
@Component(name = "TransactionHelper")
@Service(value = TransactionHelper.class)
public class TransactionHelperImpl implements TransactionHelper {

    /**
     * The {@link TransactionManager} instance.
     */
    @Reference(bind = "setTransactionManager", unbind = "unSetTransactionManager")
    private TransactionManager transactionManager;

    /**
     * Start the transaction. Create a new transaction and associate it with the current thread.
     * 
     * @throws TransactionHelperException
     *             if has any transaction exception ({@link NotSupportedException}, {@link SystemException}).
     */
    private void begin() {
        try {
            transactionManager.begin();
        } catch (NotSupportedException e) {
            throw new TransactionHelperException(
                    "The thread is already associated with a transaction and the Transaction Manager implementation "
                            + "does not support nested transactions", e);
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    /**
     * Committing the transaction. Complete the transaction associated with the current thread. When this method
     * completes, the thread is no longer associated with a transaction.
     * 
     * @throws TransactionHelperException
     *             if has any transaction exception ({@link IllegalStateException}, {@link SecurityException},
     *             {@link HeuristicMixedException}, {@link HeuristicRollbackException}, {@link SystemException}).
     */
    private void commit() {
        try {
            transactionManager.commit();
        } catch (IllegalStateException e) {
            throw new TransactionHelperException("The current thread is not associated with a transaction", e);
        } catch (SecurityException e) {
            throw new TransactionHelperException("Not allowed to commit the transaction", e);
        } catch (HeuristicMixedException e) {
            // TODO see how to handling the exception.
            throw new TransactionHelperException(
                    "To indicate that a heuristic decision was made and that some relevant "
                            + "updates have been committed while others have been rolled back", e);
        } catch (HeuristicRollbackException e) {
            // TODO see how to handling the exception.
            throw new TransactionHelperException(
                    "To indicate that a heuristic decision was made and that all relevant updates "
                            + "have been rolled back", e);
        } catch (RollbackException e) {
            rollback();
            commit();
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    @Override
    public <R> R doInTransaction(final Callback<R> cb, final boolean requiresNew) {
        int status = getStatus();
        R execute = null;

        if (requiresNew) {
            // If want to force new transaction.

            // Getting the actual transaction and save the local variable.
            Transaction suspend = null;
            if (status != Status.STATUS_NO_TRANSACTION) {
                try {
                    suspend = transactionManager.suspend();
                } catch (SystemException e) {
                    throw new TransactionHelperException("The transaction manager encounters an unexpected error",
                            e);
                }
            }

            begin();
            try {
                execute = cb.execute();
                commit();
            } catch (RuntimeException e) {
                rollback();
                throw e;
            } finally {
                if (suspend != null) {
                    try {
                        transactionManager.resume(suspend);
                    } catch (InvalidTransactionException e) {
                        throw new TransactionHelperException(
                                "The parameter transaction object contains an invalid transaction", e);
                    } catch (IllegalStateException e) {
                        throw new TransactionHelperException(
                                "The thread is already associated with another transaction", e);
                    } catch (SystemException e) {
                        throw new TransactionHelperException("The transaction manager encounters an unexpected error",
                                e);
                    }
                }
            }
        } else {
            if (status == Status.STATUS_NO_TRANSACTION) {
                begin();
                try {
                    execute = cb.execute();
                    commit();
                } catch (RuntimeException e) {
                    rollback();
                    throw e;
                }
            }

            if (status == Status.STATUS_ACTIVE) {
                try {
                    execute = cb.execute();
                } catch (RuntimeException e) {
                    rollback();
                    throw e;
                }
            }
        }

        return execute;
    }

    // /**
    // * Execute the perform code.
    // *
    // * @param cb
    // * the {@link Callback} instance which contains the execute code.
    // * @return the result of the unit of work.
    // *
    // * @throws TransactionHelperException
    // * if has {@link RuntimeException} the execute code.
    // */
    // private <R> R execute(final Callback<R> cb) {
    // try {
    // R execute = cb.execute();
    // return execute;
    // } catch (RuntimeException e) {
    // rollback();
    // throw new TransactionHelperException(e);
    // }
    // }

    // /**
    // * Finishing the transaction. If the transaction must be roll backed the method is roll back, otherwise committing
    // * the transaction.
    // */
    // private void finish() {
    // int executeStatus = getStatus();
    // if ((executeStatus == Status.STATUS_MARKED_ROLLBACK)) {
    // rollback();
    // } else {
    // commit();
    // }
    // }

    /**
     * Getting the transaction status. Obtain the status of the transaction associated with the current thread.
     * 
     * @return the transaction status.
     * 
     * @throws TransactionHelperException
     *             if has any transaction exception ({@link SystemException}).
     */
    private int getStatus() {
        int status;
        try {
            status = transactionManager.getStatus();
            return status;
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    /**
     * Roll backing the actual transaction. Roll back the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     * 
     * @throws TransactionHelperException
     *             if has any transaction exception ({@link IllegalStateException}, {@link SecurityException},
     *             {@link SystemException}).
     */
    private void rollback() {
        try {
            transactionManager.rollback();
        } catch (IllegalStateException e) {
            throw new TransactionHelperException("The current thread is not associated with a transaction", e);
        } catch (SecurityException e) {
            throw new TransactionHelperException("Not allowed to roll back the transaction", e);
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    protected void setTransactionManager(final TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
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
}
