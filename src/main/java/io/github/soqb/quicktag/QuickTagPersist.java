package io.github.soqb.quicktag;

import java.io.File;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

public class QuickTagPersist extends PersistentState {
    private static final String TAG_REGISTRY_KEY = "tag_registry";
    private MinecraftServer server;

    public QuickTagPersist(MinecraftServer server) {
        this.server = server;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound cmp) {
        QuickTag.LOGGER.info("writing tag registries");
        cmp.put(TAG_REGISTRY_KEY, TagRegistry.toNbt(TagRegistry.forServer(server)));
        return cmp;
    }

    private static QuickTagPersist createFromNbt(MinecraftServer server, NbtCompound nbt,
            ServerWorld world) {
        QuickTag.LOGGER.info("reading tag registries");
        QuickTagPersist state = new QuickTagPersist(server);
        TagRegistry<Entity> registry = TagRegistry.forServer(server);
        registry.setModifyHook(() -> state.markDirty());
        TagRegistry.updateFromNbt(registry, nbt.getCompound(TAG_REGISTRY_KEY));
        return state;
    }


    public static QuickTagPersist createFromServer(MinecraftServer server, ServerWorld world) {
        PersistentStateManager persistentStateManager =
                server.getWorld(World.OVERWORLD).getPersistentStateManager();

        Type<QuickTagPersist> type = new Type<>(() -> new QuickTagPersist(server),
                (nbt) -> createFromNbt(server, nbt, world), null);

        QuickTagPersist state = persistentStateManager.getOrCreate(type, QuickTag.MOD_ID);
        state.markDirty();

        return state;
    }

    // public static QuickTagPersist markDirtyOnServer(MinecraftServer server) {
    // PersistentStateManager persistentStateManager =
    // server.getWorld(World.OVERWORLD).getPersistentStateManager();
    // }

    @Override
    public void save(File file) {
        super.save(file);
        markDirty();
    }

}
