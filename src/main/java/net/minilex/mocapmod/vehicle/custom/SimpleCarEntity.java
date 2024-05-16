package net.minilex.mocapmod.vehicle.custom;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SimpleCarEntity extends LivingEntity implements OwnableEntity {
    private final NonNullList<ItemStack> handItems;
    private final NonNullList<ItemStack> armorItems;
    private Player owner;
    public SimpleCarEntity(EntityType<? extends LivingEntity> p_30531_, Level p_30532_) {
        super(p_30531_, p_30532_);
        this.handItems = NonNullList.withSize(2, ItemStack.EMPTY);
        this.armorItems = NonNullList.withSize(2, ItemStack.EMPTY);
    }
    @Override
    public void tick() {
        if (this.isVehicle()) {
            super.tick();
        }
    }
    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return this.armorItems;
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return new ItemStack(Items.AIR);
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {
        this.verifyEquippedItem(itemStack);
        switch (equipmentSlot.getType()) {
            case HAND:
                this.onEquipItem(equipmentSlot, (ItemStack)this.handItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
                break;
            case ARMOR:
                this.onEquipItem(equipmentSlot, (ItemStack)this.armorItems.set(equipmentSlot.getIndex(), itemStack), itemStack);
        }
    }

    @Override
    public HumanoidArm getMainArm() {
        return null;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 20D)
                .add(Attributes.FOLLOW_RANGE, 24D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5f)
                .add(Attributes.ATTACK_DAMAGE, 2f);
    }
    public InteractionResult interact(Player p_252289_, InteractionHand p_248927_) {
        this.owner = p_252289_;
        this.doPlayerRide(p_252289_);
        return InteractionResult.sidedSuccess(this.level.isClientSide);
    }
    protected void doPlayerRide(Player p_30634_) {
        if (!this.level.isClientSide) {
            p_30634_.startRiding(this);
        }
    }
    @Override
    public void travel(Vec3 p_21280_) {
        if (this.isVehicle() && this.isControlledByLocalInstance()) {
            LivingEntity player = this.owner;
            if (player == null) player = Minecraft.getInstance().player;
            this.setYRot(player.getYRot());
            float f1 = player.xxa * 0.5f;
            this.setSpeed(0.3f);
            super.travel(new Vec3(f1, player.yya, player.zza));
        } else {
            super.travel(p_21280_);
        }
    }
    @Nullable
    @Override
    public UUID getOwnerUUID() {
        return this.getControllingPassenger().getUUID();
    }
}
