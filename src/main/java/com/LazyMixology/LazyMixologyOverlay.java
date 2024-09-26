package com.LazyMixology;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;

public class LazyMixologyOverlay extends Overlay {
    public static final List<Integer> POTIONUNFID = Arrays.asList(ItemID.MAMMOTHMIGHT_MIX, ItemID.MYSTIC_MANA_AMALGAM, ItemID.MARLEYS_MOONLIGHT, ItemID.AZURE_AURA_MIX, ItemID.ALCOAUGMENTATOR, ItemID.MIXALOT, ItemID.AQUALUX_AMALGAM, ItemID.MEGALITE_LIQUID, ItemID.ANTILEECH_LOTION, ItemID.LIPLACK_LIQUOR );
    public static final List<Integer> POTIONFINISHEDID = Arrays.asList(ItemID.MAMMOTHMIGHT_MIX_30021, ItemID.MYSTIC_MANA_AMALGAM_30022, ItemID.MARLEYS_MOONLIGHT_30023, ItemID.AZURE_AURA_MIX_30026, ItemID.ALCOAUGMENTATOR_30024, ItemID.MIXALOT_30030, ItemID.AQUALUX_AMALGAM_30025, ItemID.MEGALITE_LIQUID_30029, ItemID.ANTILEECH_LOTION_30028, ItemID.LIPLACK_LIQUOR_30027);
    @Inject
    public LazyMixologyOverlay(Client client, LazyMixologyPlugin plugin)
    {
        this.client = client;
        this.plugin = plugin;
        setPosition(OverlayPosition.DYNAMIC);
        setPriority(OverlayPriority.HIGH);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }
    @Inject
    private Client client;
    @Inject
    private LazyMixologyPlugin plugin;

    public int spriteID;
    // IDs for each direction's herb
    private static final int NORTH_EAST_HERB_ID = 55396;
    private static final int NORTH_WEST_HERB_ID = 55399;
    private static final int SOUTH_EAST_HERB_ID = 55397;
    private static final int SOUTH_WEST_HERB_ID = 55398;

    //GameObject IDs
    public int moxLever = 54868;
    public int lyeLever = 54869;
    public int mixelVessel = 55395;
    public int alembic = 55391;
    public int agitator = 55390;
    public int retort = 55389;
    public int conveyorBelt = 54917;
    public int hopper = 54903;
    //Decorative object ID
    public int agaLever = 54867;

    public boolean checkInventoryForItems(List<Integer> itemIds) {
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);

