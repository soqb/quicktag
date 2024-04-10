package io.github.soqb.quicktag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import it.unimi.dsi.fastutil.ints.Int2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.Uuids;
import net.minecraft.world.entity.EntityLike;

/**
 * This should have <code>O(1)</code> insertion/deletion and <code>O(n)</code> iteration.
 */
public class EntitySet<T extends EntityLike> implements Set<T> {
    private static class ListOfEntities<T extends EntityLike> extends ArrayList<T> {
        public void removeRange(int start, int end) {
            super.removeRange(start, end);
        }
    }

    private record DenseUuid(long most, long least) {
        public DenseUuid(UUID uuid) {
            this(uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        }
    }

    protected final ListOfEntities<T> entities;
    protected final Set<DenseUuid> unloadedUuids;
    protected final Int2IntMap indices;

    public EntitySet(int size) {
        this(new ListOfEntities<>(), new HashSet<>(), new Int2IntLinkedOpenHashMap(size));
    }

    protected EntitySet(ListOfEntities<T> entities, Set<DenseUuid> unloadedUuids,
            Int2IntMap indices) {
        this.entities = entities;
        this.unloadedUuids = unloadedUuids;
        this.indices = indices;
        indices.defaultReturnValue(-1);
    }

    @SuppressWarnings("unchecked")
    private int id(Object obj) {
        return ((T) obj).getId();
    }

    public T at(int index) {
        return entities.get(index);
    }

    public int indexOf(Object entity) {
        int idx = indices.get(id(entity));
        return idx;
    }

    @Override
    public boolean contains(Object entity) {
        return indexOf(entity) >= 0;
    }

    @Override
    public boolean containsAll(Collection<?> entities) {
        for (Object entity : entities) {
            if (!contains(entity))
                return false;
        }
        return true;
    }

    private void addUnloaded(long most, long least) {
        unloadedUuids.add(new DenseUuid(most, least));
    }

    public void load(T entity) {
        unloadedUuids.remove(new DenseUuid(entity.getUuid()));
        add(entity);
    }

    public void unload(T entity) {
        unloadedUuids.add(new DenseUuid(entity.getUuid()));
        remove(entity);
    }

    @Override
    public boolean add(T entity) {
        if (contains(entity))
            return false;

        indices.put(entity.getId(), entities.size());
        entities.add(entity);
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> incoming) {
        boolean succeeded = true;
        for (T entity : incoming) {
            succeeded = succeeded & add(entity);
        }
        return succeeded;
    }

    @Override
    public void clear() {
        entities.clear();
        indices.clear();
        unloadedUuids.clear();
    }

    @Override
    public boolean isEmpty() {
        return entities.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return entities.iterator();
    }

    public int[] toDenseIntList() {
        try {
            int[] ints = new int[(size() + unloadedUuids.size()) * 4];
            int i;
            for (i = 0; i < size(); i++) {
                int[] uuid = Uuids.toIntArray(at(i).getUuid());
                System.arraycopy(uuid, 0, ints, i * 4, 4);
            }
            for (DenseUuid uuid : unloadedUuids) {
                long uuidMost = uuid.most;
                long uuidLeast = uuid.least;
                int[] uuidInts = new int[] {(int) (uuidMost >> 32), (int) uuidMost,
                        (int) (uuidLeast >> 32), (int) uuidLeast};
                System.arraycopy(uuidInts, 0, ints, i++ * 4, 4);
            }
            return ints;
        } catch (Exception e) {
            e.printStackTrace();
            return new int[0];
        }
    }

    public static EntitySet<Entity> fromDenseIntList(int[] ints) {
        EntitySet<Entity> set = new EntitySet<>(ints.length / 4);
        for (int i = 0; i < ints.length; i += 4) {
            int[] uuidInts = new int[4];
            System.arraycopy(ints, i, uuidInts, 0, 4);
            set.addUnloaded((long) uuidInts[0] << 32 | (long) uuidInts[1] & 0xFFFFFFFFL,
                    (long) uuidInts[2] << 32 | (long) uuidInts[3] & 0xFFFFFFFFL);
        }
        return set;
    }

    @Override
    public boolean remove(Object entity) {
        int entryIdx = indexOf(entity);
        if (entryIdx < 0)
            return false;

        int lastIdx = entities.size() - 1;
        indices.remove(id(entity));

        if (entryIdx == lastIdx) {
            entities.remove(lastIdx);
        } else {
            T last = entities.get(lastIdx);
            entities.remove(lastIdx);

            indices.put(last.getId(), entryIdx);
            entities.set(entryIdx, last);
        }

        return true;
    }

    @Override
    public boolean removeAll(Collection<?> incoming) {
        boolean succeeded = true;
        int lastIdx = size();
        for (Object entityObject : incoming) {
            @SuppressWarnings("unchecked")
            T entity = (T) entityObject;
            int entryIdx = indexOf(entity);

            if (entryIdx < 0) {
                succeeded = false;
                continue;
            }

            lastIdx--;
            indices.remove(entity.getId());
            if (entryIdx != lastIdx) {
                T last = entities.get(lastIdx);
                indices.put(last.getId(), entryIdx);
                entities.set(entryIdx, last);
            }
        }
        entities.removeRange(lastIdx, size());
        return succeeded;
    }

    @Override
    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Unimplemented method 'retainAll'");
    }

    @Override
    public int size() {
        return entities.size();
    }

    @Override
    public Object[] toArray() {
        return entities.toArray();
    }

    @Override
    public <U> U[] toArray(U[] arg0) {
        return entities.toArray(arg0);
    }

    public List<T> toList() {
        return entities;
    }
}
