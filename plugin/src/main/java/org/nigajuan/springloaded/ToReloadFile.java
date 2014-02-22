package org.nigajuan.springloaded;

import java.nio.file.Path;

/**
 * Created by nigajuan on 13/02/14.
 */
public class ToReloadFile {


    private final String dotted;
    private final boolean reload;
    private final boolean load;
    private final Path file;

    public ToReloadFile(Path file, String dotted, boolean load, boolean reload) {
        this.file = file;
        this.dotted = dotted;
        this.load = load;
        this.reload = reload;
    }

    public String getDotted() {
        return dotted;
    }

    public boolean isReload() {
        return reload;
    }

    public boolean isLoad() {
        return load;
    }

    public Path getFile() {
        return file;
    }
}
