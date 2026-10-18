package com.betternear;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class BetterNearSettingsScreen extends Screen {
    public BetterNearSettingsScreen() { super(Text.literal("Better Near Settings")); }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y = Math.max(58, this.height / 2 - 80);
        int w = 240;
        addDrawableChild(ButtonWidget.builder(toggleText("HUD", BetterNearConfig.hudEnabled), b -> {
            BetterNearClient.toggleHud(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(toggleText("Player heads", BetterNearConfig.showHeads), b -> {
            BetterNearClient.toggleHeads(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(toggleText("Distance", BetterNearConfig.showDistance), b -> {
            BetterNearClient.toggleDistance(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(toggleText("Direction arrows", BetterNearConfig.showDirections), b -> {
            BetterNearClient.toggleDirections(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Players shown: " + BetterNearConfig.maxPlayers + "  (click)"), b -> {
            BetterNearClient.cycleMaxPlayers(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 25;
        addDrawableChild(ButtonWidget.builder(Text.literal("Range: " + (int) BetterNearConfig.maxDistance + " m  (click)"), b -> {
            BetterNearClient.cycleDistance(); refresh();
        }).dimensions(cx - w / 2, y, w, 20).build());
        y += 30;
        addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close()).dimensions(cx - 60, y, 120, 20).build());
    }

    private Text toggleText(String label, boolean enabled) {
        return Text.literal(label + ": " + (enabled ? "ON" : "OFF"));
    }

    private void refresh() {
        clearChildren();
        init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, Math.max(20, height / 2 - 112), 0xFFFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer, Text.literal("Right Shift opens this menu"), width / 2, Math.max(34, height / 2 - 98), 0xFFBFC9D8);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() { return false; }
}
