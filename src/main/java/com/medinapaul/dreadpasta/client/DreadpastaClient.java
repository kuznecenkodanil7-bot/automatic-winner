package com.medinapaul.dreadpasta.client;

import com.medinapaul.dreadpasta.DreadpastaMod;
import com.medinapaul.dreadpasta.net.JumpscarePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.text.Text;

public class DreadpastaClient implements ClientModInitializer {
    private static int jumpscareTicks = 0;
    private static int pulse = 0;

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(DreadpastaMod.STALKER, ctx -> (EntityRenderer) new StalkerRenderer(ctx));

        ClientPlayNetworking.registerGlobalReceiver(JumpscarePayload.ID, (payload, context) ->
                context.client().execute(() -> trigger(payload.ticks()))
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            pulse++;
            if (jumpscareTicks > 0) jumpscareTicks--;
        });

        HudRenderCallback.EVENT.register(DreadpastaClient::renderJumpscare);
    }

    private static void trigger(int ticks) {
        jumpscareTicks = Math.max(jumpscareTicks, ticks);
    }

    private static void renderJumpscare(DrawContext context, Object tickCounter) {
        if (jumpscareTicks <= 0) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int w = client.getWindow().getScaledWidth();
        int h = client.getWindow().getScaledHeight();
        TextRenderer tr = client.textRenderer;

        int flicker = (pulse / 2) % 2;
        int bg = flicker == 0 ? 0xEE000000 : 0xEE260000;
        context.fill(0, 0, w, h, bg);

        int eyeW = Math.max(18, w / 9);
        int eyeH = Math.max(8, h / 28);
        int y = h / 2 - h / 9;
        context.fill(w / 2 - eyeW - 28, y, w / 2 - 28, y + eyeH, 0xFFFFFFFF);
        context.fill(w / 2 + 28, y, w / 2 + eyeW + 28, y + eyeH, 0xFFFFFFFF);
        context.fill(w / 2 - eyeW + 6 - 28, y + 2, w / 2 - eyeW + 13 - 28, y + eyeH - 2, 0xFF000000);
        context.fill(w / 2 + 28 + eyeW - 13, y + 2, w / 2 + 28 + eyeW - 6, y + eyeH - 2, 0xFF000000);

        if (pulse % 5 < 3) {
            context.drawCenteredTextWithShadow(tr, Text.literal("НЕ СМОТРИ"), w / 2, h / 2 + 32, 0xFFFF3333);
        }
        context.drawCenteredTextWithShadow(tr, Text.literal("§kXXXXXXXXXXXX"), w / 2, h / 2 + 54, 0xFF777777);
    }
}
