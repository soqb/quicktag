package io.github.soqb.quicktag.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import io.github.soqb.quicktag.mixinhelper.ServerWorldAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin implements ServerWorldAccessor {
    @Accessor
    public abstract ServerEntityManager<Entity> getEntityManager();
}
