package net.minilex.mocapmod.state;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.mixin.LivingEntityMixin;
import net.minilex.mocapmod.thread.FakePlayer;
import net.minilex.mocapmod.thread.Position;
import net.minilex.mocapmod.util.EntityData;

import java.io.Serializable;
import java.util.*;

public class SceneData implements Serializable {
    public Set<Position> positionSet;
    private transient Position[] position;
    public transient FakePlayer fakePlayer;
    public static transient int tickCount = 0;
    public transient int tickKnock = 0;
    public transient Vec3 knockPosition;

    public SceneData(Set<Position> positionSet) {
        this.positionSet = positionSet;
    }
    public void init() {
        tickCount = 0;
        fakePlayer = getPlayer();
        position = positionSet.toArray(new Position[positionSet.size()]);
        MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
        minecraftServer.overworld().addNewPlayer((FakePlayer) fakePlayer);

        ClientboundPlayerInfoUpdatePacket cpf = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, (FakePlayer) fakePlayer);
        Minecraft.getInstance().getConnection().handlePlayerInfoUpdate(cpf);
    }
    public void run() {
        fakePlayer.setPos(
                position[tickCount].x,
                position[tickCount].y,
                position[tickCount].z);
        fakePlayer.setXRot(position[tickCount].rotX);
        fakePlayer.setYRot(position[tickCount].rotY);
        fakePlayer.setYBodyRot(position[tickCount].yBodyRot);
        fakePlayer.setYHeadRot(position[tickCount].yHeadRot);
        setLoot(position[tickCount]);
        ((LivingEntityMixin)fakePlayer).callDetectEquipmentUpdates();
        if (position[tickCount].swinging) fakePlayer.swing(InteractionHand.MAIN_HAND);
        if (position[tickCount].isBowPulling) {
            EntityData.LIVING_ENTITY_FLAGS.set(fakePlayer, (byte)1);
        } else if (fakePlayer.getUseItemRemainingTicks() == 0
                && position[tickCount].looseArrowStrength != 0) {
            BowItem bowItem = (BowItem) Items.BOW;
            ((FakePlayer)fakePlayer).getAbilities().instabuild = true;
            bowItem.releaseUsing(new ItemStack(bowItem),
                    Minecraft.getInstance().getSingleplayerServer().overworld(), fakePlayer,
                    position[tickCount].looseArrowStrength);
            ((FakePlayer)fakePlayer).getAbilities().instabuild = false;
            EntityData.LIVING_ENTITY_FLAGS.set(fakePlayer, (byte)0);
        }
        if (position[tickCount].hurtAnim) {
            ((FakePlayer) fakePlayer).hurt(Minecraft.getInstance().level.damageSources().cactus(), 0.1f);
        }
        if (position[tickCount].tossItem != null) {
            ((FakePlayer) fakePlayer).drop(new ItemStack(Item.byId(position[tickCount].tossItem.itemID)), true);
        }
        if (position[tickCount].buildBlock != null) {
            if (position[tickCount].buildBlock.getAction() == BuildBlock.Action.PLACE)
                position[tickCount].buildBlock.placeBlock();
            else position[tickCount].buildBlock.breakBlock();
        }
        if(tickCount == position.length-5) {
            tickCount = 0;
            clearMap();
        }
    }
    public void addPosition(Position position) {
        this.positionSet.add(position);
    }
    public void editOnTick() {
        if (fakePlayer.getLastHurtByMob() != null && fakePlayer.getLastHurtByMob() instanceof Player) {
            Player player = Minecraft.getInstance().player;
            Vec3 vector = new Vec3(player.position().x - fakePlayer.position().x,
                    player.position().y - fakePlayer.position().y,
                    player.position().z - fakePlayer.position().z);
            knockPosition = fakePlayer.position();
            fakePlayer.knockback(0.3f, vector.x, vector.z);
            fakePlayer.aiStep();
            fakePlayer.setLastHurtByMob(null);
            tickKnock += 22;
            position[tickCount].x = fakePlayer.getX();
            position[tickCount].y = fakePlayer.getY();
            position[tickCount].z = fakePlayer.getZ();
            position[tickCount].hurtAnim = true;
            return;
        }
        if (tickKnock < 13 && tickKnock > 10) {
            Vec3 vector2 = new Vec3(fakePlayer.position().x - knockPosition.x,
                    fakePlayer.position().y - knockPosition.y,
                    fakePlayer.position().z - knockPosition.z);
            float radious = 0;
            double angle = 0;
            if (vector2.x < 0) {
                angle = 270 - (Math.atan(vector2.z / -vector2.x) * 180 / Math.PI);
            } else {
                angle = 90 + (Math.atan(vector2.z / vector2.x) * 180 / Math.PI);
            }
            radious = (float) angle;
            fakePlayer.setYRot(radious);
            fakePlayer.setYBodyRot(radious);
            fakePlayer.setYHeadRot(radious);
            position[tickCount].rotY = fakePlayer.getYRot();
            position[tickCount].yBodyRot = fakePlayer.yBodyRot;
            position[tickCount].yHeadRot = fakePlayer.yHeadRot;
        }
        if (tickKnock < 10 && tickKnock > 0) {
            Vec3 vec3 = fakePlayer.getLookAngle();
            fakePlayer.setPos(fakePlayer.getX() + (vec3.x/8), fakePlayer.getY(), fakePlayer.getZ() + (vec3.z/8));
        }
        if (tickKnock > 0) {
            fakePlayer.aiStep();
            position[tickCount].x = fakePlayer.getX();
            position[tickCount].y = fakePlayer.getY();
            position[tickCount].z = fakePlayer.getZ();
            tickKnock--;
            return;
        }
        this.run();
    }
    public SceneData getEditSceneData() {
        SceneData sceneData = new SceneData(new LinkedHashSet<>());
        for(Position position : position) {
            sceneData.addPosition(position);
        }
        return sceneData;
    }
    private FakePlayer getPlayer() {
        UUID id = UUID.randomUUID();
        String name = "Nick" + id;
        GameProfile profile = new GameProfile(id, name);
        Minecraft minecraft = Minecraft.getInstance();

        Property property = new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTcwNjYxNzI2NTg5NiwKICAicHJvZmlsZUlkIiA6ICIwMDM4Y2RkYTcyNjU0MjE1YjdkNWZlNmNmYWZhZmM1ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBR0E3T04iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIzYWVlOTk4MTc2Y2IxODMyYWNkMDhhN2E3NmIzOGMwZTk1ZTE0ZDU1YTUyMzU3M2IzYjIyOTIxYjUxYTg0ZSIKICAgIH0KICB9Cn0=", "IZvihWxWwpm6IiMjSi7L7LKQvxnkJXRqNGvsL5xL0UDJCerl5NMip9cHI+eUfiE3aJ31kHDv0SCkwd2AI8g3gHqeDV1okRzoS7x3uPE5q1ykMpkmZEsLVuqrdv2+JKH/NySoypFFrX+E+dV5Y7SIyNZ6h44L6ETHhJ1asI+RaeXXpxtfBqt6Am+eiiFKKN0p/ZiqUcwmQT3/D4GnKvGGZa20O8EYCihd5rAI51Kxil3E16AhC4zSfyyMSzZiElq66vaTrz0TU/c3HgAawYahv6VFWYzYVq07ajBSj2EVikbGaOQQGj8gUqFUAmRMoXyNa+0zQ52ShCHdkLdB1ruF1/yVvv/QmLltnc8q15BmLJiNPCWKt1X4nkLoyT5AOM6ostvM4uGptkoz/o1haFagJaa9q7dcYIOcEljZq3tFbH+h4He2pLVDJf5hJrkt4UOz29iPdRLvSpIPMHVN9tKLxSGXFqOVTgvjwhPc0LnWCepOlReWfHhsSbU7CLYHdAsLTVuFMc8Khx5ZKFjHZVwNjvU9iCWxOpsMck47p4SZ7DAu3V968YWrcZXTs1MBK91tdkDww7KezKaZaAVRlT0ltmiWmd8I35RxMpFK7XvuG8ASY83H+O9lHgEjlzUteB+YtyGmmOH6oQmc3hibjxYvQjUYcdsfBs/vKC8axUElUgI=");
        profile.getProperties().put("textures", property);

        fakePlayer = new FakePlayer(minecraft.getSingleplayerServer(),
                minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD),
                profile);
        Position pos = positionSet.stream().findFirst().get();
        fakePlayer.setPos(pos.x, pos.y, pos.z);
        fakePlayer.setXRot(pos.rotX);
        fakePlayer.setYRot(pos.rotY);
        fakePlayer.setYBodyRot(pos.yBodyRot);
        fakePlayer.setYHeadRot(pos.yHeadRot);
        fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.ZERO);
        setLoot(pos);
        return fakePlayer;
    }
    private void setLoot(Position pos) {
        List<EquippedItem> equippedItems = pos.getEquippedItem();
        if (equippedItems == null) return;
        equippedItems.forEach((EquippedItem eq) -> {
            ((FakePlayer) fakePlayer).setItemSlot(eq.getSlot(), new ItemStack(eq.getItem()));
        });
    }
    private void clearMap() {
        List<Position> list = new ArrayList<>(positionSet);
        Collections.reverse(list);
        list.forEach(position -> {
            if (position.buildBlock != null) {
                if (position.buildBlock.getAction() == BuildBlock.Action.BREAK) position.buildBlock.placeBlock();
                else position.buildBlock.breakBlock();
            }
        });
    }
}
