package org.everit.osgi.transaction.helper.tests;

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

import org.everit.osgi.transaction.helper.component.api.TransactionHelper;
import org.junit.Test;

/**
 * Test interface to the {@link TransactionHelper} component.
 */
public interface TransactionHelperTest {

    // 13.
    // @Test
    void test13();

    // 14.
    // @Test
    void test14();

    // 15.
    // @Test
    void test15();

    // 16.
    // @Test
    void test16();

    // 17.
    // @Test
    void test17();

    // 18.
    // @Test
    void test18();

    // 19.
    // @Test
    void test19();

    // 20.
    @Test
    void test20();

    // 11.
    // @Test
    void testExistTransactionNotRequiresNewTransactionInsideGetZeroCBWithFalse();

    // 10.
    // @Test
    void testExistTransactionNotRequiresNewTransactionInsideGetZeroCBWithTrue();

    // 12.
    // @Test
    void testExistTransactionNotRequiresNewTransactionInsideIllegalArgumentExceptionCBWithFalse();

    // 9.
    // @Test
    void testExistTransactionNotRequiresNewTransactionInsideIllegalArgumentExceptionCBWithTrue();

    // 7.
    // @Test
    void testExistTransactionRequiresNewTransactionInsideGetZeroCBWithFalse();

    // 6.
    // @Test
    void testExistTransactionRequiresNewTransactionInsideGetZeroCBWithTrue();

    // 8.
    // @Test
    void testExistTransactionRequiresNewTransactionInsideIllegalArgumentExceptionCBWithFalse();

    // 5.
    // @Test
    void testExistTransactionRequiresNewTransactionInsideIllegalArgumentExceptionCBWithTrue();

    // 4.
    // @Test
    void testNoTransactionNotRequiresNewTransactionGetZeroCB();

    // 3.
    // @Test
    void testNoTransactionNotRequiresNewTransactionIllegalArgumentExceptionCB();

    // 2.
    // @Test
    void testNoTransactionRequiresNewTransactionGetZeroCB();

    // 1.
    // @Test
    void testNoTransactionRequiresNewTransactionIllegalArgumentExceptionCB();
}
