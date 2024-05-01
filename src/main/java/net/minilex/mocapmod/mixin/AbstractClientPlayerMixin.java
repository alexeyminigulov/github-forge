package net.minilex.mocapmod.mixin;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.thread.FakePlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(AbstractClientPlayer.class)
abstract public class AbstractClientPlayerMixin
{
    @Shadow
    protected abstract @Nullable PlayerInfo getPlayerInfo();

    @Inject(method = "getSkinTextureLocation", at = @At(value = "HEAD"), cancellable = true)
    private void atGetSkinTextureLocation(CallbackInfoReturnable<ResourceLocation> cir)
    {
        PlayerInfo playerInfo = getPlayerInfo();
        UUID id = playerInfo.getProfile().getId();
        Player player = Minecraft.getInstance().getSingleplayerServer().overworld().getPlayerByUUID(id);
        if (!(player instanceof FakePlayer)) return;
        Property property = playerInfo.getProfile().getProperties().get("default_textures").iterator().next();
        String src = "textures/skins/" + property.getSignature() + "/" + property.getValue() + ".png";
        ResourceLocation skinLocation = new ResourceLocation(MocapMod.MODID, src);
        cir.setReturnValue(skinLocation);
        cir.cancel();
    }
}