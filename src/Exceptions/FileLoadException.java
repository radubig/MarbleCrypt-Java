package Exceptions;

public class FileLoadException extends ResourceLoadException {
    public FileLoadException(String filePath) {
        super(filePath, "File");
    }
}
