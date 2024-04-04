package net.minilex.mocapmod.particle.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minilex.mocapmod.thread.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class CitrineParticles extends TextureSheetParticle {
    public Player player = Minecraft.getInstance().player;
    protected CitrineParticles(ClientLevel level, double xCoord, double yCoord, double zCoord,
                               double xd, double yd, double zd, SpriteSet spriteSet) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);

        this.friction = 0.6f;
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize = 0.25f;
        this.lifetime = 1000;
        this.pickSprite(spriteSet);

        this.rCol = 1f;
        this.gCol = 1f;
        this.bCol = 1f;
    }
    @Override
    public void tick() {
        if (player instanceof FakePlayer) {
            this.xd = (player.position().x - this.x)/3.3;
            this.yd = (player.position().y + 2.5d - this.y)/3.3;
            this.zd = (player.position().z - this.z)/3.3;
        } else {
            this.xd = player.position().x - this.x;
            this.yd = player.position().y + 2.5d - this.y;
            this.zd = player.position().z - this.z;
        }

        super.tick();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider {
        private static SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        public static SpriteSet getSpriteSet() {
            return Provider.spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel,
                                       double x, double y, double z, double dx, double dy, double dz) {
            return new CitrineParticles(clientLevel, x, y, z, dx, dy, dz, spriteSet);
        }
    }
}
