package com.example.se2_gruppenphase_ss21.logic.tetris;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

/**
 * Representation of a TILE.
 *
 * !! PLEASE do not make fundamental changes to the logic, without coordinating with the team !!
 *
 * The shape of a TILE is defined by Position data.
 * These positions are relative to a self defined reference point (0,0).
 * For example a vertical I-Element consisting of 3 blocks could look like that:
 * 0,0 - 0,1 - 0,2
 * But the same shape may also be achieved by - just locally displaced:
 * 3,1 - 3,2 - 3,3
 *
 * A TILE always needs to be associated with a map. It also needs a map-hook
 * (absolute position on the map) to be placed on the map.
 *
 * The reference point is used to place the tile on the map. This means, it is the point
 * that will be attached to the hook point of the map (absolute coordinate of the map).
 * To place a TILE centric on the hook point, it is recommended to define the shape as follows:
 * 0,-1 - 0,0 - 0,1
 *
 * Furthermore the TILE can be attached to a map (or be removed).
 * It will check if the placement is valid and attach it if so.
 * You can also check the validity of a placement manually.
 *
 *
 * @author Manuel Simon #00326348
 */
public class Tile {
    private ArrayList<Position> shape = new ArrayList<>();
    private Map map;
    private Position hook;
    private boolean isAttached = false;
    private int color = Color.RED;
    private static final int X_AXIS = 1;
    private static final int Y_AXIS = 2;


    /**
     * Default constructor. Creating an empty TILE.
     */
    public Tile() {
        super();
    }
    /**
     * Initializes a TILE and creates its shape with the given parameters.
     * @param pos Shape coordinates
     */
    public Tile(Position... pos) {
        for(Position p : pos)
            shape.add(p);
    }

    /**
     * Initializes a TILE with the shape of the tile with given ID.
     * @param mgr AssetManager to load pool file
     * @param id ID of TILE in pool
     * @param category category (for example "standard")
     */
    public Tile(AssetManager mgr, int id, String category) {
        setTileByID(mgr, id, category);
    }


    /**
     * Add a point the shape.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public void addPoint(int x, int y) {
        shape.add(new Position(x,y));
    }

    /**
     * Add a position the shape.
     * @param pos
     */
    public void addPoints(Position... pos) {
        for(Position p : pos)
            shape.add(p);
    }

    /**
     * Remove a position from the shape.
     * @param x x-coordinate
     * @param y y-coordinate
     * @return true if removed - false if nothing was removed
     */
    public boolean removePoint(int x, int y) {
        Position pos = new Position(x,y);
        boolean removed = shape.remove(pos);
        while(shape.remove(pos)); // in case there is more than one pos with same coordinates
        return removed;
    }

    /**
     * Description see attachToMap(Position posOnMap)
     * @param x x-coordinate of map-hook
     * @param y y-coordinate of map-hook
     * @return true if placed - false if not
     */
    public boolean attachToMap(Map map, int x, int y) {
        return attachToMap(map, new Position(x,y));
    }

    /**
     * Attaches this tile to the associated map, if possible.
     *
     * It will check if the tile collides with non-eligible parts of the map
     * or with another tile. If so, the tile will not be added, returning false.
     *
     * If the tile can be placed on the map, it will do so by occupying the maps
     * respective boxes (absolute position of the map boxes is calculated by using the hook point).
     *
     * @param posOnMap map-hook
     * @return true if successful - false if not
     */
    public boolean attachToMap(Map map, Position posOnMap) {
        this.map = map;
        // check if tile is allowed to be placed at posOnMap
        if(!checkPlaceable(posOnMap))
            return false;
        // place tile on the map at posOnMap
        boolean added = addTileToMap(posOnMap);
        // register tile on map
        map.addTile(this);
        // tell the tile, it is attached (it is attached, even if added is false)
        isAttached = true;
        return added;
    }

    /**
     * Places the TILE on the MAP WITHOUT attaching it!
     * This is just for positioning purposes.
     *
     * @param map the map to place the TILE on.
     * @param posOnMap the point where to place it on the MAP.
     * @return true if successful, otherwise false.
     */
    public boolean placeOnMap(Map map, Position posOnMap) {
        this.map = map;
        // remove the current placement
        removeTileFromMap();
        if(!checkPlaceableTemp(posOnMap)) {
            // restore old position
            addTileToMap(hook);
            return false;
        }

        // add new placement
        boolean added = addTileToMap(posOnMap);
        return added;
    }

