import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    /**
     * Afegeix un fitxer al zip output stream.
     * @param path camí del fitxer.
     * @param srcFile fitxer a afegir.
     * @param zos zip output stream.
     */
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

    /**
     * Afegeix un directori al zip output stream.
     * @param path camí del directori.
     * @param srcFolder directori a afegir.
     * @param zos zip output stream.
     */
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

    /**
     * Elimina un directori.
     * @param directory directori a eliminar.
     * @return boolean, true si el directori s'ha eliminat, false si no s'ha eliminat.
     */
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

    /**
     * Crea un fitxer zip.
     * @param srcFolder directori a colocar dins el zip.
     * @param destinationZipFile directori on es trobarà el fitxer zip.
     */
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

    /**
     * Converteix la mida del fitxer en bits a una magnitud llegible (KB, MB, GB...)
     * @param fileSize mida del fitxer en bits.
     * @return String amb el valor i la magnitud apropiada.
     */
    public static String formatFileSize(long fileSize) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = fileSize;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
