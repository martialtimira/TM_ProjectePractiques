import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

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
