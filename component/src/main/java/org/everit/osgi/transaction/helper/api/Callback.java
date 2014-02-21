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

/**
 * Interface for the Callback.
 * 
 * @param <R>
 *            the result object.
 */
public interface Callback<R> {

    /**
     * Perform a unit of transactional work.
     * 
     * @return the result of the unit of work.
     */
    R execute();
}
