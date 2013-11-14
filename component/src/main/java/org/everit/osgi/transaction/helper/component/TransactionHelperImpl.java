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

import java.util.logging.Logger;

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
    @Reference(bind = "setTransactionManager", unbind = "unSetTransactionManager", policy = ReferencePolicy.STATIC)
    private TransactionManager transactionManager;

    private static final Logger LOGGER = Logger.getLogger("TransactionHelperImpl");

    // /**
    // * Start the transaction. Create a new transaction and associate it with the current thread.
    // *
    // * @throws TransactionHelperException
    // * if has any transaction exception ({@link NotSupportedException}, {@link SystemException}).
    // */
    // private void begin() {
    // try {
    // transactionManager.begin();
    // } catch (NotSupportedException e) {
    // throw new TransactionHelperException(
    // "The thread is already associated with a transaction and the Transaction Manager implementation "
    // + "does not support nested transactions", e);
    // } catch (SystemException e) {
    // throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
    // }
    // }

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
            throw new TransactionHelperException("The transaction has been rolled back rather than committed", e);
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    // private void commit(final Transaction transaction) {
    // try {
    // transaction.commit();
    // } catch (IllegalStateException e) {
    // throw new TransactionHelperException("The current thread is not associated with a transaction", e);
    // } catch (SecurityException e) {
    // throw new TransactionHelperException("Not allowed to commit the transaction", e);
    // } catch (HeuristicMixedException e) {
    // // TODO see how to handling the exception.
    // throw new TransactionHelperException(
    // "To indicate that a heuristic decision was made and that some relevant "
    // + "updates have been committed while others have been rolled back", e);
    // } catch (HeuristicRollbackException e) {
    // // TODO see how to handling the exception.
    // throw new TransactionHelperException(
    // "To indicate that a heuristic decision was made and that all relevant updates "
    // + "have been rolled back", e);
    // } catch (RollbackException e) {
    // throw new TransactionHelperException("The transaction has been rolled back rather than committed", e);
    // } catch (SystemException e) {
    // throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
    // }
    // }

    @Override
    public <R> R doInTransaction(final Callback<R> cb, final boolean requiresNew) {

        int transactionManagerStatus;
        R execute;
        try {
            transactionManagerStatus = transactionManager.getStatus();
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }

        if (requiresNew) {
            // USE CASE want to new transaction. IF exist transaction save the local variable, otherwise create new
            // transaction.
            LOGGER.info("REQUIRES NEW TRUE");
            Transaction suspend = null;
            if (transactionManagerStatus != Status.STATUS_NO_TRANSACTION) {
                LOGGER.info("EXIST TRANSACTION (SAVE LOCAL AND SUSPEND)");
                try {
                    suspend = transactionManager.suspend();
                } catch (SystemException e) {
                    throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
                }
            } else {
                LOGGER.info("NOT EXIST TRANSACTION");
            }

            LOGGER.info("START TRANSACTION");
            Transaction newTransaction = getTransaction();
            try {
                LOGGER.info("EXECUTE");
                execute = cb.execute();
                // if (getStatus(newTransaction) == Status.STATUS_MARKED_ROLLBACK) {
                if (getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                    if (suspend != null) {
                        LOGGER.info("SUSPEND TRANSACTION SETROLLBAYKONLY IN COMMIT SIDE");
                        setRollbackOnly(suspend);
                    }
                    LOGGER.info("ROLLBACK IN COMMIT SIDE");
                    // rollback(newTransaction);
                    rollback();
                } else {
                    LOGGER.info("COMMIT");
                    // commit(newTransaction);
                    commit();
                }
                resumeTransaction(suspend);
            } catch (RuntimeException e) {
                LOGGER.info("CATCH RTE");
                if (suspend != null) {
                    LOGGER.info("SUSPEND TRANSACTION SETROLLBAYKONLY");
                    setRollbackOnly(suspend);
                }
                LOGGER.info("ROLLBACK");
                // rollback(newTransaction);
                rollback();
                resumeTransaction(suspend);
                throw e;
            }

        } else {
            LOGGER.info("REQUIRES NEW FALSE");
            if (transactionManagerStatus == Status.STATUS_NO_TRANSACTION) {
                LOGGER.info("NOT EXIST TRANSACTION");
                LOGGER.info("START TRANSACTION");
                Transaction transaction = getTransaction();
                try {
                    LOGGER.info("EXECUTE");
                    execute = cb.execute();
                    if (getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                        LOGGER.info("ROLLBACK IN COMMIT SIDE");
                        // rollback(newTransaction);
                        rollback();
                    } else {
                        LOGGER.info("COMMIT");
                        // commit(newTransaction);
                        commit();
                    }
                    LOGGER.info("END TRANSACTION");
                } catch (RuntimeException e) {
                    LOGGER.info("CATCH RTE");
                    LOGGER.info("ROLLBACK");
                    // rollback(transaction);
                    rollback();
                    LOGGER.info("END TRANSACTION");
                    throw e;
                }
            } else {
                // USE CASE not want to new transaction and exist transaction.
                // Transaction transaction = getTransaction();
                LOGGER.info("EXIST TRANSACTION");
                try {
                    LOGGER.info("EXECUTE");
                    execute = cb.execute();
                    LOGGER.info("NOT CALL COMMIT");
                    LOGGER.info("END TRANSACTION");
                } catch (RuntimeException e) {
                    LOGGER.info("CATCH RTE");
                    LOGGER.info("SETROLLBACKONLY TRANSMANAGER");
                    // rollback(transaction);
                    // rollback();
                    setRollbackOnlyTransactionManager();
                    LOGGER.info("END TRANSACTION");
                    throw e;
                }
            }
        }

        return execute;
    }

    /**
     * Getting the transaction status. Obtain the status of the transaction associated with the current thread.
     * 
     * @return the transaction status.
     * 
     * @throws TransactionHelperException
     *             if has any transaction exception ({@link SystemException}).
     */
    private int getStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    // private int getStatus(final Transaction transaction) {
    // try {
    // return transaction.getStatus();
    // } catch (SystemException e) {
    // throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
    // }
    // }

    private Transaction getTransaction() {
        try {
            Transaction transaction = transactionManager.getTransaction();
            transactionManager.begin();
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        } catch (NotSupportedException e) {
            throw new TransactionHelperException(
                    "The thread is already associated with a transaction and the Transaction Manager implementation "
                            + "does not support nested transactions", e);
        }
    }

    private void resumeTransaction(final Transaction suspendTransaction) {
        LOGGER.info("END TRANSACTION");
        if (suspendTransaction != null) {
            try {
                LOGGER.info("RESUMING SUSPEND TRANSACTION");
                transactionManager.resume(suspendTransaction);
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

    // private void rollback(final Transaction transaction) {
    // try {
    // transaction.rollback();
    // } catch (IllegalStateException e) {
    // throw new TransactionHelperException("The current thread is not associated with a transaction", e);
    // } catch (SecurityException e) {
    // throw new TransactionHelperException("Not allowed to roll back the transaction", e);
    // } catch (SystemException e) {
    // throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
    // }
    // }

    private void setRollbackOnly(final Transaction transaction) {
        try {
            transaction.setRollbackOnly();
        } catch (IllegalStateException e) {
            throw new TransactionHelperException("The current thread is not associated with a transaction", e);
        } catch (SystemException e) {
            throw new TransactionHelperException("The transaction manager encounters an unexpected error", e);
        }
    }

    private void setRollbackOnlyTransactionManager() {
        try {
            transactionManager.setRollbackOnly();
        } catch (IllegalStateException e) {
            throw new TransactionHelperException("The current thread is not associated with a transaction", e);
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
