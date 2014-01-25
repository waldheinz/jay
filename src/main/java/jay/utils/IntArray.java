/*
 * IntArray.java
 *
 * Created on 20. Juni 2007, 14:18
 */

package jay.utils;

/**
 * Implements a array of int with constant size. Main use is to be
 * able to create sub-arrays of the array.
 *
 * @author Matthias Treydte <waldheinz@gmail.com>
 */
public class IntArray {
    
    /** The array which holds the data. */
    private final int data[];
    
    /** The offset into the data array, where this IntArray actually starts. */
    private final int offset;
    
    /** The length of this array. */
    private final int size;
    
    /** Creates a new instance of IntArray */
    public IntArray(int size) {
        data = new int[size];
        offset = 0;
        this.size = size;
    }
    
    private IntArray(int data[], int offset, int size) {
        if (offset + size > data.length)
            throw new IllegalArgumentException("offset + size > data.length");
        else if (size < 0)
            throw new IllegalArgumentException("negative size (" + size + ")");
        
        this.data = data;
        this.offset = offset;
        this.size = size;
    }
    
    /**
     * Returns the specified element of the array.
     *
     * @param index The index of the element to return.
     * @return The value of the specified element.
     */
    public int get(int index) {
        return data[offset + index];
    }
    
    /**
     * Sets the specified element to the given value.
     */
    public void set(int index, int value) {
        if (index >= offset + size)
            throw new ArrayIndexOutOfBoundsException();
        
        data[offset + index] = value;
    }
    
    /**
     * Returns a new IntArray which is a view of the data
     * contained in this array. The underlying array which
     * holds the element data is shared between both, the
     * new and this array.
     */
    public IntArray subArray(int offset, int size) {
        return new IntArray(data, this.offset + offset,
                (size > 0)?size:this.size - offset);
    }
    
}
