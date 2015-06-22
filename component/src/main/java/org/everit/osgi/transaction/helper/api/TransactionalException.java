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

/**
 * Exception class for the transaction helper.
 */
public class TransactionalException extends RuntimeException {

  /**
   * Generated serial version UID.
   */
  private static final long serialVersionUID = 7548858576510527613L;

  /**
   * Constructs a new transaction helper exception with the specified message.
   *
   * @param message
   *          The message of the exception.
   */
  public TransactionalException(final String message) {
    super(message);
  }

  /**
   * Constructs a new transaction helper exception with the specified detail message and cause.
   *
   * @param msg
   *          the detail message.
   * @param cause
   *          the cause.
   */
  public TransactionalException(final String msg, final Throwable cause) {
    super(msg, cause);
  }

  /**
   * Constructs a new transaction helper exception with the specified cause.
   *
   * @param cause
   *          the cause.
   */
  public TransactionalException(final Throwable cause) {
    super(cause);
  }

}
