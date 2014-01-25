
package jay.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JComponent;
import jay.sampling.ImageAdapter;
import jay.sampling.ImageFilm;
import jay.sampling.ImageFilmListener;

/**
 *
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class FilmPanel extends JComponent {
    private final ImageFilm film;
    private final ImageAdapter offImg;
    
    /**
     * 
     * @param film
     */
    public FilmPanel(ImageFilm film) {
        this.film = film;
        this.film.addListener(new ListenerImpl());
        this.offImg = new ImageAdapter(film);
        this.setMinimumSize(new Dimension(film.xRes, film.yRes));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(offImg, 0, 0, this);
    }
    
    private class ListenerImpl implements ImageFilmListener {

        public void filmUpdated(ImageFilm film) {
            for (int y=0; y < film.yRes; y++) {
                for (int x=0; x < film.xRes; x++) {
                    offImg.setRGB(x, y, film.getRGB(x, y));
                }
            }

            FilmPanel.this.repaint();
        }
    }
}
