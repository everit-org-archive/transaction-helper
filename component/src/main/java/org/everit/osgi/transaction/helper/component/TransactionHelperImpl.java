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
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.component.api.Callback;
import org.everit.osgi.transaction.helper.component.api.TransactionHelper;
import org.everit.osgi.transaction.helper.component.api.TransactionalException;

/**
 * Implementation of {@link TransactionHelper}.
 */
@Component(name = "TransactionHelper")
@Service(value = TransactionHelper.class)
public class TransactionHelperImpl implements TransactionHelper {

    /**
     * The {@link TransactionManager} instance.
     */
    @Reference(bind = "setTransactionManager", unbind = "unSetTransactionManager", policy = ReferencePolicy.STATIC)
    private TransactionManager transactionManager;

    /**
     * Starting transaction. Create a new transaction and associate it with the current thread.
     * 
     * @throws TransactionalException
     *             if has any transaction exception {@link SystemException} or {@link NotSupportedException}.
     */
    private void begin() {
        try {
            // starting transaction.
            transactionManager.begin();
        } catch (SystemException e) {
            throw new TransactionalException("The transaction manager encounters an unexpected error", e);
        } catch (NotSupportedException e) {
            throw new TransactionalException(
                    "The thread is already associated with a transaction and the Transaction Manager implementation "
                            + "does not support nested transactions", e);
        }
    }

    /**
     * Committing the transaction. Complete the transaction associated with the current thread. When this method
     * completes, the thread is no longer associated with a transaction.
     * 
     * @throws TransactionalException
     *             if has any transaction exception ({@link IllegalStateException}, {@link SecurityException},
     *             {@link HeuristicMixedException}, {@link HeuristicRollbackException}, {@link SystemException}) or
     *             {@link RollbackException}.
     */
    private void commit() {
        try {
            // committing active transaction.
            transactionManager.commit();
        } catch (IllegalStateException e) {
            throw new TransactionalException("The current thread is not associated with a transaction", e);
        } catch (SecurityException e) {
            throw new TransactionalException("Not allowed to commit the transaction", e);
        } catch (HeuristicMixedException e) {
            // TODO see how to handling the exception.
            throw new TransactionalException(
                    "To indicate that a heuristic decision was made and that some relevant "
                            + "updates have been committed while others have been rolled back", e);
        } catch (HeuristicRollbackException e) {
            // TODO see how to handling the exception.
            throw new TransactionalException(
                    "To indicate that a heuristic decision was made and that all relevant updates "
                            + "have been rolled back", e);
        } catch (RollbackException e) {
            throw new TransactionalException("The transaction has been rolled back rather than committed", e);
        } catch (SystemException e) {
            throw new TransactionalException("The transaction manager encounters an unexpected error", e);
        }
    }

    @Override
    public <R> R doInTransaction(final Callback<R> cb, final boolean requiresNew) {

        int transactionManagerStatus = getStatus();
        R execute;

        if (requiresNew) {
            // if want to new transaction.
            Transaction suspend = null;
            if (transactionManagerStatus != Status.STATUS_NO_TRANSACTION) {
                // if exist active transaction save the local variable the suspend transaction.
                try {
                    // suspending active transaction.
                    suspend = transactionManager.suspend();
                } catch (SystemException e) {
                    throw new TransactionalException("The transaction manager encounters an unexpected error", e);
                }
            }

            // start new transaction.
            begin();
            try {
                // executing the transaction code.
                execute = cb.execute();
                // committing active transaction.
                commit();

                // resuming the suspend transaction. If not exist suspend transaction nothing happens.
                resumeTransaction(suspend);
            } catch (RuntimeException e) {
                // the transaction code is throw (unhandling) RuntimeException so rollbacking the active
                // transaction.
                rollback();
                // resuming the suspend transaction. If not exist suspend transaction nothing happens.
                resumeTransaction(suspend);
                // forwarding the exception.
                throw e;
            }
        } else {
            // if not want to new transaction.
            if (transactionManagerStatus == Status.STATUS_NO_TRANSACTION) {
                // if not exist active transaction.
                // start new transaction.
                begin();
                try {
                    // executing the transaction code.
                    execute = cb.execute();
                    // committing active transaction.
                    commit();
                } catch (RuntimeException e) {
                    // the transaction code is throw (unhandling) RuntimeException so rollbacking the active
                    // transaction.
                    rollback();
                    // forwarding the exception.
                    throw e;
                }
            } else {
                // if exist active transaction.
                try {
                    // executing the transaction code.
                    execute = cb.execute();
                    // if not throw exception nothing happening.
                } catch (RuntimeException e) {
                    // the transaction code is throw (unhandling) RuntimeException so the active transaction marking
                    // rollback.
                    setRollbackOnlyTransactionManager();

                    // forwarding the exception.
                    throw e;
                }
            }
        }
        // returning the execute result.
        return execute;
    }

    private int getStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new TransactionalException("The transaction manager encounters an unexpected error", e);
        }
    }

    /**
     * Resuming suspendTransaction if exist suspendTransaction.
     * 
     * @param suspendTransaction
     *            the suspend transaction.
     * @throws TransactionalException
     *             if has any transaction exception ({@link InvalidTransactionException}), (
     *             {@link IllegalStateException}) or ({@link SystemException}).
     */
    private void resumeTransaction(final Transaction suspendTransaction) {
        if (suspendTransaction != null) {
            try {
                // resuming suspendTransacion
                transactionManager.resume(suspendTransaction);
            } catch (InvalidTransactionException e) {
                throw new TransactionalException(
                        "The parameter transaction object contains an invalid transaction", e);
            } catch (IllegalStateException e) {
                throw new TransactionalException(
                        "The thread is already associated with another transaction", e);
            } catch (SystemException e) {
                throw new TransactionalException("The transaction manager encounters an unexpected error",
                        e);
            }
        }
    }

    /**
     * Roll backing the actual transaction. Roll back the transaction associated with the current thread. When this
     * method completes, the thread is no longer associated with a transaction.
     * 
     * @throws TransactionalException
     *             if has any transaction exception ({@link IllegalStateException}, {@link SecurityException},
     *             {@link SystemException}).
     */
    private void rollback() {
        try {
            // the active transaction is rollbacked.
            transactionManager.rollback();
        } catch (IllegalStateException e) {
            throw new TransactionalException("The current thread is not associated with a transaction", e);
        } catch (SecurityException e) {
            throw new TransactionalException("Not allowed to roll back the transaction", e);
        } catch (SystemException e) {
            throw new TransactionalException("The transaction manager encounters an unexpected error", e);
        }
    }

    /**
     * The active transaction is marked to rollback. Modify the transaction associated with the current thread such that
     * the only possible outcome of the transaction is to roll back the transaction.
     * 
     * @throws TransactionalException
     *             if has any transaction exception ({@link IllegalStateException} or {@link SystemException}).
     */
    private void setRollbackOnlyTransactionManager() {
        try {
            // the active transaction marked as rollback.
            transactionManager.setRollbackOnly();
        } catch (IllegalStateException e) {
            throw new TransactionalException("The current thread is not associated with a transaction", e);
        } catch (SystemException e) {
            throw new TransactionalException("The transaction manager encounters an unexpected error", e);
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
