package net.zhaiji.catburger.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.capability.FollowerRenderData;

public class CatBurgerCapabilities {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(CatBurger.MOD_ID, "follower_render");

    public static final Capability<FollowerRenderData> FOLLOWER_RENDER = CapabilityManager.get(new CapabilityToken<>() {
    });
}
