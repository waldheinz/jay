/*
 * RasterizerListener.java
 *
 * Created on 15. Dezember 2005, 18:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jay.utils;

import jay.sampling.ImageFilm;

/**
 *
 * @author trem
 */
public interface RasterizerListener {
    public void renderStarted(ImageFilm film);
    public void tileStarted(int x0, int y0, int x1, int y1);
    public void tileFinished(int x0, int y0, int x1, int y1);
    public void passFinished(int pass);
}
