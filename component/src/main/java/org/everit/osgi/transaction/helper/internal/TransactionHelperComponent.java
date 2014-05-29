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

import javax.transaction.TransactionManager;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.References;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.api.Callback;
import org.everit.osgi.transaction.helper.api.TransactionHelper;
import org.osgi.service.log.LogService;

/**
 * Implementation of {@link TransactionHelper}.
 */
@Component(name = "org.everit.osgi.transaction.helper.TransactionHelper", metatype = true)
@Properties({ @Property(name = "transactionManager.target"), @Property(name = "logService.target") })
@Service(value = TransactionHelper.class)
@References({
        @Reference(name = "logService", referenceInterface = LogService.class, bind = "bindLogService"),
        @Reference(name = "transactionManager", referenceInterface = TransactionManager.class,
                bind = "bindTransactionManager") })
public class TransactionHelperComponent implements TransactionHelper {

    private final TransactionHelperImpl wrapped = new TransactionHelperImpl();

    protected void bindLogService(LogService logService) {
        wrapped.setLogService(logService);
    }

    protected void bindTransactionManager(final TransactionManager transactionManager) {
        wrapped.setTransactionManager(transactionManager);
    }

    public <R> R mandatory(Callback<R> callback) {
        return wrapped.mandatory(callback);
    }

    public <R> R never(Callback<R> callback) {
        return wrapped.never(callback);
    }

    public <R> R notSupported(Callback<R> callback) {
        return wrapped.notSupported(callback);
    }

    public <R> R required(Callback<R> callback) {
        return wrapped.required(callback);
    }

    public <R> R requiresNew(Callback<R> callback) {
        return wrapped.requiresNew(callback);
    }

    public void setLogService(LogService logService) {
        wrapped.setLogService(logService);
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        wrapped.setTransactionManager(transactionManager);
    }

    public <R> R supports(Callback<R> callback) {
        return wrapped.supports(callback);
    }

    protected void unbindLogService(LogService logService) {
        wrapped.setLogService(logService);
    }

    protected void unbindTransactionManager(final TransactionManager transactionManager) {
        wrapped.setTransactionManager(transactionManager);
    }

}
