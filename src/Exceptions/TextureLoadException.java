package Exceptions;

public class TextureLoadException extends ResourceLoadException {
    public TextureLoadException(String filePath) {
        super(filePath, "Texture");
    }
}
