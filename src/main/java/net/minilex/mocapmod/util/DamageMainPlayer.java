package net.minilex.mocapmod.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minilex.mocapmod.handler.PlayerHandler;
import net.minilex.mocapmod.state.RecordingState;
import net.minilex.mocapmod.state.SceneData;

public class DamageMainPlayer {
    private static DamageMainPlayer instance;
    private Minecraft minecraft;
    private Player mainPlayer;
    private Player remotePlayer;
    private PlayerHandler playerHandler;
    private SceneUtil sceneUtil;
    private Player closerPlayer;
    private CommandUtil commandUtil;
    private DamageMainPlayer() {
        minecraft = Minecraft.getInstance();
        mainPlayer = minecraft.player;
        playerHandler = PlayerHandler.getInstance();
        sceneUtil = SceneUtil.getInstance();
        remotePlayer = minecraft.getSingleplayerServer().overworld().getPlayerByUUID(mainPlayer.getUUID());
        commandUtil = CommandUtil.getInstance();
    }
    public static DamageMainPlayer getInstance() {
        if (instance == null) {
            instance = new DamageMainPlayer();
        }
        return instance;
    }
    public void handle() {
        if (playerHandler.getRecordThread().getState() != RecordingState.PLAYING_SCENE) return;
        if (sceneUtil.scene == null || sceneUtil.scene.isEmpty()) return;
        double result = Double.MAX_VALUE;
        for (SceneData sceneData : sceneUtil.scene) {
            double d = distance(sceneData.fakePlayer);
            if (result > d) {
                result = d;
                closerPlayer = sceneData.fakePlayer;
            }
        }
        result = Double.MAX_VALUE;
        Vec3 vector = new Vec3(closerPlayer.position().x - mainPlayer.position().x,
                closerPlayer.position().y - mainPlayer.position().y,
                closerPlayer.position().z - mainPlayer.position().z);
        remotePlayer.knockback(0.3f, vector.x, vector.z);
        //DamageSource damagesource = new DamageSource(minecraft.level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.CACTUS), null, null);
        remotePlayer.hurt(mainPlayer.damageSources().outOfWorld(), commandUtil.damage);
    }
    private double distance(Player fakePlayer) {
        double ac = Math.abs(fakePlayer.getZ() - mainPlayer.getZ());
        double cb = Math.abs(fakePlayer.getX() - mainPlayer.getX());
        return Math.hypot(ac, cb);
    }
}
