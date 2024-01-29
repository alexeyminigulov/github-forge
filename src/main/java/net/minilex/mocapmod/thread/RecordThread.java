package net.minilex.mocapmod.thread;

import java.io.*;
import java.util.*;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.MocapMod;
import net.minilex.mocapmod.state.RecordingState;

public class RecordThread implements Runnable {

    private static RecordThread instance;
    private LocalPlayer player;
    public RecordingState state;
    //private Boolean lastTickSwipe = false;
    private File dir;
    private FileOutputStream file;
    private ObjectOutputStream o;
    //private int itemsEquipped[] = new int[5];
    //private List<MocapAction> eventList;
    public FakePlayer fakePlayer;
    public Set<Position> result;
    public int positionIndex = 0;

    public RecordThread(LocalPlayer _player, String capname) {
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
    public static RecordThread getInstance() {
        if (instance == null) {
            RecordThread recordThread = new RecordThread(Minecraft.getInstance().player, "loh");
        }
        return instance;
    }

    public FileOutputStream getFile() {
        return file;
    }

    public void stop() {
        try {
            //o.writeObject(resultForFile);
            file.close();
            o.close();
        } catch (IOException e) {
            System.out.println("Can't close file");
        }
    }

    public void read() {
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
                // End of stream
            }
            /*List<Position> resultList = result.stream()
                    .sorted(Comparator.comparing(Position::getRowId))
                    .collect(Collectors.toList());*/

            UUID id = UUID.randomUUID();
            GameProfile profile = new GameProfile(id, "Sasha");
            Minecraft minecraft = Minecraft.getInstance();
            
            fakePlayer = new FakePlayer(minecraft.getSingleplayerServer(),
                    minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD),
                    profile);

            MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
            Position pos = result.stream().findFirst().get();
            fakePlayer.setPos(pos.x, pos.y, pos.z);
            fakePlayer.setXRot(pos.rotX);
            fakePlayer.setYRot(pos.rotY);
            fakePlayer.setYHeadRot(pos.rotY);
            minecraftServer.overworld().addNewPlayer(fakePlayer);

            ClientboundPlayerInfoUpdatePacket cpf = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer);
            minecraft.getConnection().handlePlayerInfoUpdate(cpf);

            result.forEach(position -> System.out.println(position));

            oi.close();
            fi.close();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
    }
}
