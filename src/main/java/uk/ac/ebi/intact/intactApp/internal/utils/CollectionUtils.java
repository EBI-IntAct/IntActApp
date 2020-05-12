package uk.ac.ebi.intact.intactApp.internal.utils;

import java.util.Collection;
import java.util.Collections;

public class CollectionUtils {
    public static <T> boolean anyCommonElement(Collection<T> set1, Collection<T> set2) {
        return !Collections.disjoint(set1, set2);
    }
}
