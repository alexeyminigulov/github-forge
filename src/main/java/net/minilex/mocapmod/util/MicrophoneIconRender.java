package net.minilex.mocapmod.util;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minilex.mocapmod.MocapMod;

public class MicrophoneIconRender {
    private static final ResourceLocation MICROPHONE_ICON = new ResourceLocation(MocapMod.MODID, "textures/particle/microphone.png");
    private final Minecraft minecraft;
    private static MicrophoneIconRender instance;
    private MicrophoneIconRender() {
        minecraft = Minecraft.getInstance();
    }
    public void onRenderHUD(PoseStack matrixStack) {
        renderIcon(matrixStack, MICROPHONE_ICON);
    }
    private void renderIcon(PoseStack matrixStack, ResourceLocation texture) {
        matrixStack.pushPose();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, texture);
        int posX = 16;
        int posY = -16;
        if (posX < 0) {
            matrixStack.translate(minecraft.getWindow().getGuiScaledWidth(), 0D, 0D);
        }
        if (posY < 0) {
            matrixStack.translate(0D, minecraft.getWindow().getGuiScaledHeight(), 0D);
        }
        matrixStack.translate(posX, posY, 0D);
        float scale = 1f;
        matrixStack.scale(scale, scale, 1F);

        Screen.blit(matrixStack, posX < 0 ? -16 : 0, posY < 0 ? -16 : 0, 0, 0, 16, 16, 16, 16);
        matrixStack.popPose();
    }
    public static synchronized MicrophoneIconRender instance() {
        if (instance == null) {
            instance = new MicrophoneIconRender();
        }
        return instance;
    }
}