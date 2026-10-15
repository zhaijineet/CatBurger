package net.zhaiji.catburger.init;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zhaiji.catburger.CatBurger;
import net.zhaiji.catburger.attachment.FollowerRenderData;

import java.util.function.Supplier;

public class CatBurgerAttachmentTypes {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CatBurger.MOD_ID);

    /**
     * 挂在玩家身上的跟随者渲染参数：落盘以便重生与服务端重启后保留，并自动同步给追踪者。
     */
    public static final Supplier<AttachmentType<FollowerRenderData>> FOLLOWER_RENDER = ATTACHMENT_TYPES.register(
            "follower_render",
            () -> AttachmentType.builder(FollowerRenderData::new)
                               .serialize(new IAttachmentSerializer<CompoundTag, FollowerRenderData>() {
                                   @Override
                                   public FollowerRenderData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
                                       FollowerRenderData followerRenderData = new FollowerRenderData();
                                       followerRenderData.deserializeNBT(provider, tag);
                                       return followerRenderData;
                                   }

                                   @Override
                                   public CompoundTag write(FollowerRenderData attachment, HolderLookup.Provider provider) {
                                       return attachment.serializeNBT(provider);
                                   }
                               })
                               .copyOnDeath()
                               .sync(FollowerRenderData.STREAM_CODEC)
                               .build()
    );
}
