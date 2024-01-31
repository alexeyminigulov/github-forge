package net.minilex.mocapmod.thread;

import java.io.*;
import java.util.*;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.state.RecordingState;

public class RecordThread implements Runnable {

    private static RecordThread instance;
    private LocalPlayer player;
    private RecordingState state;
    //private Boolean lastTickSwipe = false;
    private File dir;
    private FileOutputStream file;
    private String fileName;
    private ObjectOutputStream o;
    //private int itemsEquipped[] = new int[5];
    //private List<MocapAction> eventList;
    public LivingEntity fakePlayer;
    public Set<Position> result;
    public int positionIndex = 0;

    public RecordThread(LocalPlayer _player, String capname) {
        fileName = capname;
        player = _player;
        state = RecordingState.IDLE;
        //eventList = MocapMod.getInstance().getActionListForPlayer(player);
        instance = this;
    }

    public void run() {
        try {
            if (state == RecordingState.RECORDING) {
                trackAndWriteMovement();

                if (player.isDeadOrDying()) {
                    state = RecordingState.STOP;
                    MocapMod.getInstance().recordThreads.remove(player);
                    MocapMod.getInstance().broadcastMsg("Stopped recording "
                            + player.getDisplayName() + ". RIP.");
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void trackAndWriteMovement() throws IOException {
        Vec3 entityPos = player.position();
        Vec2 entityRot = player.getRotationVector();
        Position pos = new Position(entityPos.x, entityPos.y, entityPos.z, entityRot.x, entityRot.y);
        System.out.println("Recording postion is " + pos);
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
    public static RecordThread getInstance() {
        if (instance == null) {
            RecordThread recordThread = new RecordThread(Minecraft.getInstance().player, "loh");
        }
        return instance;
    }
    public RecordingState getState() {
        return state;
    }

    private void stop() {
        try {
            //o.writeObject(resultForFile);
            file.close();
            o.close();
        } catch (IOException e) {
            System.out.println("Can't close file");
        }
    }

    private void read() {
        try {
            FileInputStream fi = new FileInputStream(dir.getAbsolutePath() + "/" + "loh"
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
            /*List<Position> resultList = result.stream()
                    .sorted(Comparator.comparing(Position::getRowId))
                    .collect(Collectors.toList());*/

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
            fakePlayer.setYHeadRot(pos.rotY);
            minecraftServer.overworld().addNewPlayer((FakePlayer) fakePlayer);

            ClientboundPlayerInfoUpdatePacket cpf = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, (FakePlayer) fakePlayer);
            minecraft.getConnection().handlePlayerInfoUpdate(cpf);

            result.forEach(position -> System.out.println(position));

            oi.close();
            fi.close();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
    }
    public void changedActor() {
        state = RecordingState.PLAYING;
        try {
            FileInputStream fi = new FileInputStream(dir.getAbsolutePath() + "/" + "loh"
                    + ".mocap");
            ObjectInputStream oi = new ObjectInputStream(fi);

            result = new LinkedHashSet<>();
            try {
                for (;;) {
                    result.add((Position) oi.readObject());
                }
            } catch (EOFException e) {
            }

            Minecraft minecraft = Minecraft.getInstance();
            fakePlayer = new Villager(EntityType.VILLAGER, minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD));

            MinecraftServer minecraftServer = minecraft.getSingleplayerServer();
            Position pos = result.stream().findFirst().get();
            fakePlayer.setPos(pos.x, pos.y, pos.z);
            fakePlayer.setXRot(pos.rotX);
            fakePlayer.setYRot(pos.rotY);
            fakePlayer.setYHeadRot(pos.rotY);
            minecraftServer.overworld().addFreshEntity(fakePlayer);

            oi.close();
            fi.close();
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
        }
    }
    public void changeState(RecordingState newState) {
        if (state == RecordingState.IDLE && newState == RecordingState.RECORDING) {
            state = newState;
            initFile(fileName);
            System.out.println("State is RECORDING");
        } else if (state == RecordingState.RECORDING && newState == RecordingState.STOP) {
            Minecraft.getInstance().player.sendSystemMessage(Component.literal("The End!!!  "));
            state = newState;
            this.stop();
            System.out.println("State is STOP");
        } else if (state == RecordingState.STOP && newState == RecordingState.PLAYING) {
            state = newState;
            this.read();
            System.out.println("State is PLAYING");
        } else if (state == RecordingState.PLAYING && newState == RecordingState.IDLE) {
            state = newState;
            this.fakePlayer.remove(Entity.RemovalReason.KILLED);
            this.positionIndex = 0;
            System.out.println("State is IDLE and Remove fake");
        }
    }
}
