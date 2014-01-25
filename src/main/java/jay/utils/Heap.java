/*
 * Heap.java
 *
 * Created on 3. März 2006, 18:07
 */

package jay.utils;

import java.util.Arrays;

/**
 * Minimum - Heap.
 *
 * @author Matthias Treydte
 */
public class Heap {
    
    public static void make(Comparable[] a, int from, int to) {
        int len = to - from + 1;
        for (int i = len/2 - 1; i >= 0; i--) {
            makeSubHeap(a, i+from, to);
        }
    }
    
    public static void pop(Comparable[] a, int from, int to) {
        swap(a, from, to);
        make(a, from, to-1);
    }
    
    public static void push(Comparable[] a, int from, int to) {
        make(a, from, to);
    }
    
    @SuppressWarnings("unchecked")
    private static void makeSubHeap(Comparable[] a, int i, int max) {
        int smallestIndex = smallestChild(a, i, max);
        if (smallestIndex == -1) return;
        if (a[i].compareTo(a[smallestIndex]) > 0) {
            swap(a, i, smallestIndex);
            makeSubHeap(a, smallestIndex, max);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static int smallestChild(Comparable[] a, int i, int max) {
        /* nicht zu weit! */
        i++;
        if (i * 2 - 1 > max) return -1;
        if (i * 2 > max) return i * 2 - 1;
        
        final Comparable child1 = a[i * 2 - 1];
        final Comparable child2 = a[i * 2];
        if (child1.compareTo(child2) < 0) return i * 2 - 1;
        else return i * 2;
    }
    
    private static void swap(Object[] a, int i1, int i2) {
        final Object tmp = a[i1];
        a[i1] = a[i2];
        a[i2] = tmp;
    }
    
    public static void main(String[] args) {
        Integer[] a = new Integer[12];
        
        for (int i=0; i < a.length; i++) {
            a[i] = (int)(Math.random() * 100);
        }
        
        System.out.println(Arrays.toString(a));
        
        make(a, 0, a.length-1);
        
        System.out.println(Arrays.toString(a));
        
        
    }
    
}
