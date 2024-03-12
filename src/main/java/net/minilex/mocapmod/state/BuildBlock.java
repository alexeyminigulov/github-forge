package net.minilex.mocapmod.state;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.handler.PlayerHandler;

import java.io.Serializable;
import java.util.Set;

public class BuildBlock implements Serializable {
    private int blockId;
    private int blockPosX;
    private int blockPosY;
    private int blockPosZ;
    private Action action;

    public BuildBlock(int id, int x, int y, int z, BuildBlock.Action act) {
        blockId = id;
        blockPosX = x;
        blockPosY = y;
        blockPosZ = z;
        action = act;
    }
    public BlockState getBlockState() {
        return Block.stateById(blockId);
    }
    public BlockPos getBlockPos() {
        return new BlockPos(blockPosX, blockPosY, blockPosZ);
    }
    public Action getAction() {
        return action;
    }
    public void placeBlock() {
        ItemStack itemStack = new ItemStack(getBlockState().getBlock().asItem());
        BlockHitResult blockHitResult = BlockHitResult.miss(new Vec3(blockPosX, blockPosY, blockPosZ), Direction.DOWN, getBlockPos());
        UseOnContext context = new UseOnContext(getPlayer(), InteractionHand.MAIN_HAND, blockHitResult);
        itemStack.useOn(context);
        if (getPlayerHandler().getRecordThread().getState() == RecordingState.PLAYING) {
            getPlayer().setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(getBlockState().getBlock().asItem()));
        }
    }
    public void breakBlock() {
        Minecraft.getInstance().level.playLocalSound(blockPosX, blockPosY, blockPosZ,
                Blocks.DARK_OAK_WOOD.getSoundType(getBlockState()).getBreakSound(),
                SoundSource.BLOCKS, 1f,1f,true);
        Minecraft.getInstance().level.destroyBlock(getBlockPos(), true);
        getServerLevel().destroyBlock(getBlockPos(), false);
    }
    /*public int getId() {
        return Block.BLOCK_STATE_REGISTRY.getId(blockState);
    }*/
    private ServerLevel getServerLevel() {
        return Minecraft.getInstance().getSingleplayerServer().overworld();
    }
    private Player getPlayer() {
        Player player = ((Player) PlayerHandler.getInstance().getRecordThread().fakePlayer);
        return player != null ? player : Minecraft.getInstance().player;
    }
    private PlayerHandler getPlayerHandler() {
        return PlayerHandler.getInstance();
    }
    public enum Action {
        PLACE,
        BREAK
    }
    @Override
    public String toString() {
        return "ID:" + blockId + "\nblockX:" + blockPosX + "\nblockY: " + blockPosY + "\nblockZ: " + blockPosZ + "\nAction" + action;
    }
}
