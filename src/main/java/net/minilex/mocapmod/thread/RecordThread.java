package net.minilex.mocapmod.thread;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.state.*;
import net.minilex.mocapmod.util.Data;
import net.minilex.mocapmod.util.SceneUtil;

public class RecordThread implements Runnable {

    private static RecordThread instance;
    private LocalPlayer player;
    private RecordingState state;
    private File dir;
    private FileOutputStream file;
    private String fileName;
    private ObjectOutputStream o;
    public LivingEntity fakePlayer;
    public BuildBlock buildBlock;
    private ActorType playerType;
    public Data data = new Data();
    public Set<Position> result;
    private boolean isLootSet = true;
    private StatusInventory statusInventory;
    public int positionIndex = 0;

    public RecordThread(LocalPlayer _player, String capname) {
        fileName = capname;
        player = _player;
        state = RecordingState.EMPTY;
        instance = this;
        playerType = ActorType.VILLAGER;
    }
    public Set<Position> readFile(String capname) {
        dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT) + "/" + "mocaps");
        try {
            FileInputStream fi = new FileInputStream(dir.getAbsolutePath() + "/" + capname
                    + ".mocap");
            ObjectInputStream oi = new ObjectInputStream(fi);

            result = new LinkedHashSet<>();
            try {
                for (;;) {
                    result.add((Position) oi.readObject());
                }
            } catch (EOFException e) {
                // End of stream
            }
            oi.close();
            fi.close();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
        return result;
    }
    private void trackAndWriteMovement() throws IOException {
        Vec3 entityPos = player.position();
        Vec2 entityRot = player.getRotationVector();
        Position pos = new Position(entityPos.x, entityPos.y, entityPos.z,
                entityRot.x, entityRot.y, player.getVisualRotationYInDegrees(), player.yHeadRot);
        if (buildBlock != null) {
            pos.buildBlock = buildBlock;
            buildBlock = null;
        }
        if (isLootSet) {
            player.getInventory().armor.forEach((ItemStack is) -> {
                int index = player.getInventory().armor.indexOf(is);
                EquipmentSlot slot = switch (index) {
                    case 0 -> EquipmentSlot.FEET;
                    case 1 -> EquipmentSlot.LEGS;
                    case 2 -> EquipmentSlot.CHEST;
                    case 3 -> EquipmentSlot.HEAD;
                    default -> null;
                };
                EquippedItem item = new EquippedItem(Item.getId(is.getItem()), slot.getFilterFlag());
                pos.addEquippedItem(item);
            });

            Item offhand = ((ItemStack)player.getInventory().offhand.toArray()[0]).getItem();
            pos.addEquippedItem(new EquippedItem(Item.getId(offhand), EquipmentSlot.OFFHAND.getFilterFlag()));

            try {
                Field nameField = player.getClass().getSuperclass().getSuperclass().getDeclaredField("lastItemInMainHand");
                nameField.setAccessible(true);
                Item itemMainHand = ((ItemStack)nameField.get(player)).getItem();
                pos.addEquippedItem(new EquippedItem(Item.getId(itemMainHand), EquipmentSlot.MAINHAND.getFilterFlag()));
            } catch (Exception e) {

            }
            statusInventory = new StatusInventory(player.getMainHandItem(), ((ItemStack)player.getInventory().offhand.toArray()[0]), player.getInventory().armor);

            isLootSet = false;
        }
        statusInventory.tickUpdate(player.getMainHandItem(), ((ItemStack)player.getInventory().offhand.toArray()[0]), player.getInventory().armor);
        EquippedItem equippedItem = statusInventory.getUpdatedItem();
        if (equippedItem != null) {
            pos.addEquippedItem(equippedItem);
        }
        if (player.swinging && player.swingTime == 0) pos.swinging = true;
        if (Item.getId(Items.BOW) == Item.getId(player.getUseItem().getItem()) && player.isUsingItem()) {
            pos.isBowPulling = true;
        }
        if (!this.data.isArrowLooseEmpty()) {
            pos.looseArrowStrength = data.getArrowLooseEvent().getCharge();
        }
        if (!this.data.isTossItemEmpty()) {
            pos.tossItem = new TossItem(data.getTossItemEvent().getEntity());
        }

        o.writeObject(pos);
    }
    private void initFile(String capname) {
        try {
            dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT)
                    + "/" + "mocaps");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new FileOutputStream(dir.getAbsolutePath() + "/" + capname
                    + ".mocap");
            o = new ObjectOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void stop() {
        isLootSet = true;
        try {
            file.close();
            o.close();
        } catch (IOException e) {
            System.out.println("Can't close file");
        }
    }
    private void read() {
        result = readFile(this.fileName);

        UUID id = UUID.randomUUID();
        GameProfile profile = new GameProfile(id, "Sasha");
        Minecraft minecraft = Minecraft.getInstance();

        Property property = new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTcwNjYxNzI2NTg5NiwKICAicHJvZmlsZUlkIiA6ICIwMDM4Y2RkYTcyNjU0MjE1YjdkNWZlNmNmYWZhZmM1ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBR0E3T04iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjIzYWVlOTk4MTc2Y2IxODMyYWNkMDhhN2E3NmIzOGMwZTk1ZTE0ZDU1YTUyMzU3M2IzYjIyOTIxYjUxYTg0ZSIKICAgIH0KICB9Cn0=", "IZvihWxWwpm6IiMjSi7L7LKQvxnkJXRqNGvsL5xL0UDJCerl5NMip9cHI+eUfiE3aJ31kHDv0SCkwd2AI8g3gHqeDV1okRzoS7x3uPE5q1ykMpkmZEsLVuqrdv2+JKH/NySoypFFrX+E+dV5Y7SIyNZ6h44L6ETHhJ1asI+RaeXXpxtfBqt6Am+eiiFKKN0p/ZiqUcwmQT3/D4GnKvGGZa20O8EYCihd5rAI51Kxil3E16AhC4zSfyyMSzZiElq66vaTrz0TU/c3HgAawYahv6VFWYzYVq07ajBSj2EVikbGaOQQGj8gUqFUAmRMoXyNa+0zQ52ShCHdkLdB1ruF1/yVvv/QmLltnc8q15BmLJiNPCWKt1X4nkLoyT5AOM6ostvM4uGptkoz/o1haFagJaa9q7dcYIOcEljZq3tFbH+h4He2pLVDJf5hJrkt4UOz29iPdRLvSpIPMHVN9tKLxSGXFqOVTgvjwhPc0LnWCepOlReWfHhsSbU7CLYHdAsLTVuFMc8Khx5ZKFjHZVwNjvU9iCWxOpsMck47p4SZ7DAu3V968YWrcZXTs1MBK91tdkDww7KezKaZaAVRlT0ltmiWmd8I35RxMpFK7XvuG8ASY83H+O9lHgEjlzUteB+YtyGmmOH6oQmc3hibjxYvQjUYcdsfBs/vKC8axUElUgI=");
        profile.getProperties().put("textures", property);

        fakePlayer = new FakePlayer(minecraft.getSingleplayerServer(),
                minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD),
                profile);

        MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
        Position pos = result.stream().findFirst().get();
        fakePlayer.setPos(pos.x, pos.y, pos.z);
        fakePlayer.setXRot(pos.rotX);
        fakePlayer.setYRot(pos.rotY);
        fakePlayer.setYBodyRot(pos.yBodyRot);
        fakePlayer.setYHeadRot(pos.yHeadRot);
        fakePlayer.lookAt(EntityAnchorArgument.Anchor.EYES, Vec3.ZERO);
        setLoot(pos);
        minecraftServer.overworld().addNewPlayer((FakePlayer) fakePlayer);

        ClientboundPlayerInfoUpdatePacket cpf = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, (FakePlayer) fakePlayer);
        minecraft.getConnection().handlePlayerInfoUpdate(cpf);
    }
    public void setLoot(Position pos) {
        List<EquippedItem> equippedItems = pos.getEquippedItem();
        if (equippedItems == null) return;
        equippedItems.forEach((EquippedItem eq) -> {
            ((FakePlayer) fakePlayer).setItemSlot(eq.getSlot(), new ItemStack(eq.getItem()));
        });
    }
    public void clearMap() {
        if (result == null) result = readFile(this.fileName);
        List<Position> list = new ArrayList<>(result);
        Collections.reverse(list);
        list.forEach(position -> {
            if (position.buildBlock != null) {
                if (position.buildBlock.getAction() == BuildBlock.Action.BREAK) position.buildBlock.placeBlock((Player) fakePlayer);
                else position.buildBlock.breakBlock();
            }
        });
    }
    public static RecordThread getInstance() {
        if (instance == null) {
            RecordThread recordThread = new RecordThread(Minecraft.getInstance().player, "loh");
        }
        return instance;
    }
    public void changeActorType(ActorType type) {
        this.playerType = type;
    }
    public RecordingState getState() {
        return state;
    }
    public void run() {
        try {
            if (state == RecordingState.RECORDING) {
                trackAndWriteMovement();

                if (player.isDeadOrDying()) {
                    state = RecordingState.STOP;
                    this.fakePlayer.remove(Entity.RemovalReason.KILLED);
                    MocapMod.getInstance().broadcastMsg("Stopped recording "
                            + player.getDisplayName() + ". RIP.");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    public void changedActor() {
        state = RecordingState.PLAYING;
        result = readFile(this.fileName);

        Minecraft minecraft = Minecraft.getInstance();
        if (playerType == ActorType.VILLAGER) fakePlayer = new Villager(EntityType.VILLAGER, minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD));
        if (playerType == ActorType.ZOMBIE) fakePlayer = new Zombie(EntityType.ZOMBIE, minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD));
        if (playerType == ActorType.FOX) fakePlayer = new Fox(EntityType.FOX, minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD));
        if (playerType == ActorType.RABBIT) {
            fakePlayer = new Rabbit(EntityType.RABBIT, minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD));
            ((Rabbit) fakePlayer).startJumping();
        }

        MinecraftServer minecraftServer = minecraft.getSingleplayerServer();
        Position pos = result.stream().findFirst().get();
        fakePlayer.setPos(pos.x, pos.y, pos.z);
        fakePlayer.setXRot(pos.rotX);
        fakePlayer.setYRot(pos.rotY);
        fakePlayer.setYBodyRot(pos.yBodyRot);
        fakePlayer.setYHeadRot(pos.yHeadRot);
        minecraftServer.overworld().addFreshEntity(fakePlayer);
    }
    public void changeState(RecordingState newState) {
        if (state == RecordingState.EMPTY && newState == RecordingState.RECORDING) {
            state = newState;
            initFile(fileName);
            System.out.println("State is RECORDING");
        } else if (state == RecordingState.RECORDING && newState == RecordingState.STOP) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("The End!!!  "));
            state = newState;
            this.stop();
            this.clearMap();
            System.out.println("State is STOP");
        } else if (state == RecordingState.STOP && newState == RecordingState.PLAYING) {
            state = newState;
            this.read();
            System.out.println("State is PLAYING");
        } else if (state == RecordingState.PLAYING && newState == RecordingState.STOP) {
            state = newState;
            this.fakePlayer.remove(Entity.RemovalReason.KILLED);
            this.positionIndex = 0;
            this.clearMap();
            System.out.println("State is STOP and Remove fake");
        } else if (newState == RecordingState.EMPTY) {
            if (state == RecordingState.PLAYING) {
                this.fakePlayer.remove(Entity.RemovalReason.KILLED);
            } else if (state == RecordingState.RECORDING) {
                this.stop();
            }
            state = newState;
            this.positionIndex = 0;
        } else if ((state == RecordingState.STOP_SCENE || state == RecordingState.EMPTY || state == RecordingState.PLAYING_SCENE) && newState == RecordingState.PLAYING_SCENE) {
            boolean result = SceneUtil.getInstance().initScene();
            if (result) state = newState;
        }  else if ((state == RecordingState.STOP || state == RecordingState.EMPTY || state == RecordingState.STOP_SCENE) && newState == RecordingState.RECORDING_SCENE) {
            SceneUtil.getInstance().startRecord();
            state = newState;
        } else if (state == RecordingState.RECORDING_SCENE || state == RecordingState.EDIT_SCENE || state == RecordingState.PLAYING_SCENE && newState == RecordingState.STOP_SCENE) {
            if (state == RecordingState.RECORDING_SCENE) SceneUtil.getInstance().saveScene();
            if (state == RecordingState.EDIT_SCENE) SceneUtil.getInstance().saveSceneEdit();
            if (state == RecordingState.PLAYING_SCENE) SceneUtil.getInstance().stopScene();
            state = newState;
        } else if ((state == RecordingState.STOP_SCENE || state == RecordingState.EMPTY) && newState == RecordingState.EDIT_SCENE) {
            SceneUtil.getInstance().editScene();
            state = newState;
        }
    }
}
