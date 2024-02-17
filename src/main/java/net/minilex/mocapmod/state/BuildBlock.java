package net.minilex.mocapmod.state;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

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
        Minecraft.getInstance().level.playLocalSound(blockPosX, blockPosY, blockPosZ,
                Blocks.DARK_OAK_WOOD.getSoundType(getBlockState()).getPlaceSound(),
                SoundSource.BLOCKS, 1f,1f,true);
        Minecraft.getInstance().level.setBlock(getBlockPos(), getBlockState(), 19);
        getServerLevel().setBlock(getBlockPos(), getBlockState(), 19);
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
    public enum Action {
        PLACE,
        BREAK
    }
    @Override
    public String toString() {
        return "ID:" + blockId + "\nblockX:" + blockPosX + "\nblockY: " + blockPosY + "\nblockZ: " + blockPosZ + "\nAction" + action;
    }
}
