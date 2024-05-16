package net.minilex.mocapmod.vehicle.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.vehicle.custom.SimpleCarEntity;

public class SimpleCarRenderer extends LivingEntityRenderer<SimpleCarEntity, SimpleCarModel<SimpleCarEntity>> {
    public SimpleCarRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SimpleCarModel<>(pContext.bakeLayer(CarModelLayers.SIMPLE_CAR_LAYER)), 2f);
    }
    @Override
    public ResourceLocation getTextureLocation(SimpleCarEntity simpleCarEntity) {
        return new ResourceLocation(MocapMod.MODID, "textures/entity/simple_car_texture.png");
    }
    public void render(SimpleCarEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pMatrixStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        //pMatrixStack.scale(1.5f, 1.5f, 1.5f);

        super.render(pEntity, pEntityYaw, pPartialTicks, pMatrixStack, pBuffer, pPackedLight);
    }
}