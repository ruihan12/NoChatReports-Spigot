package com.tcoded.nochatreports.nms;

public enum NmsVersion {

    v26_3 ("26_3"),
    v26_2 ("26_2"),
    v26_1_2 ("26_1(_[12])?(_build_\\d+)?"),
    v1_21_11 ("1_21_11"),
    v1_21_10 ("1_21_(9|10)"),
    v1_21_8 ("1_21_8"),
    v1_21_5 ("1_21_5"),
    v1_21_4 ("1_21_4"),
    v1_21 ("1_21(_1)?"),
    v1_20_6 ("1_20_[5-6]"),
    v1_20_4 ("1_20_4"),
    v1_19_4 ("1_19_4")
    ;

    String[] versions;
    NmsVersion parent;

    NmsVersion(String... versions) {
        this.versions = versions;
    }

    public String[] getVersions() {
        return versions;
    }

    public static NmsVersion getNmsVersion(String version) {
        if (version == null) {
            return null;
        }

        NmsVersion[] values = NmsVersion.values();
        String replacedVersion = normalizeMinecraftVersion(version).replace(".", "_");
        for (NmsVersion nmsVersion : values) {
            for (String matchVersion : nmsVersion.getVersions()) {
                if (replacedVersion.matches(matchVersion)) {
                    return nmsVersion;
                }
            }
        }

        return null;
    }

    /**
     * Reduce a Bukkit or Paper build version to its numeric Minecraft version.
     * {@code 26.3.build.244-stable} and {@code 1.19.4-R0.1-SNAPSHOT} become {@code 26.3} and {@code 1.19.4}.
     * Pre-releases and unrecognized strings are left unchanged so they stay unsupported.
     */
    public static String normalizeMinecraftVersion(String version) {
        if (version == null) {
            return null;
        }
        return version.replaceFirst("^(\\d+\\.\\d+(?:\\.\\d+)?)(?:\\.build\\..*|-R\\d.*)?$", "$1");
    }

    public static NmsVersion getMostRecentVersion() {
        NmsVersion[] values = NmsVersion.values();
        return values[0];
    }
}