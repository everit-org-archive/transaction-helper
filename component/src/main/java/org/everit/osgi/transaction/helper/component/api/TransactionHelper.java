package org.everit.osgi.transaction.helper.component.api;

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

/**
 * The helper service to start the {@link Callback} in new transaction.
 */
public interface TransactionHelper {

    /**
     * The method executes the {@link Callback} in new transaction and returns the specific generic type.
     * 
     * If has a {@link RuntimeException} or other exception roll backing the transaction.
     * 
     * @param cb
     *            contains the execute code which want to run in the transaction.
     * @param requiresNew
     *            <code>true</code> if want a new transaction or <code>false</code> if want to partake the exist
     *            transaction.
     * @return the execute result.
     * 
     * @throws TransactionHelperException
     *             if has {@link RuntimeException} or other transaction exception.
     */
    <R> R doInTransaction(final Callback<R> cb, final boolean requiresNew);

}
