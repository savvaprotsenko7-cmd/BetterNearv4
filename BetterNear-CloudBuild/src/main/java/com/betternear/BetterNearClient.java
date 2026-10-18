package com.betternear;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudElementRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BetterNearClient implements ClientModInitializer {
    public static final String MOD_ID = "betternear";
    private static final Identifier HUD_ID = Identifier.of(MOD_ID, "hud");
    private static boolean wasRightShiftDown = false;
    private static boolean hudEnabled = true;
    private static boolean showHeads = true;
    private static boolean showDistance = true;
    private static boolean showDirections = true;
    private static int maxPlayers = 5;
    private static double maxDistance = 128.0;
    private static int panelColor = 0xCC171B22;

    @Override
    public void onInitializeClient() {
        BetterNearConfig.load();
        applyConfig();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !wasRightShiftDown && client.currentScreen == null) {
                client.setScreen(new BetterNearSettingsScreen());
            }
            wasRightShiftDown = down;
        });
        HudElementRegistry.addLast(HUD_ID, (context, tickCounter) -> renderHud(MinecraftClient.getInstance(), context));
    }

    public static void applyConfig() {
        hudEnabled = BetterNearConfig.hudEnabled;
        showHeads = BetterNearConfig.showHeads;
        showDistance = BetterNearConfig.showDistance;
        showDirections = BetterNearConfig.showDirections;
        maxPlayers = BetterNearConfig.maxPlayers;
        maxDistance = BetterNearConfig.maxDistance;
        panelColor = BetterNearConfig.panelColor;
    }

    public static boolean hudEnabled() { return hudEnabled; }
    public static void toggleHud() { BetterNearConfig.hudEnabled = !BetterNearConfig.hudEnabled; BetterNearConfig.save(); applyConfig(); }
    public static void toggleHeads() { BetterNearConfig.showHeads = !BetterNearConfig.showHeads; BetterNearConfig.save(); applyConfig(); }
    public static void toggleDistance() { BetterNearConfig.showDistance = !BetterNearConfig.showDistance; BetterNearConfig.save(); applyConfig(); }
    public static void toggleDirections() { BetterNearConfig.showDirections = !BetterNearConfig.showDirections; BetterNearConfig.save(); applyConfig(); }
    public static void cycleMaxPlayers() { BetterNearConfig.maxPlayers = BetterNearConfig.maxPlayers >= 10 ? 3 : BetterNearConfig.maxPlayers + 1; BetterNearConfig.save(); applyConfig(); }
    public static void cycleDistance() { BetterNearConfig.maxDistance = BetterNearConfig.maxDistance >= 256 ? 32 : BetterNearConfig.maxDistance * 2; BetterNearConfig.save(); applyConfig(); }

    private static void renderHud(MinecraftClient client, DrawContext ctx) {
        if (!hudEnabled || client.player == null || client.world == null || client.options.hudHidden) return;
        List<PlayerEntity> nearby = new ArrayList<>();
        for (PlayerEntity p : client.world.getPlayers()) {
            if (p == client.player || p.isRemoved()) continue;
            double d = client.player.distanceTo(p);
            if (d <= maxDistance) nearby.add(p);
        }
        nearby.sort(Comparator.comparingDouble(client.player::distanceTo));
        if (nearby.isEmpty()) return;

        int x = 8, y = 8, rowH = showHeads ? 30 : 24;
        int width = 190;
        int count = Math.min(maxPlayers, nearby.size());
        int height = 8 + count * rowH;
        ctx.fill(x, y, x + width, y + height, panelColor);
        ctx.fill(x, y, x + width, y + 2, 0xFF80A2FF);
        for (int i = 0; i < count; i++) {
            PlayerEntity p = nearby.get(i);
            int ry = y + 5 + i * rowH;
            double dist = client.player.distanceTo(p);
            PlayerListEntry entry = client.getNetworkHandler() == null ? null : client.getNetworkHandler().getPlayerListEntry(p.getUuid());
            if (showHeads && entry != null) {
                Identifier skin = entry.getSkinTextures().texture();
                ctx.drawTexture(RenderLayer::getGuiTextured, skin, x + 6, ry + 2, 8, 8, 20, 20, 64, 64);
                ctx.drawTexture(RenderLayer::getGuiTextured, skin, x + 6, ry + 2, 40, 8, 20, 20, 64, 64);
            }
            int tx = x + (showHeads ? 32 : 8);
            String name = p.getName().getString();
            if (name.length() > 16) name = name.substring(0, 16);
            ctx.drawText(client.textRenderer, name, tx, ry + 2, 0xFFFFFFFF, true);
            String details = showDistance ? String.format(java.util.Locale.ROOT, "%.1f m", dist) : "";
            if (showDirections) details += (details.isEmpty() ? "" : "  ") + directionTo(client.player, p);
            if (!details.isEmpty()) ctx.drawText(client.textRenderer, details, tx, ry + 14, 0xFFBFC9D8, false);
            if (i < count - 1) ctx.fill(x + 6, ry + rowH - 1, x + width - 6, ry + rowH, 0x553F3F3F);
        }
    }

    private static String directionTo(PlayerEntity viewer, PlayerEntity target) {
        double dx = target.getX() - viewer.getX();
        double dz = target.getZ() - viewer.getZ();
        double targetAngle = Math.toDegrees(Math.atan2(-dx, dz));
        double relative = wrapDegrees(targetAngle - viewer.getYaw());
        if (relative >= -22.5 && relative < 22.5) return "↑";
        if (relative >= 22.5 && relative < 67.5) return "↖";
        if (relative >= 67.5 && relative < 112.5) return "←";
        if (relative >= 112.5 && relative < 157.5) return "↙";
        if (relative >= 157.5 || relative < -157.5) return "↓";
        if (relative >= -157.5 && relative < -112.5) return "↘";
        if (relative >= -112.5 && relative < -67.5) return "→";
        return "↗";
    }

    private static double wrapDegrees(double angle) {
        angle %= 360.0;
        if (angle >= 180.0) angle -= 360.0;
        if (angle < -180.0) angle += 360.0;
        return angle;
    }
}
