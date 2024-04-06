package net.minilex.mocapmod.event;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;

public class ChatNotifyEvent {
    @Mod.EventBusSubscriber(modid = MocapMod.MODID, value = Dist.CLIENT)
    public static class ChatNotifyForgeEvent {
        @SubscribeEvent
        public static void onDeath(LivingDeathEvent event) {
            if (event.getEntity() instanceof Player victom && event.getSource().getEntity() instanceof Player hunter) {
                String msg = ChatFormatting.BLUE + victom.getName().getString() + ChatFormatting.WHITE + " was killed by " + ChatFormatting.RED + hunter.getName().getString();
                //Minecraft.getInstance().player.sendSystemMessage(Component.literal(msg));
            }
        }
    }
}
