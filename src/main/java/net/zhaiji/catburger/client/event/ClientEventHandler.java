package net.zhaiji.catburger.client.event;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.zhaiji.catburger.client.render.CatBurgerRenderer;
import net.zhaiji.catburger.equipment.CatBurgerEquipment;
import net.zhaiji.catburger.equipment.CatBurgerEquipment.WornCatBurger;

import java.util.Optional;

public class ClientEventHandler {
    public static void handlerRenderLivingEvent$Post(RenderLivingEvent.Post event) {
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity.isSpectator()) {
            return;
        }

        Optional<WornCatBurger> wornCatBurgerOptional = CatBurgerEquipment.find(livingEntity);
        if (wornCatBurgerOptional.isEmpty() || !wornCatBurgerOptional.get().renderable()) {
            return;
        }

        CatBurgerRenderer.render(
                livingEntity,
                wornCatBurgerOptional.get().itemStack(),
                event.getPoseStack(),
                event.getMultiBufferSource(),
                event.getPackedLight(),
                event.getPartialTick()
        );
    }
}
