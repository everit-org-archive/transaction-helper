/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.transaction.helper.internal;

import java.util.function.Supplier;

import javax.transaction.NotSupportedException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.everit.osgi.transaction.helper.api.TransactionConstants;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.everit.osgi.transaction.helper.api.TransactionalException;

/**
 * Default implementation class of {@link TransactionHelper}.
 */
public class TransactionHelperImpl implements TransactionHelper {

  private TransactionManager transactionManager;

  private <R> R doInNewTransaction(final Supplier<R> callback) {
    try {
      transactionManager.begin();
    } catch (NotSupportedException e) {
      throw new TransactionalException(e);
    } catch (SystemException e) {
      throw new TransactionalException(e);
    }

    R result = null;

    try {
      result = callback.get();
    } catch (Throwable e) {
      rollbackAndReThrow(e);
    }

    try {
      transactionManager.commit();
    } catch (Throwable e) {
      // No rollback is necessary here as if there was an exception during calling commit, the
      // transaction is
      // either rolled back or there is no transaction to roll back.
      throwOriginalIfUncheckedOrWrapped(e);
    }
    return result;
  }

  private <R> R doInOngoingTransaction(final Supplier<R> callback) {
    Transaction transaction = getTransaction();
    try {
      return callback.get();
    } catch (Throwable e) {
      setRollbackOnly(transaction, e);
      return null;
    }
  }

  private <R> R doInSuspended(final Supplier<R> callback) {
    Transaction transaction = getTransaction();
    try {
      transactionManager.suspend();
    } catch (SystemException e) {
      throw new TransactionalException(e);
    }

    try {
      R result = callback.get();
      resume(transaction, null);
      return result;
    } catch (Throwable e) {
      resume(transaction, e);
      return null;
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

  @Override
  public <R> R mandatory(final Supplier<R> callback) {
    forceTransactionStatus(Status.STATUS_ACTIVE);
    return doInOngoingTransaction(callback);
  }

  @Override
  public <R> R never(final Supplier<R> callback) {
    forceTransactionStatus(Status.STATUS_NO_TRANSACTION);
    return callback.get();
  }

  @Override
  public <R> R notSupported(final Supplier<R> callback) {
    int status = getStatus();
    if (Status.STATUS_NO_TRANSACTION == status) {
      return callback.get();
    }

    if (status != Status.STATUS_ACTIVE) {
      throwNotAllowedStatus(status, Status.STATUS_NO_TRANSACTION, Status.STATUS_ACTIVE);
    }

    return doInSuspended(callback);
  }

  @Override
  public <R> R required(final Supplier<R> callback) {
    int status = getStatus();
    if (Status.STATUS_ACTIVE == status) {
      return doInOngoingTransaction(callback);
    }
    if (Status.STATUS_NO_TRANSACTION != status) {
      throwNotAllowedStatus(status, Status.STATUS_ACTIVE, Status.STATUS_NO_TRANSACTION);
    }
    return doInNewTransaction(callback);
  }

  @Override
  public <R> R requiresNew(final Supplier<R> callback) {
    int status = getStatus();
    if (Status.STATUS_NO_TRANSACTION == status) {
      return doInNewTransaction(callback);
    }
    return doInSuspended(new Supplier<R>() {

      @Override
      public R get() {
        return doInNewTransaction(callback);
      }
    });
  }

  private void resume(final Transaction transaction, final Throwable thrownThrowable) {
    try {
      transactionManager.resume(transaction);
    } catch (Throwable e) {
      if (thrownThrowable != null) {
        suppressThrowable(thrownThrowable, e);
      } else {
        throwOriginalIfUncheckedOrWrapped(e);
      }
    }
  }

  private void rollbackAndReThrow(final Throwable thrownThrowable) {
    try {
      transactionManager.rollback();
    } catch (Throwable e) {
      suppressThrowable(thrownThrowable, e);
    }
    throwOriginalIfUncheckedOrWrapped(thrownThrowable);
  }

  private void setRollbackOnly(final Transaction transaction,
      final Throwable thrownThrowable) {
    try {
      transaction.setRollbackOnly();
    } catch (Throwable e) {
      suppressThrowable(thrownThrowable, e);
    }
    throwOriginalIfUncheckedOrWrapped(thrownThrowable);
  }

  public void setTransactionManager(final TransactionManager transactionManager) {
    this.transactionManager = transactionManager;
  }

  @Override
  public <R> R supports(final Supplier<R> callback) {
    int status = getStatus();
    if (Status.STATUS_NO_TRANSACTION == status) {
      return callback.get();
    }
    if (Status.STATUS_ACTIVE != status) {
      throwNotAllowedStatus(status, Status.STATUS_ACTIVE, Status.STATUS_NO_TRANSACTION);
    }
    Transaction transaction = getTransaction();
    try {
      return callback.get();
    } catch (Throwable e) {
      setRollbackOnly(transaction, e);
      return null;
    }
  }

  private void suppressThrowable(final Throwable originalThrowable,
      final Throwable suppressedThrowable) {
    originalThrowable.addSuppressed(suppressedThrowable);
  }

  private void throwNotAllowedStatus(final int currentStatus, final int... allowedStatuses) {
    StringBuilder sb = new StringBuilder("Allowed status");
    int n = allowedStatuses.length;
    if (n == 1) {
      sb.append(": ").append(
          TransactionConstants.STATUS_NAME_BY_CODE.get(allowedStatuses[0]));
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
    sb.append("; Current status: ").append(
        TransactionConstants.STATUS_NAME_BY_CODE.get(currentStatus));

    throw new IllegalStateException(sb.toString());
  }

  private void throwOriginalIfUncheckedOrWrapped(final Throwable e) {
    if (e instanceof RuntimeException) {
      throw (RuntimeException) e;
    } else if (e instanceof Error) {
      throw (Error) e;
    }
    throw new TransactionalException(e);
  }
}
