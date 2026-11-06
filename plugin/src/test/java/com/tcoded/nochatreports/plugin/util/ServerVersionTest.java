package com.tcoded.nochatreports.plugin.util;

import com.tcoded.nochatreports.nms.NmsVersion;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ServerVersionTest {

    @Test
    void paper263NativeVersionSelects263Adapter() {
        String version = ServerVersion.readMinecraftVersion(Native263.class, () -> {
            throw new AssertionError("API/build version must not override the game version");
        });
        assertEquals("26.3", version);
        assertEquals(NmsVersion.v26_3, NmsVersion.getNmsVersion(version));
    }

    @Test
    void paper263BuildStringStillSelects263WhenNativeApiIsAbsent() {
        AtomicInteger calls = new AtomicInteger();
        String version = ServerVersion.readMinecraftVersion(LegacySpigot.class, () -> {
            calls.incrementAndGet();
            return "26.3.build.244-stable";
        });
        assertEquals(1, calls.get());
        assertEquals(NmsVersion.v26_3, NmsVersion.getNmsVersion(version));
    }

    @Test
    void keepsNative262AndLegacy1194Selection() {
        String modern = ServerVersion.readMinecraftVersion(Native262.class, () -> "26.3.build.244-stable");
        assertEquals(NmsVersion.v26_2, NmsVersion.getNmsVersion(modern));

        String legacy = ServerVersion.readMinecraftVersion(LegacySpigot.class, () -> "1.19.4-R0.1-SNAPSHOT");
        assertEquals(NmsVersion.v1_19_4, NmsVersion.getNmsVersion(legacy));
    }

    @Test
    void nativeFailureDoesNotFallBackToAnotherAdapter() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> ServerVersion.readMinecraftVersion(BrokenNative.class, () -> "26.2"));
        assertInstanceOf(UnsupportedOperationException.class, error.getCause());
    }

    @Test
    void invalidNativeResultDoesNotFallBack() {
        for (Class<?> api : new Class<?>[]{NullNative.class, BlankNative.class, InvalidNative.class}) {
            assertThrows(IllegalStateException.class,
                    () -> ServerVersion.readMinecraftVersion(api, () -> "26.2"));
        }
    }

    @Test
    void unknownNativeVersionStaysUnsupported() {
        String version = ServerVersion.readMinecraftVersion(FutureNative.class, () -> "26.2");
        assertEquals("26.30", version);
        assertNull(NmsVersion.getNmsVersion(version));
    }

    public static class Native263 {
        public static String getMinecraftVersion() {
            return "26.3";
        }
    }

    public static class Native262 {
        public static String getMinecraftVersion() {
            return "26.2";
        }
    }

    public static class LegacySpigot {
    }

    public static class BrokenNative {
        public static String getMinecraftVersion() {
            throw new UnsupportedOperationException("unavailable");
        }
    }

    public static class NullNative {
        public static String getMinecraftVersion() {
            return null;
        }
    }

    public static class BlankNative {
        public static String getMinecraftVersion() {
            return " ";
        }
    }

    public static class InvalidNative {
        public static int getMinecraftVersion() {
            return 263;
        }
    }

    public static class FutureNative {
        public static String getMinecraftVersion() {
            return "26.30";
        }
    }
}
