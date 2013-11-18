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
 * Exception class for the transaction helper.
 */
public class TransactionalException extends RuntimeException {

    /**
     * Generated serial version UID.
     */
    private static final long serialVersionUID = 7548858576510527613L;

    /**
     * Constructs a new transaction helper exception with the specified detail message and cause.
     * 
     * @param msg
     *            the detail message.
     * @param cause
     *            the cause.
     */
    public TransactionalException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Constructs a new transaction helper exception with the specified cause.
     * 
     * @param cause
     *            the cause.
     */
    public TransactionalException(final Throwable cause) {
        super(cause);
    }
}