    /**
     * Adds a TILE to the MAP without attaching it.
     * isAttached() will therefore return false.
     * @param posOnMap
     */
    private boolean addTileToMap(Position posOnMap) {
        // this is only relevant to temporary placements
        if(isAttached) {
            Log.e("tile", "Tile needs to be detached before placing!");
            return false;
        }

        hook = posOnMap;
        // place tile on the map at posOnMap
        for(int i=0; i < shape.size(); i++) {
            int x = hook.x + shape.get(i).x;
            int y = hook.y + shape.get(i).y;
            map.coverBox(this, x, y);
        }

        return true;
    }

    /**
     * Detaches this tile from the map.
     */
    public void detachFromMap() {
        if(!isAttached)
            return;

        removeTileFromMap();

        this.map.removeTile(this);
        this.map = null;
        isAttached = false;
    }

    /**
     * Removes the TILE from the map on current POSITION (hook).
     */
    public void removeTileFromMap() {
        if(hook == null)
            return;

        for(int i=0; i < shape.size(); i++) {
            int x = hook.x + shape.get(i).x;
            int y = hook.y + shape.get(i).y;
            map.clearBox(x, y);
        }
    }

    /**
     * Checks if tile can be placed on associated map considering a custom hook point as parameter.
     * @param posOnMap hook point
     * @return true if it can be placed - false if not
     */
    public boolean checkPlaceable(Position posOnMap) {
        if(map == null)
            return false;

        for(Position pos : shape) {
            int x = posOnMap.x + pos.x;
            int y = posOnMap.y + pos.y;
            if(!map.checkAvailable(x,y))
                return false;
        }

        return true;
    }

    /**
     * Checks if tile can be placed temporarily (not attaching) on associated map.
     * NOTE: this check is just for positioning - not attaching!!
     * For checking to attach, use checkPlaceable()
     *
     * @param posOnMap hook point
     * @return true if it can be placed - false if not
     */
    public boolean checkPlaceableTemp(Position posOnMap) {
        if(map == null)
            return false;

        for(Position pos : shape) {
            int x = posOnMap.x + pos.x;
            int y = posOnMap.y + pos.y;
            if(!map.checkAvailableTemp(x,y))
                return false;
        }

        return true;
    }

    /**
     * Moves the current temporary position of the TILE on the MAP upwards by one unit.
     * @return true if it could be placed on new position, otherwise false.
     */
    public boolean moveUp() {
        Position posOnMap = new Position(hook.x, hook.y - 1);
        boolean success = placeOnMap(map, posOnMap);
        if(success)
            hook = posOnMap;
        return success;
    }

    /**
     * Moves the current temporary position of the TILE on the MAP down by one unit.
     * @return true if it could be placed on new position, otherwise false.
     */
    public boolean moveDown() {
        Position posOnMap = new Position(hook.x, hook.y + 1);
        boolean success = placeOnMap(map, posOnMap);
        if(success)
            hook = posOnMap;
        return success;
    }

    /**
     * Moves the current temporary position of the TILE on the MAP right by one unit.
     * @return true if it could be placed on new position, otherwise false.
     */
    public boolean moveRight() {
        Position posOnMap = new Position(hook.x + 1, hook.y);
        boolean success = placeOnMap(map, posOnMap);
        if(success)
            hook = posOnMap;
        return success;
    }

    /**
     * Moves the current temporary position of the TILE on the MAP left by one unit.
     * @return true if it could be placed on new position, otherwise false.
     */
    public boolean moveLeft() {
        Position posOnMap = new Position(hook.x - 1, hook.y);
        boolean success = placeOnMap(map, posOnMap);
        if(success)
            hook = posOnMap;
        return success;
    }

    /**
     * Rotates the TILE clockwise.
     * Has no effect if TILE is already attached to a MAP.
     */
    public void rotateRight() {
        if(shape.size() == 0 || isAttached)
            return;

        invertY();
        switchAxis();
    }

    /**
     * Rotates the TILE counter-clockwise.
     * Has no effect if TILE is already attached to a MAP.
     */
    public void rotateLeft() {
        if(shape.size() == 0 || isAttached)
            return;

        invertX();
        switchAxis();
    }

    /**
     * Mirrors the TILE vertically
     */
    public void mirrorVertically() {
        if(shape.size() == 0 || isAttached)
            return;

        invertY();
    }

    /**
     * Mirrors the TILE horizontally
     */
    public void mirrorHorizontally() {
        if(shape.size() == 0 || isAttached)
            return;

        invertX();
    }

