/**
 * This file is part of Everit - Transaction Helper.
 *
 * Everit - Transaction Helper is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Transaction Helper is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Transaction Helper.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.transaction.helper.internal;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionConstants;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.api.TransactionalException;
import org.osgi.service.log.LogService;

public class TransactionHelperImpl implements TransactionHelper {

    private LogService logService;

    private TransactionManager transactionManager;

    private <R> R doInNewTransaction(final Callback<R> callback) {
        try {
            transactionManager.begin();
        } catch (NotSupportedException e) {
            throw new TransactionalException(e);
        } catch (SystemException e) {
            throw new TransactionalException(e);
        }

        R result = null;

        try {
            result = callback.execute();
        } catch (RuntimeException e) {
            rollbackAndReThrow(e);
        }

        try {
            transactionManager.commit();
        } catch (Exception e) {
            // No rollback is necessary here as if there was an exception during calling commit, the transaction is
            // either rolled back or there is no transaction to roll back.
            throwWrappedIfNotRuntimeOrOriginal(e);
        }
        return result;
    }

    private <R> R doInOngoingTransaction(final Callback<R> callback) {
        Transaction transaction = getTransaction();
        try {
            return callback.execute();
        } catch (RuntimeException e) {
            setRollbackOnly(transaction, e);
            throw e;
        }
    }

    private <R> R doInSuspended(final Callback<R> callback) {
        Transaction transaction = getTransaction();
        try {
            transactionManager.suspend();
        } catch (SystemException e) {
            throw new TransactionalException(e);
        }

        RuntimeException thrownException = null;
        try {
            return callback.execute();
        } catch (RuntimeException e) {
            thrownException = e;
            throw e;
        } finally {
            resume(transaction, thrownException);
        }
    }

    private void forceTransactionStatus(final int allowedStatus) {
        int status = getStatus();
        if (status != allowedStatus) {
            throwNotAllowedStatus(status, allowedStatus);
        }
    }

    private int getStatus() {
        try {
            return transactionManager.getStatus();
        } catch (SystemException e) {
            throw new TransactionalException(e);
        }
    }

    private Transaction getTransaction() {
        try {
            return transactionManager.getTransaction();
        } catch (SystemException e) {
            throw new TransactionalException(e);
        }
    }

    public <R> R mandatory(final Callback<R> callback) {
        forceTransactionStatus(Status.STATUS_ACTIVE);
        return doInOngoingTransaction(callback);
    }

    public <R> R never(final Callback<R> callback) {
        forceTransactionStatus(Status.STATUS_NO_TRANSACTION);
        return callback.execute();
    }

    public <R> R notSupported(final Callback<R> callback) {
        int status = getStatus();
        if (Status.STATUS_NO_TRANSACTION == status) {
            return callback.execute();
        }

        if (status != Status.STATUS_ACTIVE) {
            throwNotAllowedStatus(status, Status.STATUS_NO_TRANSACTION, Status.STATUS_ACTIVE);
        }

        return doInSuspended(callback);
    }

    public <R> R required(final Callback<R> callback) {
        int status = getStatus();
        if (Status.STATUS_ACTIVE == status) {
            return doInOngoingTransaction(callback);
        }
        if (Status.STATUS_NO_TRANSACTION != status) {
            throwNotAllowedStatus(status, Status.STATUS_ACTIVE, Status.STATUS_NO_TRANSACTION);
        }
        return doInNewTransaction(callback);
    }

    public <R> R requiresNew(final Callback<R> callback) {
        int status = getStatus();
        if (Status.STATUS_NO_TRANSACTION == status) {
            return doInNewTransaction(callback);
        }
        return doInSuspended(new Callback<R>() {

            public R execute() {
                return doInNewTransaction(callback);
            }
        });
    }

    private void resume(final Transaction transaction, final RuntimeException thrownException) {
        try {
            transactionManager.resume(transaction);
        } catch (Exception e) {
            if (thrownException != null) {
                suppressException(thrownException, e);
                throw thrownException;
            } else {
                throwWrappedIfNotRuntimeOrOriginal(e);
            }
        }
    }

    private void rollbackAndReThrow(final RuntimeException thrownException) {
        try {
            transactionManager.rollback();
        } catch (Exception e) {
            suppressException(thrownException, e);
        }
        throw thrownException;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    private void setRollbackOnly(final Transaction transaction, final RuntimeException thrownException) {
        try {
            transaction.setRollbackOnly();
        } catch (Exception e) {
            suppressException(thrownException, e);
        }
        throw thrownException;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public <R> R supports(final Callback<R> callback) {
        int status = getStatus();
        if (Status.STATUS_NO_TRANSACTION == status) {
            return callback.execute();
        }
        if (Status.STATUS_ACTIVE != status) {
            throwNotAllowedStatus(status, Status.STATUS_ACTIVE, Status.STATUS_NO_TRANSACTION);
        }
        Transaction transaction = getTransaction();
        try {
            return callback.execute();
        } catch (RuntimeException e) {
            setRollbackOnly(transaction, e);
            throw e;
        }
    }

    private void suppressException(final Throwable originalException, final Throwable suppressedException) {
        if (logService != null) {
            logService.log(LogService.LOG_ERROR, "Suppressed exception", suppressedException);
        } else {
            System.err.println("Suppressed exception");
            suppressedException.printStackTrace();
        }
    }

    private void throwNotAllowedStatus(final int currentStatus, final int... allowedStatuses) {
        StringBuilder sb = new StringBuilder("Allowed status");
        int n = allowedStatuses.length;
        if (n == 1) {
            sb.append(": ").append(TransactionConstants.STATUS_NAME_BY_CODE.get(allowedStatuses[0]));
        } else {
            sb.append("es: [");
            for (int i = 0; i < n; i++) {
                sb.append(TransactionConstants.STATUS_NAME_BY_CODE.get(allowedStatuses[i]));
                if (i < (n - 1)) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
        sb.append("; Current status: ").append(TransactionConstants.STATUS_NAME_BY_CODE.get(currentStatus));

        throw new IllegalStateException(sb.toString());
    }

    private void throwWrappedIfNotRuntimeOrOriginal(final Exception e) {
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        throw new TransactionalException(e);
    }
}
