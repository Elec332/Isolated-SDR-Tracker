package elec332.isdrt;

import elec332.isdrt.util.ISetup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Elec332 on 5-10-2019
 */
public class PreStartup {

    public PreStartup(File folderBase, Config config) {
        this.folderBase = new File(folderBase, Main.SDR_FOLDER);
        this.config = config;
    }

    private final File folderBase;
    private final Config config;

    public void clearWorkingDir() throws IOException {
        if (folderBase.exists()){
            try {
                rmDir(folderBase);
            } catch (IOException e) {
                rmDir(folderBase);
            }
        }
    }

    public void extractProgram(File zip) throws IOException {
        unzip(zip, folderBase);
    }


    public void setup(ISetup... runners) throws IOException {
        for (ISetup setup : runners){
            setup.setup(folderBase, config);
        }
    }

    private static void rmDir(File folder) throws IOException {
        Files.walkFileTree(folder.toPath(), new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void unzip(File source, File out) throws IOException {
        byte[] buffer = new byte[1024];
        if (!out.exists()) {
            out.mkdir();
        }
        ZipInputStream zis = new ZipInputStream(new FileInputStream(source));
        ZipEntry ze = zis.getNextEntry();
        while (ze != null) {
            String fileName = ze.getName();
            File newFile = new File(out, fileName);
            if (ze.isDirectory()) {
                newFile.mkdirs();
            } else {
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
            ze = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }
}
