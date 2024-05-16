package net.minilex.mocapmod.vehicle.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minilex.mocapmod.MocapMod;

public class CarModelLayers {
    public static final ModelLayerLocation SIMPLE_CAR_LAYER = new ModelLayerLocation(
            new ResourceLocation(MocapMod.MODID, "simple_car_layer"),
            "main"
    );
}
