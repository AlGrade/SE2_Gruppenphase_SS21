package com.example.se2_gruppenphase_ss21.logic.tetris;

import android.content.res.AssetManager;

import java.util.ArrayList;

/**
 * Representation of MAP
 *
 * !! PLEASE do not make fundamental changes to the logic, without coordinating with the team !!
 *
 * A MAP represents a rectangular field and consists of
 * -> a play field (area where tiles can be placed on)
 * -> surrounding area
 *
 * The functionality of this class includes:
 * -> setting up the play field
 * -> checking if a tile can be placed on a field
 * -> validating if riddle was solved successfully
 *
 * The indices of the map are defined as follows:
 * -> X represents horizontal axis (beginning with 0)
 * -> Y represents vertical axis (beginning with 0)
 *
 *
 * @author Manuel Simon #00326348
 */
public class Map {
    private Box[][] boxMap;
    private ArrayList<Tile> tiles = new ArrayList<>();

    /**
     * Default constructor, creating an empty map
     */
    public Map() {
        super();
    }

    /**
     * Creates a MAP with the given shape.
     * @param field shape of the map.
     */
    public Map(boolean[][] field) {
        setUpMap(field);
    }

    /**
     * Creates a MAP with the shape of a map of the pool.
     * @param mgr AssetManager to read pool file.
     * @param id ID of map to be loaded.
     * @param category category of map (dimension, for example: "5x6")
     */
    public Map(AssetManager mgr, int id, String category) {
        boolean[][] field = StructureLoader.getStructure(mgr, id, "map", category);
        setUpMap(field);
    }

    /**
     * Sets up the play field (area on which tiles are placed).
     * This is done by reading a two-dimensional boolean array, where the first array index represents the y-axis,
     * whereas the second index represents the x-axis.
     * True represents a valid field
     * False represents an invalid field
     *
     * Example:
     * If the desired play field looks like this (O field element - X invalid field element):
     * X X X X
     * X O O X
     * X O O X
     * X X X X
     *
     * this can be achieved by following array:
     * {
     *     { false, false, false, false },
     *     { false, true,  true,  false },
     *     { false, true,  true,  false },
     *     { false, false, false, false }
     * }
     *
     * @param field the setup array - true for a valid field, false for invalid
     */
    public void setUpMap(boolean[][] field) {
        if(field == null)
            field = StructureLoader.getStandardStructure();

        int ySize = field.length;
        int xSize = field[0].length;
        boxMap = new Box[ySize][xSize];

        for(int y=0; y < field.length; y++) {
            for(int x=0; x < field[y].length; x++) {
                Box box = new Box(y,x);
                box.setField(field[y][x]);
                boxMap[y][x] = box;
            }
        }
    }

    /**
     * Covers a specific box on the map with the given TILE.
     * @param tile the tile to cover box with
     * @param x x-coordinate of the box
     * @param y y-coordinate of the box
     */
    void coverBox(Tile tile, int x, int y) {
        boxMap[y][x].setTile(tile);
    }

    /**
     * Clears any tile from the specified box on the map.
     * @param x x-coordinate of the box
     * @param y y-coordinate of the box
     */
    void clearBox(int x, int y) {
        boxMap[y][x].setTile(null);
    }

    /**
     * Checks if a specified BOX on the map is available for placement.
     * @param x x-coordinate of the box
     * @param y y-coordinate of the box
     * @return true if available - false if not
     */
    boolean checkAvailable(int x, int y) {
        // coordinates outside of map
        if(y >= boxMap.length || x >= boxMap[0].length || y < 0 || x < 0)
            return false;

        Box box = boxMap[y][x];
        return box.isAvailable();
    }

    /**
     * Checks if the specified BOX is available for temporary placement.
     * Temporary placement is used to position the TILE. The TILE will not be attached.
     * @param x x-coordinate to check
     * @param y y-coordinate to check
     * @return true if available for temporary placement, otherwise false
     */
    boolean checkAvailableTemp(int x, int y) {
        // coordinates outside of map
        if(y >= boxMap.length || x >= boxMap[0].length || y < 0 || x < 0)
            return false;

        Box box = boxMap[y][x];
        return !box.isCoveredByTile();
    }

    /**
     * Add TILE to attached tiles.
     * @param tile
     */
    void addTile(Tile tile) {
        tiles.add(tile);
    }

    public Tile[] getTiles() {
        Tile[] t = new Tile[tiles.size()];
        tiles.toArray(t);
        return t;
    }

    /**
     * Removes the given TILE from the list of attached TILES.
     * @param tile the TILE to be removed
     * @return true if found and removed, otherwise false
     */
    boolean removeTile(Tile tile) {
        return tiles.remove(tile);
    }

    /**
     * Checks if riddle is solved. Therefore every play field box needs to be covered by a tile.
     * @return true if riddle is solved, otherwise false.
     */
    public boolean checkSolved() {
        for(Box[] line : boxMap) {
            for(Box box : line) {
                if(box.isAvailable())
                    return false;
            }
        }
        return true;
    }

    /**
     * Receive reference to the instance of Box at specified coordinates.
     * A BOX contains information like whether it is part of the play-field or not,
     * if it is covered by a TILE, and if yes - by which.
     *
     * @param x x-coordinate of Box
     * @param y y-coordinate of Box
     * @return The Box at given coordinates.
     */
    public Box getBox(int x, int y) {
        return boxMap[y][x];
    }

    /**
     * Returns the max index of the x-coordinate.
     * @return max index of x-coordinate
     */
    public int getMaxX() {
        if(boxMap == null)
            return 0;
        return boxMap[0].length-1;
    }

    /**
     * Returns the max index of the y-coordinate.
     * @return max index of y-coordinate
     */
    public int getMaxY() {
        if(boxMap == null)
            return 0;
        return boxMap.length-1;
    }

    /**
     * Sets up the this MAP with a map from the pool.
     * The pool is represented by a JSON file (structures.json in assets).
     * It contains all maps of the game.
     *
     * The category stands for the maps dimensions. Standard is "5x6".
     *
     * @param mgr AssetManager needed to read JSON file
     * @param id ID of the map
     * @param category map dimensions (for example "5x6")
     */
    public void setMapByID(AssetManager mgr, int id, String category) {
        boolean[][] field = StructureLoader.getStructure(mgr, id, "map", category);
        setUpMap(field);
    }
}

