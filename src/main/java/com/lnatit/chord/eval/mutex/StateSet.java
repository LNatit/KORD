package com.lnatit.chord.eval.mutex;

import com.lnatit.chord.data.mutex.MutexSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;

public sealed interface StateSet permits StateSet.UnionSet, StateSet.HyperRect {
    StateSet intersect(StateSet other);

    StateSet union(StateSet other);

    StateSet complement();

    List<HyperRect> rects();

    // TODO
    default boolean isIdenticalWith(StateSet other) {
        return this == other || this.isSubsetOf(other) && other.isSubsetOf(this);
    }

    default boolean isSubsetOf(StateSet other) {
        if (other.isFull()) return true;
        if (this.isFull()) return false;
        if (this.isEmpty()) return true;
        if (other.isEmpty()) return false;

        return switch (other) {
            case HyperRect otherRect -> this.rects().stream().allMatch(r -> r.isSubsetOfRect(otherRect));
            case UnionSet otherUnion -> {
                if (this.rects().stream().allMatch(r -> otherUnion.rects().stream().anyMatch(r::isSubsetOfRect)))
                    yield true;
                yield this.intersect(otherUnion.complement()).isEmpty();
            }
        };
    }

    default boolean isProperSubsetOf(StateSet other) {
        return this.isSubsetOf(other) && !this.isIdenticalWith(other);
    }

    default boolean isSupersetOf(StateSet other) {
        return other.isSubsetOf(this);
    }

    record HyperRect(Object2IntMap<MutexSet> mutexSetToBitmap) implements StateSet {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public HyperRect {
            mutexSetToBitmap = Object2IntMaps.unmodifiable(mutexSetToBitmap);
        }

        private static HyperRect singleton(MutexSet mutexSet, int bitmap) {
            Object2IntOpenHashMap<MutexSet> map = new Object2IntOpenHashMap<>();
            map.put(mutexSet, bitmap);
            return new HyperRect(map);
        }

        @Override
        public StateSet intersect(StateSet other) {
            if (this.isFull()) return other;
            if (other.isFull()) return this;
            if (this.isEmpty() || other.isEmpty()) return EMPTY;

            // TODO recheck
            return switch (other) {
                case HyperRect otherRect -> {
                    Object2IntMap<MutexSet> newMap = new Object2IntOpenHashMap<>();
                    Set<MutexSet> allMutexSets = new HashSet<>();
                    allMutexSets.addAll(this.mutexSetToBitmap().keySet());
                    allMutexSets.addAll(otherRect.mutexSetToBitmap().keySet());

                    for (MutexSet mutexSet : allMutexSets) {
                        int bitmap1 = this.mutexSetToBitmap().getOrDefault(mutexSet, mutexSet.getMask());
                        int bitmap2 = otherRect.mutexSetToBitmap().getOrDefault(mutexSet, mutexSet.getMask());
                        int res = bitmap1 & bitmap2;
                        if (res == 0) {
                            yield EMPTY;
                        }
                        newMap.put(mutexSet, res);
                    }
                    yield new HyperRect(newMap);
                }
                case UnionSet otherUnion -> otherUnion.intersect(this);
            };
        }

        @Override
        public StateSet union(StateSet other) {
            if (this.isFull() || other.isFull()) return FULL;
            if (this.isEmpty()) return other;
            if (other.isEmpty()) return this;

            return switch (other) {
                case HyperRect otherRect -> UnionSet.of(this, otherRect);
                case UnionSet ignored -> other.union(this);
            };
        }

        @Override
        public StateSet complement() {
            if (this.isFull()) return EMPTY;
            // No possible to be EMPTY

            // TODO recheck
            List<HyperRect> result = new ArrayList<>();
            for (Object2IntMap.Entry<MutexSet> entry : this.mutexSetToBitmap().object2IntEntrySet()) {
                int bitmap = entry.getIntValue();
                int res = entry.getKey().getMask() & ~bitmap; // 取反并保留有效位
                if (res != 0) {
                    result.add(singleton(entry.getKey(), res));
                }
            }
            return UnionSet.of(result);
        }

        @Override
        public List<HyperRect> rects() {
            return Collections.singletonList(this);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StateSet other) {
                return this.isIdenticalWith(other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            // TODO
            return 0;
        }

        private boolean isSubsetOfRect(HyperRect other) {
            for (Object2IntMap.Entry<MutexSet> entry : this.mutexSetToBitmap().object2IntEntrySet()) {
                int bitmap1 = entry.getIntValue();
                int bitmap2 = other.mutexSetToBitmap().getOrDefault(entry.getKey(), entry.getKey().getMask());
                if ((bitmap1 & bitmap2) != bitmap1) {
                    return false;
                }
            }
            return true;
        }
    }

    StateSet FULL = new HyperRect(new Object2IntOpenHashMap<>());

    default boolean isFull() {
        return this == FULL;
    }

    static StateSet singleton(MutexSet mutexSet, int bitmap) {
        if (bitmap == 0) {
            return EMPTY;
        }
        return HyperRect.singleton(mutexSet, bitmap);
    }

    record UnionSet(List<HyperRect> rects) implements StateSet {
        @Deprecated
        @ApiStatus.Internal
        @SuppressWarnings("all")
        public UnionSet {
            rects = List.copyOf(rects);
        }

        // 带化简的UnionSet构造器
        private static StateSet of(List<HyperRect> rects) {
            if (rects.isEmpty()) return EMPTY;

            List<HyperRect> result = new ArrayList<>();
            for (HyperRect rect : rects) {
                // 跳过被已有矩形包含的矩形
                if (result.stream().noneMatch(rect::isSubsetOf)) {
                    // 删除被当前矩形包含的已有矩形
                    result.removeIf(rect::isSupersetOf);
                    result.add(rect);
                }
            }
            return new UnionSet(List.copyOf(result));
        }

        private static StateSet of(HyperRect... rects) {
            return of(Arrays.asList(rects));
        }

        @Override
        public StateSet intersect(StateSet other) {
            if (this.isEmpty() || other.isEmpty()) return EMPTY;
            if (this.isFull()) return other;
            if (other.isFull()) return this;

            List<HyperRect> result = new ArrayList<>();
            for (HyperRect r1 : this.rects()) {
                for (HyperRect r2 : other.rects()) {
                    StateSet inter = r1.intersect(r2);
                    if (!inter.isEmpty()) {
                        result.addAll(inter.rects());
                    }
                }
            }
            return of(result);
        }

        @Override
        public StateSet union(StateSet other) {
            if (this.isEmpty()) return other;
            if (other.isEmpty()) return this;
            if (this.isFull() || other.isFull()) return FULL;

            List<HyperRect> result = new ArrayList<>();
            result.addAll(this.rects());
            result.addAll(other.rects());
            return of(result);
        }

        @Override
        public StateSet complement() {
            if (this.isEmpty()) return FULL;

            StateSet result = FULL;
            // 每个矩形取补，然后求交集
            for (HyperRect rect : this.rects()) {
                result = result.intersect(rect.complement());
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof StateSet other) {
                return this.isIdenticalWith(other);
            }
            return false;
        }

        @Override
        public int hashCode() {
            // TODO
            return 0;
        }
    }

    StateSet EMPTY = new UnionSet(List.of());

    default boolean isEmpty() {
        return this == EMPTY;
    }

    static boolean isMutex(StateSet set1, StateSet set2) {
        return set1.intersect(set2).isEmpty();
    }
}
