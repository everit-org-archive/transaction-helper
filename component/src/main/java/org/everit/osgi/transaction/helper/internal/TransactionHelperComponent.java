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

import java.util.function.Supplier;

import javax.transaction.TransactionManager;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.transaction.helper.api.TransactionHelper;

/**
 * Implementation of {@link TransactionHelper}.
 */
@Component(name = "org.everit.osgi.transaction.helper.TransactionHelper", metatype = true)
@Properties({ @Property(name = "transactionManager.target") })
@Service(value = TransactionHelper.class)
@Reference(name = "transactionManager", referenceInterface = TransactionManager.class, policy = ReferencePolicy.STATIC,
bind = "setTransactionManager")
public class TransactionHelperComponent implements TransactionHelper {

    TransactionHelperImpl wrapped = new TransactionHelperImpl();

    @Override
    public <R> R mandatory(final Supplier<R> callback) {
        return wrapped.mandatory(callback);
    }

    @Override
    public <R> R never(final Supplier<R> callback) {
        return wrapped.never(callback);
    }

    @Override
    public <R> R notSupported(final Supplier<R> callback) {
        return wrapped.notSupported(callback);
    }

    @Override
    public <R> R required(final Supplier<R> callback) {
        return wrapped.required(callback);
    }

    @Override
    public <R> R requiresNew(final Supplier<R> callback) {
        return wrapped.requiresNew(callback);
    }

    protected void setTransactionManager(final TransactionManager transactionManager) {
        wrapped.setTransactionManager(transactionManager);
    }

    @Override
    public <R> R supports(final Supplier<R> callback) {
        return wrapped.supports(callback);
    }

    protected void unbindTransactionManager(final TransactionManager transactionManager) {
        wrapped.setTransactionManager(transactionManager);
    }

}
