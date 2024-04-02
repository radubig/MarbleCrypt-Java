package Exceptions;

public class ResourceLoadException extends Exception {
    public ResourceLoadException(String filePath, String fileType) {
        super(fileType + " \"" + filePath + "\" could not be loaded!");
    }
}
