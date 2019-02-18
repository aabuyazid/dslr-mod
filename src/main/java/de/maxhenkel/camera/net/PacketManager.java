package de.maxhenkel.camera.net;

import de.maxhenkel.camera.Config;
import de.maxhenkel.camera.ImageTools;
import de.maxhenkel.camera.Main;
import de.maxhenkel.camera.items.ItemImage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PacketManager {

    private Map<UUID, byte[]> clientDataMap;

    private Map<UUID, BufferedImage> imageCache;

    private Map<UUID, Long> times;

    public PacketManager() {
        this.clientDataMap = new HashMap<>();
        this.imageCache = new HashMap<>();
        this.times = new HashMap<>();
    }

    public void addBytes(EntityPlayerMP playerMP, UUID imgUUID, int offset, int length, byte[] bytes) {
        byte[] data;
        if (!clientDataMap.containsKey(imgUUID)) {
            data = new byte[length];
        } else {
            data = clientDataMap.get(imgUUID);
        }

        System.arraycopy(bytes, 0, data, offset, bytes.length);

        clientDataMap.put(imgUUID, data);

        if (offset + bytes.length >= data.length) {
            try {
                BufferedImage image = completeImage(imgUUID);
                if (image == null) {
                    throw new IOException("Image incomplete");
                }
                imageCache.put(imgUUID, image);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ImageTools.saveImage(playerMP, imgUUID, image);

                            playerMP.getServer().addScheduledTask(() -> {
                                ItemStack stack = new ItemStack(Main.IMAGE);
                                ItemImage.setUUID(stack, imgUUID);
                                ItemImage.setTime(stack, System.currentTimeMillis());
                                ItemImage.setOwner(stack, playerMP.getName().getUnformattedComponentText());

                                if (!playerMP.addItemStackToInventory(stack)) {
                                    InventoryHelper.spawnItemStack(playerMP.world, playerMP.posX, playerMP.posY, playerMP.posZ, stack);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, "SaveImageThread").start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedImage getExistingImage(EntityPlayerMP playerMP, UUID uuid) throws IOException {
        if (imageCache.containsKey(uuid)) {
            return imageCache.get(uuid);
        }
        BufferedImage image = ImageTools.loadImage(playerMP, uuid);
        imageCache.put(uuid, image);
        return image;
    }

    public BufferedImage completeImage(UUID imgUUID) {
        byte[] data = clientDataMap.get(imgUUID);
        if (data == null) {
            return null;
        }

        try {
            BufferedImage image = ImageTools.fromBytes(data);
            clientDataMap.remove(imgUUID);
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean canTakeImage(UUID player) {
        if (times.containsKey(player)) {
            if (System.currentTimeMillis() - times.get(player).longValue() < Config.imageCooldown) {
                return false;
            } else {
                times.put(player, System.currentTimeMillis());
                return true;
            }
        } else {
            times.put(player, System.currentTimeMillis());
            return true;
        }
    }


}
