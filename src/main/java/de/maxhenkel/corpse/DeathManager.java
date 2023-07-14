package de.maxhenkel.corpse;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DeathManager {

    public static void addDeath(EntityLivingBase player, Death death) {
        try {
            File deathFile = getDeathFile(player, death.getId());
            deathFile.getParentFile().mkdirs();
            CompressedStreamTools.write(death.toNBT(), deathFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public static Death getDeath(EntityLivingBase player, UUID id) {
        try {
            return Death.fromNBT(CompressedStreamTools.read(getDeathFile(player, id)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<Death> getDeaths(EntityPlayerMP player) {
        return getDeaths(player);
    }

    public static List<Death> getDeaths(EntityPlayerMP context, EntityPlayerMP player) {
        return getDeaths(context, player);
    }

    public static List<Death> getDeaths(EntityPlayerMP context, UUID playerUUID) {
        File playerDeathFolder = getPlayerDeathFolder(context, playerUUID);

        if (!playerDeathFolder.exists()) {
            return Collections.emptyList();
        }

        File[] deaths = playerDeathFolder.listFiles((dir, name) -> {
            String[] split = name.split("\\.");
            if (split.length != 2) {
                return false;
            }
            if (split[1].equals("dat")) {
                try {
                    UUID.fromString(split[0]);
                    return true;
                } catch (Exception e) {
                }
            }
            return false;
        });

        return Arrays.stream(deaths)
                .map(f -> {
                    try {
                        return Death.fromNBT(CompressedStreamTools.read(f));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                })
                .filter(d -> d != null)
                .sorted(Comparator.comparingLong(Death::getTimestamp).reversed())
                .collect(Collectors.toList());
    }

    public static File getDeathFile(EntityLivingBase player, UUID id) {
        return new File(getPlayerDeathFolder(player), id.toString() + ".dat");
    }

    public static File getPlayerDeathFolder(EntityLivingBase player) {
        return getPlayerDeathFolder(player, player.getUniqueID());
    }

    public static File getPlayerDeathFolder(EntityLivingBase context, UUID uuid) {
        try {
            Files.createDirectory(Paths.get(context.getEntityWorld().getProviderName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new File(context.getEntityWorld().getProviderName(), uuid.toString());
    }

}
