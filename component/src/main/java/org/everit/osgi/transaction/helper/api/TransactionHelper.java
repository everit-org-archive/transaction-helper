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
package org.everit.osgi.transaction.helper.api;

import java.util.function.Supplier;

/**
 * TransactionHelper OSGi service can be used to manipulate transactions on the current thread. The
 * methods of this service can be used with normal Callback objects, anonymous classes or lambda
 * expressions.
 */
public interface TransactionHelper {

  /**
   * Support a current transaction, throw an exception if none exists. In case there is an exception
   * in the callback, the transaction status is set to MARKED_ROLLBACK.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   * @throws IllegalStateException
   *           if there is no active transaction.
   */
  <R> R mandatory(Supplier<R> callback);

  /**
   * Execute non-transactionally, throw an exception if a transaction exists.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   * @throws IllegalStateException
   *           if the status of the current thread is different from
   *           {@link javax.transaction.Status#STATUS_NO_TRANSACTION}.
   */
  <R> R never(Supplier<R> callback);

  /**
   * Execute non-transactionally, suspend the current transaction if one exists.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   * @throws IllegalStateException
   *           if the transaction status at the time of calling this function is neither
   *           {@link javax.transaction.Status#STATUS_ACTIVE} nor
   *           {@link javax.transaction.Status#STATUS_NO_TRANSACTION}.
   */
  <R> R notSupported(Supplier<R> callback);

  /**
   * Support a current transaction, create a new one if none exists. In case there is an exception
   * in the callback and the function call created the transaction, the transaction will be
   * rollbacked. In case there is an exception and there was already a transaction when the method
   * was called, the transaction status is set to MARKED_ROLLBACK.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   * @throws IllegalStateException
   *           if the transaction status at the time of calling this function is neither
   *           {@link javax.transaction.Status#STATUS_ACTIVE} nor
   *           {@link javax.transaction.Status#STATUS_NO_TRANSACTION}.
   */
  <R> R required(Supplier<R> callback);

  /**
   * Create a new transaction, suspend the current transaction if one exists. In case there is an
   * exception, the newly created transaction will be rolled back.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   */
  <R> R requiresNew(Supplier<R> callback);

  /**
   * Support a current transaction, execute non-transactionally if none exists. If there was an
   * ACTIVE transaction at the time calling the function and the callback throws an exception, the
   * transaction status will be MARKED_ROLLBACK.
   *
   * @param <R>
   *          Return type.
   * @param callback
   *          The callback instance will be called inside.
   * @return The result of the callback execution.
   * @throws IllegalStateException
   *           if the transaction status at the time of calling this function is neither
   *           {@link javax.transaction.Status#STATUS_ACTIVE} nor
   *           {@link javax.transaction.Status#STATUS_NO_TRANSACTION}.
   */
  <R> R supports(Supplier<R> callback);

}
