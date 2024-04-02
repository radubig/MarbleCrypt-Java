package Exceptions;

public class FontLoadException extends ResourceLoadException {
    public FontLoadException(String filePath) {
        super(filePath, "Font");
    }
}
