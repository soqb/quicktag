package io.github.soqb.quicktag.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.entity.EntityChangeListener;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import io.github.soqb.quicktag.TagRegistry;
import io.github.soqb.quicktag.mixinhelper.TagsOnEntitySet;

@Mixin(Entity.class)
public class EntityMixin {
    @Mutable
    @Final
    @Shadow
    private Set<String> commandTags;

    @Shadow
    private EntityChangeListener changeListener;

    private TagRegistry<Entity> getRegistry(Entity entity) {
        if (!(entity.getWorld() instanceof ServerWorld))
            throw new UnsupportedOperationException("expected server world!");
        return TagRegistry.forServer(entity.getWorld().getServer());
    }

    @Overwrite
    public void setChangeListener(EntityChangeListener newChangeListener) {
        Entity thisEntity = (Entity) (Object) this;
        changeListener = new EntityChangeListener() {
            @Override
            public void updateEntityPosition() {
                newChangeListener.updateEntityPosition();
            }

            @Override
            public void remove(RemovalReason reason) {
                newChangeListener.remove(reason);

                for (String tag : commandTags) {
                    getRegistry(thisEntity).tagMut(tag).remove(thisEntity);
                }
            }

        };
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<?> type, World world, CallbackInfo ci) {
        TagsOnEntitySet.Listener handler = new TagsOnEntitySet.Listener() {
            @Override
            public void removeTag(String tag, Entity entity) {
                getRegistry(entity).tagMut(tag).remove(entity);
            }

            @Override
            public void addTag(String tag, Entity entity) {
                getRegistry(entity).tagMut(tag).add(entity);
            }
        };
        commandTags = new TagsOnEntitySet((Entity) (Object) this, commandTags, handler);
    }
}
