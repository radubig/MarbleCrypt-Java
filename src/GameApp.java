import Exceptions.*;
import org.jsfml.graphics.*;
import org.jsfml.system.*;
import org.jsfml.window.*;
import org.jsfml.window.event.Event;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Vector;

public class GameApp {
    private static GameApp app;
    private GameApp() throws FontLoadException, TextureLoadException, FileLoadException {
        m_window = new RenderWindow();
        m_inv = new Inventory();

        // Load m_font (Mandatory)
        try {
            m_font.loadFromFile(fontFilePath);
        } catch (IOException e) {
            throw new Exceptions.FontLoadException(fontFilePath.toString());
        }

        // Load Shop Texture (Mandatory)
        try {
            m_shop_tx.loadFromFile(shopTextureFilePath);
        } catch (IOException e) {
            throw new Exceptions.TextureLoadException(shopTextureFilePath.toString());
        }

        // Load inventory data
        m_inv.LoadTextures("data/textures.txt");
        m_inv.LoadMarbleData("data/marbles.txt");
        m_inv.LoadInventory();
    }

    public static synchronized GameApp getInstance() throws FileLoadException, FontLoadException, TextureLoadException {
        if (app == null) {
            app = new GameApp();
        }
        return app;
    }

    public void SetResolution(int width, int height) {
        m_width = width;
        m_height = height;
    }
    public void SetFramerateLimit(int value) {
        m_framerate_limit = value;
    }
    public void InitWindow() {
        // create window
        m_window.create(new VideoMode(m_width, m_height), m_title, WindowStyle.DEFAULT);
        // m_window.setVerticalSyncEnabled(true);
        m_window.setFramerateLimit(m_framerate_limit);
    }
    public void EnableCheats() {
        m_cheats_enabled = true;
    }


    public void Run() {
        Clock fpsClock = new Clock();
        // Main rendering loop
        while (m_window.isOpen()) {
            // Render code here
            fpsClock.restart();
            m_window.clear(new Color(80, 80, 80));

            Vector<DrawableEntity> renderItems = new Vector<>();
            renderItems.add(new ShopEntity(m_shop_tx, m_font, m_inv));
            renderItems.add(new ActionEntity(m_inv, m_selected_marbles, m_font));

            // insert all marbles into renderItems
            for (int i = 0; i < m_inv.GetMarblesSize(); i++) {
                MarbleEntity marble = new MarbleEntity(m_inv, i, m_font);
                if(m_selected_marbles.contains(i))
                    marble.SetOutlineColor(Color.RED);
                renderItems.add(marble);
            }

            DrawableEntity.X = 0;
            DrawableEntity.Y = DrawableEntity.s_entity_offsetY;
            for(DrawableEntity entity : renderItems)
                entity.Draw(m_window);

            m_window.display();

            // Poll events
            View view = (View) m_window.getView();
            Vector2f pos = new Vector2f(Mouse.getPosition(m_window));
            Vector2f scrollOffset = Vector2f.div(view.getSize(), 2.0f);
            scrollOffset = Vector2f.sub(view.getCenter(), scrollOffset);
            pos = Vector2f.add(pos, scrollOffset);
            for (Event e : m_window.pollEvents())
            {
                if(e.type == Event.Type.CLOSED)
                    m_window.close();
                else if (e.type == Event.Type.RESIZED) {
                    var point = m_window.mapPixelToCoords(new Vector2i(0, 0));
                    FloatRect visibleArea = new FloatRect(point.x, point.y, e.asSizeEvent().size.x, e.asSizeEvent().size.y);
                    m_window.setView(new View(visibleArea));
                    m_width = e.asSizeEvent().size.x;
                    m_height = e.asSizeEvent().size.y;
                }
                else if (e.type == Event.Type.MOUSE_BUTTON_PRESSED) {
                    // Left Click events
                    if (e.asMouseButtonEvent().button == Mouse.Button.LEFT) {
                        for (DrawableEntity entity : renderItems) {
                            if (entity.isHovered(pos.x, pos.y)) {
                                if (entity instanceof ILeftClickable) {
                                    ((ILeftClickable) entity).OnLeftClick();
                                }
                            }
                        }
                    }
                    // Right Click events
                    else if (e.asMouseButtonEvent().button == Mouse.Button.RIGHT) {
                        for (DrawableEntity entity : renderItems) {
                            if (entity.isHovered(pos.x, pos.y)) {
                                if (entity instanceof MarbleEntity) {
                                    int index = ((MarbleEntity) entity).GetMarbleIndex();
                                    if (m_selected_marbles.contains(index))
                                        m_selected_marbles.remove(index);
                                    else
                                        m_selected_marbles.add(index);
                                }
                            }
                        }
                    }
                }
                else if (e.type == Event.Type.KEY_PRESSED)
                {
                    if (e.asKeyEvent().key == Keyboard.Key.A) {
                        if (m_cheats_enabled) {
                            double freeMoney = 15 * m_inv.GetNewMarblePrice();
                            m_inv.AddCoins(freeMoney);
                            System.out.println("Gained " + freeMoney + " free $MTK");
                        }
                    }
                    else if (e.asKeyEvent().key == Keyboard.Key.G) {
                        if (m_cheats_enabled)
                            m_inv.GenerateEachRarity();
                    }
                    else if (e.asKeyEvent().key == Keyboard.Key.S) {
                        m_inv.SaveInventory();
                    }
                    else if (e.asKeyEvent().key == Keyboard.Key.B) {
                        if (!m_inv.BuyMarble())
                            System.out.println("Not enough funds!");
                    }
                    else if (e.asKeyEvent().key == Keyboard.Key.DELETE) {
                        // Delete all progress and force save
                        m_inv.SetDefault();
                        m_inv.SaveInventory();
                    }
                    else if (e.asKeyEvent().key == Keyboard.Key.F) {
                        System.out.println("FPS: " + 1.0f / fpsClock.getElapsedTime().asSeconds());
                    }
                }
                else if (e.type == Event.Type.MOUSE_WHEEL_MOVED) {
                    m_scroll -= e.asMouseWheelEvent().delta * 80;
                    if (m_scroll < 0)
                        m_scroll = 0;
                    else {
                        view.move(0, -e.asMouseWheelEvent().delta * 80);
                        m_window.setView(view);
                    }
                }
            }
        }

        // Before app closing events
        m_inv.SaveInventory();
    }


    private final String m_title = "MarbleCrypt";
    private final Path fontFilePath = Path.of("data/OpenSans-Regular.ttf");
    private final Path shopTextureFilePath = Path.of("data/shop.png");
    private boolean m_cheats_enabled = false;
    private RenderWindow m_window;
    private int m_width = 1280;
    private int m_height = 720;
    private int m_framerate_limit = 144;
    private float m_scroll = 0.0f;
    private Inventory m_inv;
    private Font m_font = new Font();
    private Texture m_shop_tx = new Texture();
    private HashSet<Integer> m_selected_marbles = new HashSet<>();
}