    /**
     * Switches the axis.
     */
    private void switchAxis() {
        for(Position pos : shape) {
            int tmp = pos.x;
            pos.x = pos.y;
            pos.y = tmp;
        }
    }

    /**
     * Inverts the x-axis coordinates of the shape Positions
     */
    private void invertX() {
        int max = shape.get(0).x;
        int min = max;
        for (Position pos : shape) {
            max = Math.max(pos.x, max);
            min = Math.min(pos.x, min);
        }

        for(Position pos : shape) {
            int upperDiff = max - pos.x;
            int lowerDiff = pos.x - min;
            pos.x = upperDiff < lowerDiff ? min + upperDiff : max - lowerDiff;
        }
    }

    /**
     * Inverts the y-axis coordinates of the shape Positions
     */
    private void invertY() {
        int max = shape.get(0).y;
        int min = max;
        for (Position pos : shape) {
            max = Math.max(pos.y, max);
            min = Math.min(pos.y, min);
        }

        for(Position pos : shape) {
            int upperDiff = max - pos.y;
            int lowerDiff = pos.y - min;
            pos.y = upperDiff < lowerDiff ? min + upperDiff : max - lowerDiff;
        }
    }

    /**
     * Sets the absolute position of the map on which this TILE is inserted (with shape Position 0,0).
     * @param hook absolute position of map where tile is placed
     */
    void setHook(Position hook) {
        this.hook = hook;
    }

    /**
     * Get the absolute position of the MAP where tile will be placed (with shape Position 0,0).
     * @return the absolute hook position of the MAP.
     */
    public Position getHook() {
        return hook;
    }

    /**
     * Set the map associated with this tile.
     * This will detach the current TILE from the map of course.
     * @param map the map associated with this tile
     */
    public void setMap(Map map) {
        this.map = map;
        isAttached = false;
    }

    /**
     * Returns the map associated with this tile.
     * @return The map associated with this tile
     */
    public Map getMap() {
        return this.map;
    }

    /**
     * Returns an array of this tiles shape.
     * @return shape array
     */
    public Position[] getShape() {
        Position[] s = new Position[shape.size()];
        shape.toArray(s);
        return s;
    }

    /**
     * Indicates if tile is placed on the map.
     * @return true if placed on a map - false if not.
     */
    public boolean isAttached() {
        return isAttached;
    }

    /**
     * Sets up the TILE with a tile from the pool.
     * The pool is represented by a JSON file containing all the structures.
     *
     * @param mgr AssetManager needed to read JSON file
     * @param id ID of tile in pool
     * @param category tile category (default: "standard")
     */
    public void setTileByID(AssetManager mgr, int id, String category) {
        if(!shape.isEmpty())
            shape = new ArrayList<>();

        boolean[][] shape = StructureLoader.getStructure(mgr, id, "tile", category);
        int midY = shape.length / 2;
        int midX = shape[0].length / 2;
        for(int y=0; y < shape.length; y++) {
            for(int x=0; x < shape[y].length; x++) {
                if(shape[y][x])
                    addPoint(x-midX,y-midY);
            }
        }
    }

    /**
     * Centers a tile symmetrically around 0,0 so that it can be placed centric
     * around the insertion point on a MAP.
     */
    public void centerTile() {
        if(shape.isEmpty())
            return;

        int maxX = shape.get(0).x;
        int minX = maxX;
        int maxY = shape.get(0).y;
        int minY = maxY;
        for(Position val : shape) {
            maxX = Math.max(maxX, val.x);
            minX = Math.min(minX, val.x);
            maxY = Math.max(maxY, val.y);
            minY = Math.min(minY, val.y);
        }

        int diffX = (maxX < 0) ? (minX * -1) - (maxX * -1) : maxX - minX;
        int diffY = (maxY < 0) ? (minY * -1) - (maxY * -1) : maxY - minY;

        int offsetX = (maxX - diffX / 2) * -1;
        int offsetY = (maxY - diffY / 2) * -1;

        for(Position val : shape) {
            val.x = val.x + offsetX;
            val.y = val.y + offsetY;
        }
    }

    /**
     * Gets the color of this TILE. (Use android.graphics.Color class)
     * @return color of this TILE
     */
    public int getColor() {
        return color;
    }

    /**
     * Sets the Color of this TILE. (use android.graphics.Color class)
     * @param color the color to set.
     */
    public void setColor(int color) {
        this.color = color;
    }
}

