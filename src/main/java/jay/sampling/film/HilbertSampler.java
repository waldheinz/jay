/*
 * HilbertSampler.java
 *
 * Created on 2. März 2006, 16:01
 */

package jay.sampling.film;

import java.util.Stack;

/**
 *
 * @author Matthias Treydte
 */
public class HilbertSampler extends FilmSampler {
    
    static enum Direction { NORTH, SOUTH, EAST, WEST };
    
    class ToDo {
        public Direction dir;
        public int depth;
        
        public ToDo(Direction dir, int depth) {
            this.dir = dir;
            this.depth = depth;
        }
    }
    
    Stack<ToDo> stack = new Stack<ToDo>();
    final int depth;
    int currX;
    int currY;
    
    /** Creates a new instance of HilbertSampler */
    public HilbertSampler(int[] extent) {
        super(extent);
        currX = minX;
        currY = minY;
        int length = Math.max(maxX-minX, maxY-minY);
        depth = neededDepth(length);
        initStack();
    }
    
    boolean outside(int x, int y) {
        return (x > maxX || y > maxY);
    }
    
    int neededDepth(int size) {
        if ((size / 2) <= 1) return 1;
        else return 1 + neededDepth(size / 2);
    }
    
    public void nextSample(int[] pixel) {
        pixel[0] = currX;
        pixel[1] = currY;
        
        do {
            nextPixel();
        } while(outside(currX, currY));
    }

    void nextPixel() {
        ToDo todo = stack.pop();
        
        if (todo.depth > 0) {
            switch (todo.dir) {
                case SOUTH:
                    stack.push(new ToDo(Direction.EAST, depth-1));
                    stack.push(new ToDo(Direction.SOUTH, depth-1));
                    stack.push(new ToDo(Direction.WEST, depth-1));
                    break;
                case EAST:
                    
            }
        }
    }

    private void initStack() {
        stack.push(new ToDo(Direction.SOUTH, depth));
        stack.push(new ToDo(Direction.EAST, depth));
        stack.push(new ToDo(Direction.NORTH, depth));
    }
    
}
