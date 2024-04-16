package net.minilex.mocapmod.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.state.BuildBlock;
import net.minilex.mocapmod.state.RecordingState;

@Mod.EventBusSubscriber(modid = MocapMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BlockInteractionEvent {
    private static PlayerHandler playerHandler;
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event)
    {
        if (event.getPlayer() instanceof ServerPlayer && playerHandler == null)
            playerHandler = PlayerHandler.getInstance();
        if (playerHandler != null
                && (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE
                || playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE)) {
            int id = Block.BLOCK_STATE_REGISTRY.getId(event.getState());
            BuildBlock buildBlock = new BuildBlock(id,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    BuildBlock.Action.BREAK);
            playerHandler.getRecordThread().buildBlock = buildBlock;
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event)
    {
        if (event.getEntity() instanceof ServerPlayer && playerHandler == null)
            playerHandler = PlayerHandler.getInstance();
        if (playerHandler != null
                && (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE
                || playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE)) {
            int id = Block.BLOCK_STATE_REGISTRY.getId(event.getState());
            BuildBlock buildBlock = new BuildBlock(id,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    BuildBlock.Action.PLACE);
            playerHandler.getRecordThread().buildBlock = buildBlock;
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event)
    {
        if (event.getEntity() instanceof ServerPlayer && playerHandler == null)
            playerHandler = PlayerHandler.getInstance();
        if (playerHandler != null
                && (playerHandler.getRecordThread().getState() == RecordingState.RECORDING_SCENE
                || playerHandler.getRecordThread().getState() == RecordingState.EDIT_SCENE)) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(event.getPos());
            int id = Block.BLOCK_STATE_REGISTRY.getId(blockState);
            BuildBlock buildBlock = new BuildBlock(id,
                    event.getPos().getX(),
                    event.getPos().getY(),
                    event.getPos().getZ(),
                    BuildBlock.Action.DESTROY_PROGRESS);
            playerHandler.getRecordThread().buildBlock = buildBlock;
        }
    }
}
