package net.minilex.mocapmod.thread;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.MocapMod;

public class RecordThread implements Runnable {

    private static RecordThread instance;
    private Player player;
    public Boolean capture;
    private Boolean lastTickSwipe = false;
    private File dir;
    private FileOutputStream file;
    private ObjectOutputStream o;
    private int itemsEquipped[] = new int[5];
    private List<MocapAction> eventList;

    public RecordThread(Player _player, String capname) {
        // Create a new, second thread
        try {

            dir = new File(Minecraft.getInstance().getSingleplayerServer().getWorldPath(LevelResource.ROOT)
                    + "/" + "mocaps");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new FileOutputStream(dir.getAbsolutePath() + "/" + capname
                    + ".mocap");

            o = new ObjectOutputStream(file);
            //file.setLength(0);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        player = _player;
        capture = false;
        eventList = MocapMod.getInstance().getActionListForPlayer(player);
        //t = new Thread(this, "Mocap Record Thread");
        //t.start();
        instance = this;
    }

    // This is the entry point for the second thread.
    public void run() {
        try {

            if (capture) {
                trackAndWriteMovement();

                if (player.isDeadOrDying()) {
                    capture = false;
                    MocapMod.getInstance().recordThreads.remove(player);
                    MocapMod.getInstance().broadcastMsg("Stopped recording "
                            + player.getDisplayName() + ". RIP.");
                }
            }
            //in.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void trackAndWriteMovement() throws IOException {
        Vec3 entityPos = player.position();
        Position pos = new Position(entityPos.x, entityPos.y, entityPos.z);
        o.writeObject(pos);
    }

    private void trackArmor() {
        /*
         * Track armor equipped.
         */
        // TODO: Sequential equipping of same item id but different type =
        // problem.
        for (int ci = 1; ci < 5; ci++) {
            ItemStack armor = player.getInventory().armor.get(ci - 1);
            if (armor != null) {
                if (Item.getId(armor.getItem()) != itemsEquipped[ci]) {
                    itemsEquipped[ci] = Item.getId(armor.getItem());
                    MocapAction ma = new MocapAction(MocapActionTypes.EQUIP);
                    ma.armorSlot = ci;
                    ma.armorId = itemsEquipped[ci];
                    ma.armorDmg = armor.getDamageValue();
                    //ma.itemData = player.getInventory().armor.get(ci - 1);
                    eventList.add(ma);
                }
            } else {
                // TODO
                if (itemsEquipped[ci] != -1) {
                    itemsEquipped[ci] = -1;
                    MocapAction ma = new MocapAction(MocapActionTypes.EQUIP);
                    ma.armorSlot = ci;
                    ma.armorId = itemsEquipped[ci];
                    ma.armorDmg = 0;
                    eventList.add(ma);
                }
            }
        }
    }

    private void trackHeldItem() {
        if (player.getItemInHand(player.getUsedItemHand()) != null) {
            if (Item.getId(player.getItemInHand(player.getUsedItemHand()).getItem()) != itemsEquipped[0]) {
                itemsEquipped[0] = Item.getId(player.getItemInHand(player.getUsedItemHand())
                        .getItem());
                MocapAction ma = new MocapAction(MocapActionTypes.EQUIP);
                ma.armorSlot = 0;
                ma.armorId = itemsEquipped[0];
                ma.armorDmg = player.getItemInHand(player.getUsedItemHand()).getDamageValue();
                //player.getHeldItem().writeToNBT(ma.itemData);
                eventList.add(ma);
            }
        } else {
            if (itemsEquipped[0] != -1) {
                itemsEquipped[0] = -1;
                MocapAction ma = new MocapAction(MocapActionTypes.EQUIP);
                ma.armorSlot = 0;
                ma.armorId = itemsEquipped[0];
                ma.armorDmg = 0;
                eventList.add(ma);
            }
        }
    }

    private void trackSwing() {
        /*
         * Track "Swings" weapon / fist.
         */
        if (player.swinging) {
            if (!lastTickSwipe) {
                lastTickSwipe = true;
                eventList.add(new MocapAction(MocapActionTypes.SWIPE));
            }
        } else {
            lastTickSwipe = false;
        }
    }

//    private void writeActions() throws IOException {
//        // Any actions?
//        if (eventList != null && eventList.size() > 0) {
//            file.writeBoolean(true);
//            MocapAction ma = eventList.get(0);
//            file.writeByte(ma.type);
//
//            switch (ma.type) {
//                case MocapActionTypes.CHAT: {
//                    file.writeUTF(ma.message);
//                    break;
//                }
//
//                case MocapActionTypes.SWIPE: {
//                    break;
//                }
//
//                case MocapActionTypes.DROP: {
//                    //CompressedStreamTools.write(ma.itemData, in);
//                    break;
//                }
//
//                case MocapActionTypes.EQUIP: {
//                    file.writeInt(ma.armorSlot);
//                    file.writeInt(ma.armorId);
//                    file.writeInt(ma.armorDmg);
//
//                    if (ma.armorId != -1) {
//                        //CompressedStreamTools.write(ma.itemData, in);
//                    }
//
//                    break;
//                }
//
//                case MocapActionTypes.SHOOTARROW: {
//                    file.writeInt(ma.arrowCharge);
//                    break;
//                }
//
//                case MocapActionTypes.PLACEBLOCK: {
//                    file.writeInt(ma.xCoord);
//                    file.writeInt(ma.yCoord);
//                    file.writeInt(ma.zCoord);
//                    //CompressedStreamTools.write(ma.itemData, in);
//                    break;
//                }
//
//                case MocapActionTypes.BREAKBLOCK: {
//                    file.writeInt(ma.xCoord);
//                    file.writeInt(ma.yCoord);
//                    file.writeInt(ma.zCoord);
//                    break;
//                }
//
//                case MocapActionTypes.LOGOUT: {
//                    MocapMod.getInstance().recordThreads.remove(player);
//                    MocapMod.getInstance().broadcastMsg("Stopped recording "
//                            + player.getDisplayName() + ". Bye!");
//                    capture = false;
//                    break;
//                }
//            }
//
//            eventList.remove(0);
//        } else {
//            file.writeBoolean(false);
//        }
//    }

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
        try {
            FileInputStream fi = new FileInputStream(dir.getAbsolutePath() + "/" + "loh"
                    + ".mocap");
            ObjectInputStream oi = new ObjectInputStream(fi);

            Set<Position> result = new HashSet<>();
            try {
                for (;;) {
                    result.add((Position) oi.readObject());
                }
            } catch (EOFException e) {
                // End of stream
            }

            UUID id = UUID.randomUUID();
            GameProfile profile = new GameProfile(id, "Sasha");
            Minecraft minecraft = Minecraft.getInstance();
            
            ServerPlayer fakePlayer = new FakePlayer(minecraft.getSingleplayerServer(),
                    minecraft.getSingleplayerServer().getPlayerList().getServer().getLevel(Level.OVERWORLD),
                    profile);

            MinecraftServer minecraftServer = Minecraft.getInstance().getSingleplayerServer();
            Position pos = result.stream().findFirst().get();
            fakePlayer.setPos(pos.x, pos.y, pos.z);
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
