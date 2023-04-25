import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Classe encarregada de mostrar la seqüència d'imatges,
 * extent JFrame.
 */
public class Visor extends JFrame {

    JLabel lblImageHolder;

    /**
     * Constructor de la classe.
     * @param imagen Imatge inicial que es mostrarà al panell.
     */
    public Visor(BufferedImage imagen) {

        JPanel contentPane = new JPanel();
        lblImageHolder = new JLabel("Image Holder");
        contentPane.add(lblImageHolder, BorderLayout.CENTER);
        this.setContentPane(contentPane);

        if (imagen != null) {
            ImageIcon icono = new ImageIcon((Image) imagen);
            lblImageHolder.setIcon(icono);
            this.setSize(icono.getIconWidth(), icono.getIconHeight());
        }

    }

    /**
     * Actualitza la imatge mostrada en el visor
     * @param imagen Nova imatge a mostrar
     */
    public void update_image(BufferedImage imagen) {
        if (imagen != null) {
            ImageIcon icono = new ImageIcon((Image) imagen);
            lblImageHolder.setIcon(icono);
            this.setSize(icono.getIconWidth(), icono.getIconHeight());
        }
    }


}