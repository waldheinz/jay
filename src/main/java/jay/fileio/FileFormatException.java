/*
 * FileFormatException.java
 *
 * Created on 10. Januar 2006, 17:30
 */

package jay.fileio;

import java.io.IOException;

/**
 * @author Matthias Treydte
 */
public class FileFormatException extends IOException {
    
    public FileFormatException(String expl) {
        super(expl);
    }
    
    public FileFormatException(String expl, Exception cause) {
        super(expl, cause);
    }
    
}
