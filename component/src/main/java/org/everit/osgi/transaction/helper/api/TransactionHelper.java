package org.everit.osgi.transaction.helper.api;

/**
 * TransactionHelper OSGi service can be used to manipulate transactions on the current thread. The methods of this
 * service can be used with normal Callback objects, anonymous classes or lambda expressions.
 */
public interface TransactionHelper {

    /**
     * Support a current transaction, create a new one if none exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R required(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R requiresNew(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R mandatory(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R never(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R notSupported(Callback<R> callback);

    /**
     * Execute non-transactionally, suspend the current transaction if one exists.
     * 
     * @param callback
     *            The callback instance will be called inside.
     * @return The result of the callback execution.
     */
    <R> R supports(Callback<R> callback);

}
