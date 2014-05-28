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
package org.everit.osgi.transaction.helper.api;

import javax.transaction.Status;

/**
 * TransactionHelper OSGi service can be used to manipulate transactions on the current thread. The methods of this
 * service can be used with normal Callback objects, anonymous classes or lambda expressions.
 */
public interface TransactionHelper {

    /**
     * Support a current transaction, throw an exception if none exists. In case there is an exception in the callback,
     * the transaction status is set to MARKED_ROLLBACK.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     * @throws IllegalStateException
     *             if there is no active transaction.
     */
    <R> R mandatory(Callback<R> callback);

    /**
     * Execute non-transactionally, throw an exception if a transaction exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     * @throws IllegalStateException
     *             if the status of the current thread is different from {@link Status#STATUS_NO_TRANSACTION}.
     */
    <R> R never(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     * @throws IllegalStateException
     *             if the transaction status at the time of calling this function is neither
     *             {@link Status#STATUS_ACTIVE} nor {@link Status#STATUS_NO_TRANSACTION}.
     */
    <R> R notSupported(Callback<R> callback);

    /**
     * Support a current transaction, create a new one if none exists. In case there is an exception in the callback and
     * the function call created the transaction, the transaction will be rollbacked. In case there is an exception and
     * there was already a transaction when the method was called, the transaction status is set to MARKED_ROLLBACK.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     * @throws IllegalStateException
     *             if the transaction status at the time of calling this function is neither
     *             {@link Status#STATUS_ACTIVE} nor {@link Status#STATUS_NO_TRANSACTION}.
     */
    <R> R required(Callback<R> callback);

    /**
     * Create a new transaction, suspend the current transaction if one exists. In case there is an exception, the newly
     * created transaction will be rolled back.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R requiresNew(Callback<R> callback);

    /**
     * Support a current transaction, execute non-transactionally if none exists. If there was an ACTIVE transaction at
     * the time calling the function and the callback throws an exception, the transaction status will be
     * MARKED_ROLLBACK.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     * @throws IllegalStateException
     *             if the transaction status at the time of calling this function is neither
     *             {@link Status#STATUS_ACTIVE} nor {@link Status#STATUS_NO_TRANSACTION}.
     */
    <R> R supports(Callback<R> callback);

}
