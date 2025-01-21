import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;

public class ImageHandler {
    // Memastikan folder "images" ada dan dapat digunakan untuk menyimpan gambar
    public static void createImagesFolder() {
        File folder = new File("images");
        if (!folder.exists()) {
            folder.mkdir();
            System.out.println("Folder 'images' telah dibuat.");
        }
    }

    // Memuat gambar untuk item menu tertentu, jika tidak ditemukan, buat placeholder default
    public static ImageIcon loadImageForItem(String menuItem) {
        String imagePath = "images/" + menuItem.replaceAll("\\s", "_").toLowerCase(); // Menyesuaikan format nama file
        File imageFileJPG = new File(imagePath + ".jpg");
        File imageFileJPEG = new File(imagePath + ".jpeg");

        if (imageFileJPG.exists()) {
            return loadImage(imageFileJPG);
        } else if (imageFileJPEG.exists()) {
            return loadImage(imageFileJPEG);
        } else {
            System.out.println("Gambar tidak ditemukan untuk " + menuItem + ", membuat placeholder.");
            return createPlaceholderImage();
        }
    }

    // Metode bantu untuk memuat gambar dari file
    private static ImageIcon loadImage(File imageFile) {
        try {
            BufferedImage img = ImageIO.read(imageFile);
            return new ImageIcon(img.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.out.println("Error saat memuat gambar: " + e.getMessage());
            return createPlaceholderImage();
        }
    }

    // Membuat gambar placeholder jika gambar spesifik tidak ditemukan
    private static ImageIcon createPlaceholderImage() {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.WHITE);
        g2d.drawString("No Image", 20, 50);
        g2d.dispose();
        return new ImageIcon(img);
    }
}
