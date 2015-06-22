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
@Reference(name = "transactionManager", referenceInterface = TransactionManager.class,
    policy = ReferencePolicy.STATIC,
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