        Item[] items;
        if (inventory == null || inventory.getItems() == null) {
            items = new Item[0];
        } else {
            items = inventory.getItems();
        }
        for (Item item : items) {
            if (itemIds.contains(item.getId())) {
                return true;
            }
        }
        return false;
    }
    private List<GameObject> findGameObjectsByID(int objectID) {
        List<GameObject> gameObjects = new ArrayList<>();
        for (int x = 0; x < Constants.SCENE_SIZE; x++) {
            for (int y = 0; y < Constants.SCENE_SIZE; y++) {
                Tile tile = client.getScene().getTiles()[client.getPlane()][x][y];
                if (tile == null) {
                    continue;
                }

                for (GameObject gameObject : tile.getGameObjects()) {
                    if (gameObject != null && gameObject.getId() == objectID) {
                        gameObjects.add(gameObject);
                    }
                }
            }
        }
        return gameObjects;
    }

    private void drawGameObjectClickbox(Graphics2D graphics, GameObject gameObject, Color color) {
        Shape objectClickbox = gameObject.getClickbox();
        if (objectClickbox != null) {
            graphics.setColor(color);
            graphics.draw(objectClickbox);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() / 5));
            graphics.fill(objectClickbox);
        }
    }
    public Overlay gameObjectOverlay(int objectId, Color color, String text) {
        return new Overlay() {
            @Override
            public Dimension render(Graphics2D graphics) {
                if (client != null) {
                    // Find all game objects with the given ID
                    List<GameObject> gameObjects = findGameObjectsByID(objectId);

                    // Loop through each game object
                    for (GameObject gameObject : gameObjects) {
                        // Draw the colored clickbox around the game object
                        drawGameObjectClickbox(graphics, gameObject, color);
                    }
                }
                return null;
            }
        };
    }


    public List<DecorativeObject> findDecorativeObjectsByID(int objectId) {
        List<DecorativeObject> foundDecorativeObjects = new ArrayList<>();

        if (client != null) {
            Tile[][][] tiles = client.getScene().getTiles();
            for (int plane = 0; plane < tiles.length; plane++) {
                for (int x = 0; x < tiles[plane].length; x++) {
                    for (int y = 0; y < tiles[plane][x].length; y++) {
                        Tile tile = tiles[plane][x][y];
                        if (tile != null) {
                            DecorativeObject decorativeObject = tile.getDecorativeObject();
                            if (decorativeObject != null && decorativeObject.getId() == objectId) {
                                foundDecorativeObjects.add(decorativeObject);
                            }
                        }
                    }
                }
            }
        }

        return foundDecorativeObjects;
    }

    public Overlay decorativeObjectOverlay(int objectId, Color color) {
        return new Overlay() {
            @Override
            public Dimension render(Graphics2D graphics) {
                if (client != null) {
                    List<DecorativeObject> decorativeObjects = findDecorativeObjectsByID(objectId);
                    for (DecorativeObject decorativeObject : decorativeObjects) {
                        drawDecorativeObjectClickbox(graphics, decorativeObject, color);
                    }
                }
                return null;
            }
        };
    }


    public void drawDecorativeObjectClickbox(Graphics2D graphics, DecorativeObject decorativeObject, Color color) {
        Shape clickbox = decorativeObject.getClickbox();
        if (clickbox != null) {
            graphics.setColor(color);
            graphics.draw(clickbox);
            graphics.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
            graphics.fill(clickbox);
        }
    }
    private void drawTextOnTile(Graphics2D graphics, String text, WorldPoint worldPoint) {
        // Convert WorldPoint to LocalPoint (needed for positioning in the game's view)
        LocalPoint localPoint = LocalPoint.fromWorld(client, worldPoint);

        // Ensure the tile is in the client's viewing area
        if (localPoint != null) {
            // Get screen location from the local point (where the text will be drawn)
            Point screenLocation = Perspective.getCanvasTextLocation(client, graphics, localPoint, text, 0);

            if (screenLocation != null) {
                // Set the desired font and color for the text
                graphics.setFont(new Font("Arial", Font.BOLD, 25));  // Customize font size, style as needed
                graphics.setColor(Color.WHITE); // Customize text color as needed

                // Get font metrics to calculate text width
                FontMetrics fontMetrics = graphics.getFontMetrics();
                int textWidth = fontMetrics.stringWidth(text);

                // Calculate the starting X position for the text to be center-aligned
                int centeredX = screenLocation.getX() - (textWidth / 2);

                // Draw the text at the centered position
                graphics.drawString(text, centeredX, screenLocation.getY());
            }
        }
    }

    Boolean finished = true;
    @Override
    public Dimension render(Graphics2D graphics) {
        int varAlembic = client.getVarbitValue(11328);
        int varAgitator = client.getVarbitValue(11329);
        int varRetort = client.getVarbitValue(11327);
            if(plugin.herbSpawned) {
                herb(graphics, plugin.location);
            }
            else if (checkInventoryForItems(POTIONFINISHEDID)) {
                gameObjectOverlay(conveyorBelt, Color.GREEN, null).render(graphics);
                finished = true;
            }
            //Alembic
            else if (varAlembic != 0) {
                WorldPoint worldPoint = new WorldPoint(1391, 9326, 0);

                boolean isAnimationActive = false; // Flag to check if the animation is active
                WorldView worldView = client.getWorldView(-1);
                for (GraphicsObject graphicsObject : worldView.getGraphicsObjects()) {
                    // Check if the specific graphics object ID is present
                    if (graphicsObject.getId() == 2955) {
                        isAnimationActive = true; // Set the flag to true if the animation ID is found
                        break; // No need to continue checking if we found it
                    }
                }

                // Render the overlays based on whether the animation is active
                if (isAnimationActive) {
                    gameObjectOverlay(conveyorBelt, Color.YELLOW, null).render(graphics);
                    gameObjectOverlay(alembic, Color.CYAN, null).render(graphics);
                    drawTextOnTile(graphics, "Click", worldPoint);
                } else {
                    gameObjectOverlay(alembic, Color.GREEN, null).render(graphics);
                    gameObjectOverlay(conveyorBelt, Color.YELLOW, null).render(graphics);
                }
            }
            //agitator
            else if (varAgitator != 0) {
                WorldPoint worldPoint = new WorldPoint(1394, 9329, 0);
                boolean isAnimationActive = false; // Flag to check if the animation is active
                WorldView worldView = client.getWorldView(-1);
                for (GraphicsObject graphicsObject : worldView.getGraphicsObjects()) {
                    // Check if the specific graphics object ID is present
                    if (graphicsObject.getId() == 2954) {
                        isAnimationActive = true; // Set the flag to true if the animation ID is found
                        break; // No need to continue checking if we found it
                    }
                }

                // Render the overlays based on whether the animation is active
                if (isAnimationActive) {
                    gameObjectOverlay(conveyorBelt, Color.YELLOW, null).render(graphics);
                    gameObjectOverlay(agitator, Color.CYAN, null).render(graphics);
                    drawTextOnTile(graphics, "Click", worldPoint);
                } else {
                    gameObjectOverlay(agitator, Color.GREEN, null).render(graphics);
                    gameObjectOverlay(conveyorBelt, Color.YELLOW, null).render(graphics);
                }
            }
            //retort
            else if (varRetort != 0) {
                WorldPoint worldPoint = new WorldPoint(1397, 9326, 0);
                gameObjectOverlay(retort, Color.GREEN, null).render(graphics);
                gameObjectOverlay(conveyorBelt, Color.YELLOW, null).render(graphics);
                drawTextOnTile(graphics, "Spam click", worldPoint);
            } else if (checkInventoryForItems(POTIONUNFID)) {
                workbench(graphics, Color.GREEN);
            } else if (client.getVarbitValue(11339) != 0) {
                gameObjectOverlay(mixelVessel, Color.GREEN, null).render(graphics);
                workbench(graphics, Color.YELLOW);
                finished = false;
            } else if (client.getVarbitValue(11326) == 0 && finished) {
                if (client.getVarbitValue(11321) < 30 || client.getVarbitValue(11322) < 30 || client.getVarbitValue(11323) < 30) {
                    gameObjectOverlay(hopper, Color.GREEN, null).render(graphics);
                } else {
                    mixerSetup(graphics);
                }
            }
        return null;
    }
    public void workbench(Graphics2D graphics, Color color)
    {
        updateSpriteValues();
        if (spriteID == 5673) {
            gameObjectOverlay(alembic, color,null).render(graphics);
        }
        if (spriteID == 5672) {
            gameObjectOverlay(retort, color,null).render(graphics);
        }
        if (spriteID == 5674) {
            gameObjectOverlay(agitator, color,null).render(graphics);
        }
    }
    public void herb (Graphics2D graphics, String location)
    {
        if (Objects.equals(location, "NW")) {
            gameObjectOverlay(NORTH_WEST_HERB_ID, Color.GREEN, null).render(graphics);
        } else if (Objects.equals(location, "NE")) {
            gameObjectOverlay(NORTH_EAST_HERB_ID, Color.GREEN, null).render(graphics);
        } else if (Objects.equals(location, "SE")) {
            gameObjectOverlay(SOUTH_EAST_HERB_ID, Color.GREEN, null).render(graphics);
        } else if (Objects.equals(location, "SW")) {
            gameObjectOverlay(SOUTH_WEST_HERB_ID, Color.GREEN, null).render(graphics);
        }
    }
    int blueCount=0;
    int greenCount=0;
    int redCount=0;
    public void updateSpriteValues() {
        List<Object> sortedValues = plugin.getSortedPotionValues();
        if (!sortedValues.isEmpty() && sortedValues.size() >= 4) {
            blueCount = (int) sortedValues.get(0) / 10;
            greenCount = (int) sortedValues.get(1) / 10;
            redCount = (int) sortedValues.get(2) / 10;
            spriteID = (int) sortedValues.get(3);
        } else {
            // Reset values or set to defaults if sortedValues is empty
            blueCount = 0;
            greenCount = 0;
            redCount = 0;
            spriteID = 5673;  // Default to alembic sprite ID
        }
    }
    public void mixerSetup(Graphics2D graphics)
    {
        updateSpriteValues();
        int vial1 = client.getVarbitValue(11324);
        int vial2 = client.getVarbitValue(11325);
        int vial3 = client.getVarbitValue(11326);

        int blueVials = countVials(vial1, vial2, vial3, 1);
        int greenVials = countVials(vial1, vial2, vial3, 2);
        int redVials = countVials(vial1, vial2, vial3, 3);

        if (blueCount > 0 && blueVials < blueCount) {
            gameObjectOverlay(moxLever, Color.BLUE,null).render(graphics);
        }
        if (greenCount > 0 && greenVials < greenCount) {
            decorativeObjectOverlay(agaLever, Color.GREEN).render(graphics);
        }
        if (redCount > 0 && redVials < redCount) {
            gameObjectOverlay(lyeLever, Color.RED,null).render(graphics);
        }
    }

    private int countVials(int vial1, int vial2, int vial3, int colorCode) {
        return (vial1 == colorCode ? 1 : 0) + (vial2 == colorCode ? 1 : 0) + (vial3 == colorCode ? 1 : 0);
    }
}
