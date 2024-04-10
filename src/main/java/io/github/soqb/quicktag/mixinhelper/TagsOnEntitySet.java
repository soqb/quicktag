package io.github.soqb.quicktag.mixinhelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.Entity;

public class TagsOnEntitySet implements Set<String> {
    public interface Listener {
        public void removeTag(String tag, Entity entity);

        public void addTag(String tag, Entity entity);
    }

    private final Entity holder;
    private final Listener listener;

    private final Set<String> inner;

    public TagsOnEntitySet(Entity holder, Set<String> inner, Listener listener) {
        this.holder = holder;
        this.inner = inner;
        this.listener = listener;
    }

    private void addTag(String tag) {
        listener.addTag(tag, holder);
    }

    private void removeTag(String tag) {
        listener.removeTag(tag, holder);
    }

    @Override
    public boolean addAll(Collection<? extends String> arg0) {
        for (String tag : arg0) {
            addTag(tag);
        }
        return inner.addAll(arg0);
    }

    @Override
    public boolean add(String tag) {
        addTag(tag);
        return inner.add(tag);
    }

    @Override
    public void clear() {
        for (String tag : this) {
            removeTag(tag);
        }
        inner.clear();
    }

    @Override
    public boolean contains(Object arg0) {
        return inner.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return inner.containsAll(arg0);
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public Iterator<String> iterator() {
        return inner.iterator();
    }

    @Override
    public boolean remove(Object tagObject) {
        String tag = (String) tagObject;
        removeTag(tag);
        return inner.remove(tag);
    }

    @Override
    public boolean removeAll(Collection<?> tags) {
        for (Object tagObject : tags) {
            String tag = (String) tagObject;
            removeTag(tag);
        }
        return inner.removeAll(tags);
    }

    @Override
    public boolean retainAll(Collection<?> tags) {
        for (Object tagObject : tags) {
            String tag = (String) tagObject;
            if (!contains(tag)) {
                removeTag(tag);
            }
        }
        return inner.retainAll(tags);
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public Object[] toArray() {
        return inner.toArray();
    }

    @Override
    public <T> T[] toArray(T[] arg0) {
        return inner.toArray(arg0);
    }
}

