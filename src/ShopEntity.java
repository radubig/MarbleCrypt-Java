import org.jsfml.graphics.*;
import org.jsfml.system.Vector2f;

import java.text.DecimalFormat;

public class ShopEntity extends DrawableEntity implements ILeftClickable {
    public ShopEntity(Texture texture, Font font, Inventory inv) {
        m_inv = inv;
        // Set canvas color and size
        m_canvas.setSize(new Vector2f(s_entity_width, s_entity_height));
        m_canvas.setFillColor(Color.TRANSPARENT);
        m_canvas.setOutlineColor(Color.WHITE);
        m_canvas.setOutlineThickness(2.0f);

        // Set image properties
        m_image.setTexture(texture);
        m_image.setSize(new Vector2f(s_entity_image_size, s_entity_image_size));

        // Set text properties
        m_cost.setFont(font);
        m_cost.setCharacterSize(s_character_size);
        m_cost.setColor(Color.WHITE);
        m_owned_marbles.setFont(font);
        m_owned_marbles.setCharacterSize(s_character_size);
        m_owned_marbles.setColor(Color.WHITE);
        m_balance.setFont(font);
        m_balance.setCharacterSize(s_character_size);
        m_balance.setColor(Color.WHITE);

        double newMarblePrice = inv.GetNewMarblePrice();
        double balance = inv.GetBalance();
        if (newMarblePrice < 1e9)
            m_cost.setString("Cost: " + (long) newMarblePrice);
        else {
            DecimalFormat df = new DecimalFormat("0.0####E0");
            m_cost.setString("Cost: " + df.format(newMarblePrice));
        }

        if (balance < 1e9)
            m_balance.setString("Balance: " + (long) balance);
        else {
            DecimalFormat df = new DecimalFormat("0.0####E0");
            m_balance.setString("Balance: " + df.format(balance));
        }

        m_owned_marbles.setString("Collected: " + m_inv.GetCurrentDistinctMarbles() +
                " / " + m_inv.GetTotalDistinctMarbles());
    }
    @Override
    public void OnLeftClick() {
        if (!m_inv.BuyMarble())
            System.out.println("Not enough funds!");
    }
    @Override
    protected void DrawObject(RenderWindow window) {
        float x, y;

        // Render image
        x = (s_entity_width - s_entity_image_size) / 2.0f;
        y = 10.0f; // Hardcoded for now
        m_image.setPosition(X + x, Y + y);
        window.draw(m_image);

        // Render text
        x = 5.0f;
        y = s_entity_image_size + 25.0f;
        m_cost.setPosition(X + x, Y + y);
        window.draw(m_cost);
        y += 35.0f;
        m_balance.setPosition(X + x, Y + y);
        window.draw(m_balance);
        y += 35.0f;
        m_owned_marbles.setPosition(X + x, Y + y);
        window.draw(m_owned_marbles);
    }

    private Inventory m_inv;
    private RectangleShape m_image = new RectangleShape();
    private Text m_balance = new Text();
    private Text m_cost = new Text();
    private Text m_owned_marbles = new Text();
}
