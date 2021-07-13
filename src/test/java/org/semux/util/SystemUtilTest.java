/**
 * Copyright (c) 2017-2020 The Semux Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.semux.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import static org.semux.util.SystemUtil.OsName.LINUX;
import static org.semux.util.SystemUtil.OsName.MACOS;
import static org.semux.util.SystemUtil.OsName.WINDOWS;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

import org.junit.Test;
import org.semux.util.exception.UnreachableException;

public class SystemUtilTest {

    private Logger logger = Logger.getLogger(SystemUtilTest.class.getName());

    @Test
    public void testCompareVersion() {
        assertEquals(0, SystemUtil.compareVersion("1.0.0", "1.0.0"));
        assertEquals(1, SystemUtil.compareVersion("1.0.0", "1.0.0-alpha"));
        assertEquals(1, SystemUtil.compareVersion("2.0.1", "1.0.2"));
        assertEquals(-1, SystemUtil.compareVersion("2.0.1-beta", "2.0.1-beta.1"));
    }

    @Test
    public void testGetIp() {
        Instant begin = Instant.now();
        String ip = SystemUtil.getIp();
        logger.info(String.format("IP address = %s, took %s ms", ip, Duration.between(begin, Instant.now()).toMillis()));

        assertNotEquals("127.0.0.1", ip);
    }

    @Test
    public void testGetAvailableMemorySize() {
        long size = SystemUtil.getAvailableMemorySize();
        logger.info(String.format("Available memory size = %s MB", size / 1024L / 1024L));

        assertTrue(size > 0);
        assertTrue(size < 64L * 1024L * 1024L * 1024L);
        assertTrue(size != 0xffffffffL);
    }

    @Test
    public void testGetTotalMemorySize() {
        long size = SystemUtil.getTotalMemorySize();
        logger.info(String.format("Total memory size = %s MB", size / 1024L / 1024L));

        assertTrue(size > 0);
        assertTrue(size < 64L * 1024L * 1024L * 1024L);
        assertTrue(size != 0xffffffffL);
    }

    @Test
    public void testGetUsedHeapSize() {
        long size = SystemUtil.getUsedHeapSize();
        logger.info(String.format("Used heap size = %s MB", size / 1024L / 1024L));

        assertTrue(size > 0);
        assertTrue(size < 4L * 1024L * 1024L * 1024L);
    }

    @Test
    public void testBench() {
        logger.info(String.format("System benchmark result = %s", SystemUtil.bench()));
    }

    @Test
    public void testIsWindowsVCRedist2012Installed() {
        assumeTrue(SystemUtil.getOsName() == WINDOWS);
        assertTrue(SystemUtil.isWindowsVCRedist2012Installed());
    }

    @Test
    public void testIsJavaPlatformModuleSystemAvailable() {
        switch (ManagementFactory.getRuntimeMXBean().getSpecVersion()) {
        case "1.8":
            assertFalse(SystemUtil.isJavaPlatformModuleSystemAvailable());
            break;
        case "9":
        case "10":
        case "11":
        case "12":
        case "13":
        case "14":
        case "15":
        case "16":
            assertTrue(SystemUtil.isJavaPlatformModuleSystemAvailable());
            break;
        default:
            // do not check for other versions
        }
    }

    @Test
    public void testIsPosixTrue() {
        SystemUtil.OsName os = SystemUtil.getOsName();
        assumeTrue(os.equals(LINUX) || os.equals(MACOS));
        assertTrue(SystemUtil.isPosix());
    }

    @Test
    public void testIsPosixFalse() {
        SystemUtil.OsName os = SystemUtil.getOsName();
        assumeTrue(os.equals(WINDOWS));
        assertFalse(SystemUtil.isPosix());
    }
}
