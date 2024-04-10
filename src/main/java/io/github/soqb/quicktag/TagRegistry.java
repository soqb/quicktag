package io.github.soqb.quicktag;

import java.util.HashMap;
import java.util.Map;
import io.github.soqb.quicktag.mixinhelper.HasTagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityLike;

public class TagRegistry<T extends EntityLike> {
    private static final int DEFAULT_TAG_SIZE = 4;
    private final Map<String, EntitySet<T>> map;
    private Runnable onModify = () -> {
        return;
    };

    public TagRegistry(int size) {
        map = new HashMap<>(size);
    }

    public void setModifyHook(Runnable hook) {
        onModify = hook;
    }

    public EntitySet<T> tagMut(String tag) {
        onModify.run();
        return tagPromiseNotToModify(tag);
    }

    public EntitySet<T> tagPromiseNotToModify(String tag) {
        return map.computeIfAbsent(tag, (k) -> new EntitySet<>(DEFAULT_TAG_SIZE));
    }

    @SuppressWarnings("unchecked")
    public static <T extends EntityLike> TagRegistry<T> forServer(MinecraftServer server) {
        return ((HasTagRegistry<T>) (Object) server).getTagRegistry();
    }

    public static NbtCompound toNbt(TagRegistry<Entity> tags) {
        NbtCompound nbt = new NbtCompound();
        for (String key : tags.map.keySet()) {
            EntitySet<Entity> set = tags.tagPromiseNotToModify(key);
            if (!set.isEmpty())
                nbt.putIntArray(key, set.toDenseIntList());
        }
        return nbt;
    }

    public static void updateFromNbt(TagRegistry<Entity> registry, NbtCompound nbt) {
        for (String key : nbt.getKeys()) {
            int[] ints = nbt.getIntArray(key);
            registry.map.put(key, EntitySet.fromDenseIntList(ints));
        }
    }
}
