package de.maxhenkel.corpse.entities;

import de.maxhenkel.corpse.PlayerSkins;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelEnderman;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class RenderCorpse extends Render<EntityCorpse> {

    private static final ResourceLocation SKELETON_TEXTURE = new ResourceLocation("textures/entity/skeleton/skeleton.png");

    private Minecraft mc;
    private ModelPlayer modelPlayer;
    private ModelPlayer modelPlayerSlim;
    private static final MethodHandles.Lookup IMPL_LOOKUP;

    static {
        try {
            Field f = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            f.setAccessible(true);
            IMPL_LOOKUP = (MethodHandles.Lookup) f.get(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }


    public RenderCorpse(RenderManager renderManager) {
        super(renderManager);
        mc = Minecraft.getMinecraft();
        modelPlayer = new ModelPlayer(0F, false);
        modelPlayerSlim = new ModelPlayer(0F, true);
        modelPlayer.isChild = false;
        modelPlayerSlim.isChild = false;
    }

    private Map<Render, MethodHandle> handlerCache = new HashMap<>();
    private Map<EntityCorpse, ResourceLocation> cache = new WeakHashMap<>();

    @Override
    public void doRender(EntityCorpse entity, double x, double y, double z, float entityYaw, float partialTicks) {

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(360F - entity.getCorpseRotation(), 0F, 1F, 0F);
        GlStateManager.rotate(90, 1F, 0F, 0F);
        GlStateManager.translate(0D, -0.5D, -2D / 16D);


        if (entity.getCorpseAge() >= 20 * 60 * 60 || !entity.isPlayer()) {
            //bindTexture(SKELETON_TEXTURE);
            // get texture
            if (cache.containsKey(entity)) {
                bindTexture(cache.get(entity));
            } else {
                if (!handlerCache.containsKey(entity.getRenderer())) {
                    for (Method declaredMethod : entity.getRenderer().getClass().getDeclaredMethods()) {
                        if (declaredMethod.getReturnType() == ResourceLocation.class && declaredMethod.getParameterCount() <= 2) {
                            declaredMethod.setAccessible(true);
                            try {
                                handlerCache.put(entity.getRenderer(), IMPL_LOOKUP.unreflect(declaredMethod));
                            } catch (IllegalAccessException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
                try {
                    cache.put(entity, (ResourceLocation) handlerCache.get(entity.getRenderer()).invoke(entity.getRenderer(), entity.getPreviousEntity()));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                bindTexture(cache.get(entity));
            }
            entity.getRenderer().getMainModel().render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
        } else {
            bindTexture(getEntityTexture(entity));
            if (isSlim(entity.getCorpseUUID())) {
                modelPlayerSlim.render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
            } else {
                modelPlayer.render(entity, 0F, 0F, 0F, 0F, 0F, 0.0625F);
            }
        }
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityCorpse entity) {
        return PlayerSkins.getSkin(entity.getCorpseUUID(), entity.getCorpseName());
    }

    public boolean isSlim(UUID uuid) {
        NetworkPlayerInfo networkplayerinfo = mc.getConnection().getPlayerInfo(uuid);
        return networkplayerinfo == null ? (uuid.hashCode() & 1) == 1 : networkplayerinfo.getSkinType().equals("slim");
    }

}
