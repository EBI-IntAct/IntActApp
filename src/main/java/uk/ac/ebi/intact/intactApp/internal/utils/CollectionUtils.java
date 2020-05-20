package uk.ac.ebi.intact.intactApp.internal.utils;

import java.util.*;
import java.util.function.Function;

public class CollectionUtils {
    public static <T> boolean anyCommonElement(Collection<T> set1, Collection<T> set2) {
        return !Collections.disjoint(set1, set2);
    }

    public static <K, E> Map<K, List<E>> groupBy(Iterable<E> toGroup, Function<E, K> groupBy) {
        Map<K, List<E>> groups = new HashMap<>();
        for (E element : toGroup) {
            if (element != null)
                addToGroups(groups, element, groupBy);
        }
        return groups;
    }

    public static <K, E> void addToGroups(Map<K, List<E>> groups, E toAdd, Function<E, K> groupBy) {
        K key = groupBy.apply(toAdd);
        if (!groups.containsKey(key)) {
            List<E> elementsOfKey = new ArrayList<>();
            elementsOfKey.add(toAdd);
            groups.put(key, elementsOfKey);
        } else {
            groups.get(key).add(toAdd);
        }
    }


    public static <K, E> Map<K, List<E>> groupByMultipleKeys(Iterable<E> toGroup, Function<E, Set<K>> elementToGroupingKeys) {
        Map<K, List<E>> groups = new HashMap<>();
        for (E element : toGroup) {
            if (element != null)
                addToMultipleGroups(groups, element, elementToGroupingKeys);
        }
        return groups;
    }

    public static <K, E> void addToMultipleGroups(Map<K, List<E>> groups, E toAdd, Function<E, Set<K>> elementToGroupingKeys) {
        for (K key : elementToGroupingKeys.apply(toAdd)) {
            if (!groups.containsKey(key)) {
                List<E> elementsOfKey = new ArrayList<>();
                elementsOfKey.add(toAdd);
                groups.put(key, elementsOfKey);
            } else {
                groups.get(key).add(toAdd);
            }
        }
    }
}

