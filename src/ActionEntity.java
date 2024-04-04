import org.jsfml.graphics.Color;
import org.jsfml.graphics.Font;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.system.Vector2f;

import java.text.DecimalFormat;
import java.util.HashSet;

public class ActionEntity extends DrawableEntity implements ILeftClickable {
    public ActionEntity(Inventory inventory, HashSet<Integer> selectedMarbles, Font font) {
        m_inv = inventory;
        m_selectedMarbles = selectedMarbles;

        // Set canvas color and size
        m_canvas.setSize(new Vector2f(s_entity_width, s_entity_height));
        m_canvas.setFillColor(Color.TRANSPARENT);
        m_canvas.setOutlineColor(Color.WHITE);
        m_canvas.setOutlineThickness(2.0f);

        // Set text properties
        m_action_name.setFont(font);
        m_action_name.setCharacterSize(s_character_size);
        m_action_name.setColor(Color.WHITE);
        m_action_value.setFont(font);
        m_action_value.setCharacterSize(s_character_size);
        m_action_value.setColor(Color.WHITE);
    }

    @Override
    protected void DrawObject(RenderWindow window) {
        float x, y;
        DetermineAction();

        switch (m_action_type) {
            case Collect:
                m_action_name.setString("Collect all");
                m_action_name.setColor(Color.GREEN);
                x = (s_entity_width - m_action_name.getGlobalBounds().width) / 2.0f;
                y = s_entity_height / 2.0f - m_action_name.getGlobalBounds().height;
                m_action_name.setPosition(X + x, Y + y);
                window.draw(m_action_name);
                break;

            case Burn:
                m_action_name.setString("Burn marble for:");
                m_action_name.setColor(Color.RED);
                x = (s_entity_width - m_action_name.getGlobalBounds().width) / 2.0f;
                y = s_entity_height / 2.0f - m_action_name.getGlobalBounds().height;
                m_action_name.setPosition(X + x, Y + y);
                window.draw(m_action_name);

                int index = m_selectedMarbles.iterator().next();
                double value = m_inv.GetBurnValue(index);
                if(value < 1e9)
                    m_action_value.setString((long) value + " $MTK");
                else {
                    DecimalFormat df = new DecimalFormat("0.0####E0");
                    m_action_value.setString(df.format(value) + " $MTK");
                }
                x = (s_entity_width - m_action_value.getGlobalBounds().width) / 2.0f;
                y += 35.0f;
                m_action_value.setPosition(X + x, Y + y);
                window.draw(m_action_value);
                break;

            case Fusion:
                // Selected marbles are fusable
                m_action_name.setString("Fuse Marbles!");
                m_action_name.setColor(Color.CYAN);
                x = (s_entity_width - m_action_name.getGlobalBounds().width) / 2.0f;
                y = s_entity_height / 2.0f - m_action_name.getGlobalBounds().height;
                m_action_name.setPosition(X + x, Y + y);
                window.draw(m_action_name);
                break;

            default:
                // Show option to de-select marbles
                m_action_name.setString("De-select all");
                x = (s_entity_width - m_action_name.getGlobalBounds().width) / 2.0f;
                y = s_entity_height / 2.0f - m_action_name.getGlobalBounds().height;
                m_action_name.setPosition(X + x, Y + y);
                window.draw(m_action_name);
        }
    }

    @Override
    public void OnLeftClick() {
        DetermineAction();
        switch (m_action_type) {
            case Collect:
                m_inv.CollectAll();
                break;

            case Burn:
                m_inv.BurnMarble(m_selectedMarbles.iterator().next());
                m_selectedMarbles.clear();
                break;

            case Fusion:
                var iterator = m_selectedMarbles.iterator();
                int ind1 = iterator.next();
                int ind2 = iterator.next();
                m_inv.FuseMarbles(ind1, ind2);
                m_selectedMarbles.clear();
                break;

            case Deselect:
                m_selectedMarbles.clear();
                break;
        }
    }

    private Inventory m_inv;
    private HashSet<Integer> m_selectedMarbles;
    private Text m_action_name = new Text();
    private Text m_action_value = new Text();
    private enum ActionType {
        None, Collect, Burn, Fusion, Deselect
    }
    private ActionType m_action_type = ActionType.None;

    private void DetermineAction() {
        if (m_selectedMarbles.isEmpty())
            m_action_type = ActionType.Collect;
        else if (m_selectedMarbles.size() == 1)
            m_action_type = ActionType.Burn;
        else {
            var iterator = m_selectedMarbles.iterator();
            int m1 = iterator.next();
            int m2 = iterator.next();
            if(m_selectedMarbles.size() == 2 && m_inv.IsFusable(m1, m2))
                m_action_type = ActionType.Fusion;
            else
                m_action_type = ActionType.Deselect;
        }
    }
}
