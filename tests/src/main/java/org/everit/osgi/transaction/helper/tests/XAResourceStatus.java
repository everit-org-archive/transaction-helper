/**
 * This file is part of Everit - Transaction Helper Tests.
 *
 * Everit - Transaction Helper Tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - Transaction Helper Tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - Transaction Helper Tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.transaction.helper.tests;

import javax.transaction.Status;

/**
 * Extending javax.persistence.Status interface which contains the start and end status.
 */
public interface XAResourceStatus extends Status {

    /**
     * The transaction is assigned resource and started.
     */
    int STATUS_START = 10;

    /**
     * The transaction is assigned resource and ended without any actions.
     */
    int STATUS_END = 14;
}
