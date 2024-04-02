import org.jsfml.graphics.Color;
import org.jsfml.graphics.RectangleShape;
import org.jsfml.graphics.RenderWindow;

public abstract class DrawableEntity {
    // Methods to be implemented by the derived class
    protected abstract void DrawObject(RenderWindow window);


    // Pre-defined methods
    public boolean isHovered(float x, float y) {
        return m_canvas.getGlobalBounds().contains(x, y);
    }
    public void SetOutlineColor(Color color) {
        m_canvas.setOutlineColor(color);
    }
    private static void UpdateBounds(RenderWindow window) {
        float w_width = (float) window.getSize().x;
        X += s_entity_width;
        if (X + s_entity_width + s_entity_offsetX > w_width) {
            Y += s_entity_height + s_entity_offsetY;
            X = 0;
        }
    }
    public final void Draw(RenderWindow window) {
        // Add X offset to create some space
        X += s_entity_offsetX;

        // Render canvas
        m_canvas.setPosition(X, Y);
        window.draw(m_canvas);

        // Custom draw behavior
        DrawObject(window);

        // Update bounds
        UpdateBounds(window);
    }

    protected static float X = 0;
    protected static float Y = 0;
    protected final static float s_entity_width = 200.0f;
    protected final static float s_entity_height = 300.0f;
    protected final static float s_entity_image_size = 150.0f;
    protected final static float s_entity_offsetX = 20.0f;
    protected final static float s_entity_offsetY = 20.0f;
    protected final static int s_character_size = 19;
    protected RectangleShape m_canvas = new RectangleShape();
}
