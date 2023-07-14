package de.maxhenkel.corpse.events;

import de.maxhenkel.corpse.Death;
import de.maxhenkel.corpse.DeathManager;
import de.maxhenkel.corpse.entities.EntityCorpse;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collection;

@Mod.EventBusSubscriber
public class DeathEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void playerDeath(LivingDropsEvent event) {
        if (event.isCanceled()) {
            return;
        }

        Entity entity = event.getEntity();

        if (entity.world.isRemote) {
            return;
        }

        if (!(entity instanceof EntityLiving)) {
            return;
        }
        EntityLiving living = (EntityLiving) entity;

        try {
            Collection<EntityItem> drops = event.getDrops();



            NonNullList<ItemStack> stacks = NonNullList.create();

            for (EntityItem item : drops) {
                if (!item.getItem().isEmpty()) {
                    stacks.add(item.getItem());
                }
            }

            drops.clear();

            Death death = Death.fromEntity(living, stacks);
            DeathManager.addDeath(living, death);

            living.world.spawnEntity(EntityCorpse.createFromDeath(living, death));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
