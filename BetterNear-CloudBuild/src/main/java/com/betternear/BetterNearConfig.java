package com.betternear;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class BetterNearConfig {
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("betternear.properties");
    public static boolean hudEnabled = true;
    public static boolean showHeads = true;
    public static boolean showDistance = true;
    public static boolean showDirections = true;
    public static int maxPlayers = 5;
    public static double maxDistance = 128.0;
    public static int panelColor = 0xCC171B22;

    private BetterNearConfig() {}

    public static void load() {
        if (!Files.exists(FILE)) { save(); return; }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(FILE)) {
            p.load(in);
            hudEnabled = Boolean.parseBoolean(p.getProperty("hudEnabled", "true"));
            showHeads = Boolean.parseBoolean(p.getProperty("showHeads", "true"));
            showDistance = Boolean.parseBoolean(p.getProperty("showDistance", "true"));
            showDirections = Boolean.parseBoolean(p.getProperty("showDirections", "true"));
            maxPlayers = clamp(Integer.parseInt(p.getProperty("maxPlayers", "5")), 1, 10);
            maxDistance = Math.max(16, Math.min(512, Double.parseDouble(p.getProperty("maxDistance", "128"))));
            panelColor = (int) Long.parseLong(p.getProperty("panelColor", "CC171B22"), 16);
        } catch (Exception ignored) { /* Defaults remain usable if the config is damaged. */ }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("hudEnabled", Boolean.toString(hudEnabled));
        p.setProperty("showHeads", Boolean.toString(showHeads));
        p.setProperty("showDistance", Boolean.toString(showDistance));
        p.setProperty("showDirections", Boolean.toString(showDirections));
        p.setProperty("maxPlayers", Integer.toString(maxPlayers));
        p.setProperty("maxDistance", Double.toString(maxDistance));
        p.setProperty("panelColor", String.format("%08X", panelColor));
        try {
            Files.createDirectories(FILE.getParent());
            try (OutputStream out = Files.newOutputStream(FILE)) { p.store(out, "Better Near settings"); }
        } catch (IOException ignored) { }
    }

    private static int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }
}
