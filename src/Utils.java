import ImageClass.ImageFrame;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    public int count;
    public void saveZip(ArrayList<ImageFrame> imgList, String outName){
        ZipOutputStream outputStreamZip = null;
        BufferedOutputStream outputStreamBuffered;
        FileOutputStream outputStreamFile;
        try {
            outputStreamFile = new FileOutputStream(outName, true);
            outputStreamBuffered = new BufferedOutputStream (outputStreamFile);
            outputStreamZip = new ZipOutputStream(outputStreamBuffered);

            for (ImageFrame image : imgList) {
                ZipEntry entry = new ZipEntry("outImage"+image.getId()+".jpg");
                try {
                    outputStreamZip.putNextEntry(entry);
                    ImageIO.write(image.getImage(), "jpg", outputStreamZip);
                } catch (IOException ex) {
                    System.out.println("ERROR: There has been a problem while writing the image to the output file");
                    try{
                        outputStreamZip.flush();
                        outputStreamZip.close();
                    } catch (Exception e) {
                        System.out.println("S'ha produit un error tancant la connexio");
                    }
                }
                this.count++;
            }
            try{
                outputStreamZip.flush();
                outputStreamZip.close();
            } catch (Exception e) {
                System.out.println("S'ha produit un error tancant la connexio");
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            try{
                outputStreamZip.flush();
                outputStreamZip.close();
            } catch (Exception e) {
                System.out.println("S'ha produit un error tancant la connexio");
            }
        }
    }

    public void addFile(String path, String srcFile, ZipOutputStream zos) {
        File folder = new File(srcFile);
        if(folder.isDirectory()) {
            this.addDirectory(path, srcFile, zos);
        }
        else {
            try {
                byte[] buffer = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(srcFile);
                zos.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
                while ((len = in.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                in.close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void addDirectory(String path, String srcFolder, ZipOutputStream zos) {
        File folder = new File(srcFolder);

        for(String fileName: folder.list()) {
            if (path.equals("")) {
                addFile(folder.getName(), srcFolder + "/" + fileName, zos);
            } else {
                addFile(path + "/" + folder.getName(), srcFolder + "/" + fileName, zos);
            }
        }
    }

    public boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            for (String file: files) {
                boolean deleted = this.deleteDirectory(new File(directory, file));
                if(!deleted) {
                    return false;
                }
            }
        }
        return directory.delete();
    }

    public void createZipFolder(String srcFolder, String destinationZipFile) {
        try {
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destinationZipFile));

            this.addDirectory("", srcFolder, zos);

            zos.flush();
            zos.close();


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
