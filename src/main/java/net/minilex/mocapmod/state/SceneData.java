package net.minilex.mocapmod.state;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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
import net.minilex.mocapmod.util.CommandUtil;
import net.minilex.mocapmod.util.EntityData;

import java.io.Serializable;
import java.util.*;

public class SceneData implements Serializable {
    public Set<Position> positionSet;
    private transient Position[] position;
    public transient FakePlayer fakePlayer;
    private transient CommandUtil.ScriptObject scriptObject;
    private transient BlockPos blockPos;
    private transient int blockDamage;
    public static transient int tickCount = 0;
    public transient int tickKnock = 0;
    public transient Vec3 knockPosition;
    public transient boolean speak = false;
    private transient int deathTime = 0;

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
        clearMap();
    }
    public void run() {
        if (deathTime > 1) {
            deathTime--;
            if (deathTime == 5) {
                fakePlayer.remove(Entity.RemovalReason.KILLED);
                this.particleDeath((Player) fakePlayer);
            }
        }
        if (deathTime > 1 || deathTime == 1) return;

        if (deathTime == 0) this.death();
        if (deathTime == 0 && !position[tickCount].ignoreAttack) this.editOnDamage();
        if (deathTime == 0 && position[tickCount].ignoreAttack) this.knockBack();
        if (deathTime == 0 && tickKnock == 0) this.action();
        if (scriptObject.aiStep) fakePlayer.aiStep();
    }
    private void death() {
        if (fakePlayer.getLastHurtByMob() != null && fakePlayer.getLastHurtByMob() instanceof Player player) {
            if (fakePlayer.getHealth() < 2f) {
                deathTime = 20;
                Vec3 vector = new Vec3(player.position().x - fakePlayer.position().x,
                        player.position().y - fakePlayer.position().y,
                        player.position().z - fakePlayer.position().z);
                DeathState deathState = new DeathState(2.1f, vector.x, vector.z);
                fakePlayer.knockback(deathState.force, deathState.x, deathState.z);
                fakePlayer.aiStep();
                ((FakePlayer) fakePlayer).kill();
                ((FakePlayer) fakePlayer).setLastHurtByMob(null);
                position[tickCount].dead = deathState;
            }
        }
    }
    private void action() {
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
        if (this.speak != position[tickCount].speakerIcon) {
            this.speak = position[tickCount].speakerIcon;
        }
        if (position[tickCount].hurtAnim) {
            ((FakePlayer) fakePlayer).hurt(Minecraft.getInstance().level.damageSources().cactus(), 0.1f);
        }
        if (position[tickCount].tossItem != null) {
            ((FakePlayer) fakePlayer).drop(new ItemStack(Item.byId(position[tickCount].tossItem.itemID)), true);
        }
        if (position[tickCount].buildBlock != null) {
            if (position[tickCount].buildBlock.getAction() == BuildBlock.Action.PLACE) {
                position[tickCount].buildBlock.placeBlock(fakePlayer);
            }
            else if (position[tickCount].buildBlock.getAction() == BuildBlock.Action.BREAK) {
                position[tickCount].buildBlock.breakBlock();
            } else if (position[tickCount].buildBlock.getAction() == BuildBlock.Action.DESTROY_PROGRESS) {
                if (blockPos != null && position[tickCount].buildBlock.isEqualTo(blockPos)) {
                    this.blockDamage++;
                    position[tickCount].buildBlock.destroyBlock(blockDamage, fakePlayer);
                } else {
                    if (blockPos != null) Minecraft.getInstance().getSingleplayerServer().overworld().destroyBlockProgress(fakePlayer.getId(), blockPos, 0);
                    blockPos = position[tickCount].buildBlock.getBlockPos();
                    this.blockDamage = 0;
                }
            }
        }
        if (position[tickCount].dead != null) {
            deathTime = 20;
            DeathState deathState = position[tickCount].dead;
            fakePlayer.knockback(deathState.force, deathState.x, deathState.z);
            fakePlayer.aiStep();
            ((FakePlayer) fakePlayer).kill();
        }
        if(tickCount == position.length-5) {
            tickCount = 0;
            clearMap();
        }
    }
    public void addPosition(Position position) {
        this.positionSet.add(position);
    }
    private void knockBack() {
        if (fakePlayer.getLastHurtByMob() != null && fakePlayer.getLastHurtByMob() instanceof Player) {
            Player player = (Player)fakePlayer.getLastHurtByMob();
            Vec3 vector = new Vec3(player.position().x - fakePlayer.position().x,
                    player.position().y - fakePlayer.position().y,
                    player.position().z - fakePlayer.position().z);
            fakePlayer.knockback(0.3f, vector.x, vector.z);
            fakePlayer.aiStep();
            fakePlayer.setLastHurtByMob(null);
            position[tickCount].hurtAnim = true;
            tickKnock += 5;
            return;
        }
        if (tickKnock > 0) {
            fakePlayer.aiStep();
            position[tickCount].x = fakePlayer.getX();
            position[tickCount].y = fakePlayer.getY();
            position[tickCount].z = fakePlayer.getZ();
            tickKnock--;
            if (tickKnock == 0) {
                tickCount += 6;
            }
        }
    }
    private void editOnDamage() {
        if (fakePlayer.getLastHurtByMob() != null && fakePlayer.getLastHurtByMob() instanceof Player) {
            Player player = (Player)fakePlayer.getLastHurtByMob();
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
    }
    public SceneData getEditSceneData() {
        SceneData sceneData = new SceneData(new LinkedHashSet<>());
        for(Position position : position) {
            sceneData.addPosition(position);
        }
        return sceneData;
    }
    private FakePlayer getPlayer() {
        scriptObject = CommandUtil.getInstance().getNextScriptElement();
        UUID id = UUID.randomUUID();
        String name = scriptObject.nameColor + scriptObject.name;
        GameProfile profile = new GameProfile(id, name);
        Minecraft minecraft = Minecraft.getInstance();

        Property property = new Property("default_textures", scriptObject.srcTexture, scriptObject.sex);
        profile.getProperties().put("default_textures", property);

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
        fakePlayer.setHealth(scriptObject.health);
        return fakePlayer;
    }
    private void particleDeath(Player player) {
        for(int i = 0; i < 5; ++i) {
            double d0 = Math.random() / 10000;
            double d1 = Math.random() / 10000;
            d0 = Math.random() > 0.5d ? d0 : d0 * (-1);
            d1 = Math.random() > 0.5d ? d1 : d1 * (-1);
            Minecraft.getInstance().level.addParticle(ParticleTypes.CLOUD,
                    player.getRandomX(1.0), player.getRandomY() + 0.6f, player.getRandomZ(1.0),
                    d0, 0.005d, d1);
        }
    }
    private void setLoot(Position pos) {
        List<EquippedItem> equippedItems = pos.getEquippedItem();
        if (equippedItems == null) return;
        equippedItems.forEach((EquippedItem eq) -> {
            ((FakePlayer) fakePlayer).setItemSlot(eq.getSlot(), new ItemStack(eq.getItem()));
        });
    }
    public void clearMap() {
        List<Position> list = new ArrayList<>(positionSet);
        Collections.reverse(list);
        list.forEach(position -> {
            if (position.buildBlock != null) {
                if (position.buildBlock.getAction() == BuildBlock.Action.BREAK) {
                    position.buildBlock.placeBlockSimple();
                }
                else if (position.buildBlock.getAction() == BuildBlock.Action.PLACE) {
                    position.buildBlock.breakBlockSimple();
                }
            }
        });
    }
}
