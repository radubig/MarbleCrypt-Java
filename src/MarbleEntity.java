import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

public class MarbleEntity extends DrawableEntity implements ILeftClickable {
    public MarbleEntity(Inventory inventory, int index, Font font) {
        this.m_inv = inventory;
        this.m_indexOfMarble = index;

        // Set canvas color and size
        m_canvas.setSize(new Vector2f(s_entity_width, s_entity_height));
        m_canvas.setFillColor(Color.TRANSPARENT);
        m_canvas.setOutlineColor(Color.WHITE);
        m_canvas.setOutlineThickness(2.0f);

        // Set text properties
        m_current_yield.setFont(font);
        m_current_yield.setCharacterSize(s_character_size);
        m_current_yield.setColor(Color.WHITE);
        m_name.setFont(font);
        m_name.setCharacterSize(s_character_size);
        m_name.setColor(Color.WHITE);
        m_daily_yield.setFont(font);
        m_daily_yield.setCharacterSize(s_character_size);
        m_daily_yield.setColor(Color.WHITE);

        m_current_yield.setString("Current yield: " + m_inv.GetMarble(index).GetYield());
        m_daily_yield.setString("Daily yield: " + m_inv.GetMarble(index).GetDailyYield());
        m_name.setString(m_inv.GetMarble(index).GetName());
        m_name.setCharacterSize(s_character_size + 2);
    }
    @Override
    public void OnLeftClick() {
        m_inv.AddCoins(m_inv.GetMarble(m_indexOfMarble).GetYield());
        m_inv.GetMarble(m_indexOfMarble).CollectYield();
    }
    @Override
    protected void DrawObject(RenderWindow window) {
        float x, y;
        Marble marble = m_inv.GetMarble(m_indexOfMarble);

        // Rarity Color
        switch (marble.GetRarity()) {
            case Rare -> m_name.setColor(new Color(52, 211, 153));
            case SuperRare -> m_name.setColor(new Color(207, 151, 0));
            case UltraRare -> m_name.setColor(new Color(244, 63, 94));
            case Legendary -> m_name.setColor(new Color(139, 92, 246));
            case Mythic -> m_name.setColor(new Color(37, 99, 235));
        }
        m_name.setStyle(Text.BOLD);

        // Render image
        x = (s_entity_width - s_entity_image_size) / 2.0f;
        y = 10.0f; // Hardcoded for now
        RectangleShape imag1 = new RectangleShape(), imag2 = new RectangleShape();
        imag1.setSize(new Vector2f(s_entity_image_size, s_entity_image_size));
        imag1.setPosition(X + x, Y + y);
        imag2.setSize(new Vector2f(s_entity_image_size, s_entity_image_size));
        imag2.setPosition(X + x, Y + y);
        if (marble.GetTexture2() == null) {
            imag1.setTexture(marble.GetTexture1());
            window.draw(imag1);
        }
        else {
            imag1.setTexture(marble.GetTexture1());
            imag2.setTexture(marble.GetTexture2());
            Color origColor = imag1.getFillColor();
            Color cl2 = new Color(origColor, origColor.a / 2);
            imag2.setFillColor(cl2);
            window.draw(imag1);
            window.draw(imag2);
        }

        // Render text
        x = 5.0f;
        y = s_entity_image_size + 25.0f;
        m_name.setPosition(X + x, Y + y);
        window.draw(m_name);
        y += 35.0f;
        m_current_yield.setPosition(X + x, Y + y);
        window.draw(m_current_yield);
        y += 35.0f;
        m_daily_yield.setPosition(X + x, Y + y);
        window.draw(m_daily_yield);
    }
    public int GetMarbleIndex() {
        return m_indexOfMarble;
    }

    private Inventory m_inv;
    private int m_indexOfMarble;
    private Text m_name = new Text();
    private Text m_current_yield = new Text();
    private Text m_daily_yield = new Text();
}
