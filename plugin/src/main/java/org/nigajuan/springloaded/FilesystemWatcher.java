package org.nigajuan.springloaded;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nigajuan on 10/02/14.
 */
public class FilesystemWatcher {

    private static final Logger log = LoggerFactory.getLogger(FilesystemWatcher.class);

    public static final String CLASS_EXTENSION = ".class";

    private Path basePath;

    private Map<String, Long> loadedFiles = new ConcurrentHashMap<>();

    public FilesystemWatcher() throws URISyntaxException {
        basePath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();
    }

    public FilesystemWatcher(Path basePath) {
        this.basePath = basePath;
    }

    public void init() throws IOException {
        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String fullPath = file.toString();
                if (isClassFile(fullPath)) {
                    loadedFiles.put(keyFromPath(file), attrs.lastModifiedTime().toMillis());
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }


    public void scan() throws IOException {

        final List<ToReloadFile> modifiedFiles = new ArrayList<>();

        Files.walkFileTree(basePath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                loadClass(file, attrs, modifiedFiles);
                return FileVisitResult.CONTINUE;
            }
        });

        for (ToReloadFile toReloadFile : modifiedFiles) {
            try {
                if (toReloadFile.isLoad()) {
                    getClass().getClassLoader().loadClass(toReloadFile.getDotted());
                }
                if (toReloadFile.isReload()) {
                    //Force reload to trigger the event
                    long modifiedDate = new Date().getTime();
                    Files.setLastModifiedTime(toReloadFile.getFile(), FileTime.fromMillis(modifiedDate));

                    loadedFiles.put(keyFromPath(toReloadFile.getFile()), modifiedDate);
                }
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    private void loadClass(Path file, BasicFileAttributes attrs, List<ToReloadFile> modifiedFiles) throws IOException {
        String fullPath = file.toString();

        if (isClassFile(fullPath)) {
            if (!loadedFiles.containsKey(fullPath)) {
                String dotted = basePath.relativize(file).toString().replaceAll("[\\\\/]", ".").replace(CLASS_EXTENSION, "");
                modifiedFiles.add(new ToReloadFile(file, dotted, true, true));
            } else if (loadedFiles.get(fullPath) != attrs.lastModifiedTime().toMillis()) {
                modifiedFiles.add(new ToReloadFile(file, null, false, true));
            }
        }
    }


    private boolean isClassFile(String path) {
        return path.endsWith(CLASS_EXTENSION);
    }

    private String keyFromPath(Path path) {
        return path.toString();
    }
}
