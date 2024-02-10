package net.minilex.mocapmod.entity.client;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;

public class VillagerTwoModel extends VillagerModel {
    public VillagerTwoModel(ModelPart p_171051_) {
        super(p_171051_);
        this.getHead().yScale = 1.3f;
        this.getHead().xScale = 1.3f;
    }

    public static LayerDefinition createBodyLayer() {
        return LayerDefinition.create(VillagerTwoModel.createBodyModel(), 128, 128);
    }
}
