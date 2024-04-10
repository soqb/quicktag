package io.github.soqb.quicktag.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import io.github.soqb.quicktag.mixinhelper.ServerEntityManagerAccessor;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;

@Mixin(ServerEntityManager.class)
public abstract class ServerEntityManagerMixin<T extends EntityLike>
        implements AutoCloseable, ServerEntityManagerAccessor<T> {
    @Accessor
    public abstract EntityIndex<T> getIndex();
}
