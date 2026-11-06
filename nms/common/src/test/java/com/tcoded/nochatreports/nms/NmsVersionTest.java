package com.tcoded.nochatreports.nms;

import com.tcoded.nochatreports.nms.types.NmsProviderConfig;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NmsVersionTest {

    @Test
    void paper263Build244SelectsDedicatedAdapter() {
        // apiVersioning.json "version" from paper-api 26.3.build.244-stable.
        String bukkitVersion = "26.3.build.244-stable";
        assertEquals("26.3", NmsVersion.normalizeMinecraftVersion(bukkitVersion));
        assertEquals(NmsVersion.v26_3, NmsVersion.getNmsVersion(bukkitVersion));
        assertEquals(NmsVersion.v26_3, NmsVersion.getNmsVersion("26.3"));
        assertEquals(NmsVersion.v26_3, NmsVersion.getMostRecentVersion());
    }

    @Test
    void keepsPreviouslySupportedVersions() {
        assertEquals(NmsVersion.v26_2, NmsVersion.getNmsVersion("26.2"));
        assertEquals(NmsVersion.v26_2, NmsVersion.getNmsVersion("26.2.build.59-stable"));
        assertEquals(NmsVersion.v26_1_2, NmsVersion.getNmsVersion("26.1.2.build.20-stable"));
        assertEquals(NmsVersion.v26_1_2, NmsVersion.getNmsVersion("26.1"));
        assertEquals(NmsVersion.v26_1_2, NmsVersion.getNmsVersion("26.1.1"));
        assertEquals(NmsVersion.v1_21_11, NmsVersion.getNmsVersion("1.21.11-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21_10, NmsVersion.getNmsVersion("1.21.9-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21_10, NmsVersion.getNmsVersion("1.21.10-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21_8, NmsVersion.getNmsVersion("1.21.8-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21_5, NmsVersion.getNmsVersion("1.21.5-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21_4, NmsVersion.getNmsVersion("1.21.4-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21, NmsVersion.getNmsVersion("1.21-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_21, NmsVersion.getNmsVersion("1.21.1-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_20_6, NmsVersion.getNmsVersion("1.20.5-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_20_6, NmsVersion.getNmsVersion("1.20.6-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_20_4, NmsVersion.getNmsVersion("1.20.4-R0.1-SNAPSHOT"));
        assertEquals(NmsVersion.v1_19_4, NmsVersion.getNmsVersion("1.19.4-R0.1-SNAPSHOT"));
    }

    @Test
    void doesNotTreatNearbyVersionsAs263() {
        assertNull(NmsVersion.getNmsVersion("26.30"));
        assertNull(NmsVersion.getNmsVersion("26.4"));
        assertNull(NmsVersion.getNmsVersion("26.3.1"));
        assertEquals("26.3-pre-2", NmsVersion.normalizeMinecraftVersion("26.3-pre-2"));
        assertNull(NmsVersion.getNmsVersion("26.3-pre-2"));
        assertNull(NmsVersion.getNmsVersion("unknown"));
        assertNull(NmsVersion.getNmsVersion(null));
        assertNull(NmsVersion.getNmsVersion(""));
    }

    @Test
    void unsupportedVersionReturnsNullWithoutPrinting() {
        ByteArrayOutputStream errors = new ByteArrayOutputStream();
        PrintStream previous = System.err;
        try (PrintStream capture = new PrintStream(errors, true, StandardCharsets.UTF_8)) {
            System.setErr(capture);
            assertNull(NmsProvider.getNmsProvider("26.4", new NmsProviderConfig(true, () -> false)));
            assertNull(NmsProvider.getNmsProvider(null, new NmsProviderConfig(true, () -> false)));
        } finally {
            System.setErr(previous);
        }
        assertEquals("", errors.toString(StandardCharsets.UTF_8));
    }
}
