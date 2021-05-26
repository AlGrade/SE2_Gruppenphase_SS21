package com.example.se2_gruppenphase_ss21.logic.tetris;

import org.junit.Test;

public class MapAndTileTests {

    /**
     * visual test
     */
    @Test
    public void testMapAndTile() {
        Map map = new Map();
        boolean[][] field = {
                {false, true,  true,  false, false},
                {false, true,  true,  true,  false},
                {false, true,  true,  true,  false},
                {false, true,  true,  true,  false},
                {false, false, false, true,  false},
                {false, false, false, true,  false},

        };
        /*
        boolean[][] field = {
                { true, true, true, true, true },
                { true, true, true, true, true },
                { true, true, true, true, true },
                { true, true, true, true, true },
                { true, true, true, true, true },
                { true, true, true, true, true },

        };
        */

        map.setUpMap(field);

        Position p1 = new Position(1, 0);
        Position p2 = new Position(0, 0);
        Position p3 = new Position(0, 1);
        Position p4 = new Position(0, 2);
        Tile tile = new Tile(p1, p2, p3, p4);
        Tile tile2 = new Tile(p2, new Position(1, 0));
        Tile tile3 = new Tile(new Position(2, 0), new Position(2, 2), new Position(3, 2),
                new Position(2, 3), new Position(3, 3), new Position(3, 4), new Position(3, 5));

        tile.attachToMap(map, 1, 1);
        //tile2.attachToMap(2,1);
        //tile3.attachToMap(0,0);

        displayMap(map);
        System.out.println("*************");
        tile.detachFromMap();
        tile.rotateRight();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println("*************");
        tile.detachFromMap();
        tile.rotateRight();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println("*************");
        tile.detachFromMap();
        tile.rotateRight();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println("*************");
        tile.detachFromMap();
        tile.rotateRight();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println(map.checkSolved());
        tile.detachFromMap();
        tile.rotateLeft();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println(map.checkSolved());
        tile.detachFromMap();
        tile.rotateRight();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println(map.checkSolved());
        tile.detachFromMap();
        tile.mirrorVertically();
        tile.attachToMap(map, 1, 1);
        displayMap(map);
        System.out.println(map.checkSolved());
        tile.detachFromMap();
        tile.mirrorHorizontally();
        System.out.println("first attachment: "+tile.attachToMap(map, 1, 1));
        displayMap(map);
        System.out.println(map.checkSolved());

        Tile collide = new Tile(new Position(0,0),
                new Position(1,0));

        System.out.println("Does this work? "+collide.attachToMap(map, new Position(1,0)));
        displayMap(map);
        System.out.println(map.checkSolved());

        Tile temp = new Tile(new Position(0,0), new Position(0,1), new Position(0,2));
        System.out.println("added: "+temp.placeOnMap(map, new Position(3,1)));
        displayMap(map);
        System.out.println(map.checkSolved());
        System.out.println("moved: "+temp.moveDown());
        displayMap(map);
        System.out.println(map.checkSolved());

        for (Position pos : tile.getShape()) {
            System.out.print(pos.x + ", ");
            System.out.print(pos.y + "\n");
        }
    }

    private static void displayMap(Map map) {
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                if (map.getBox(x, y).isField() && map.getBox(x, y).getTile() != null)
                    System.out.print("I ");
                else if (map.getBox(x, y).isField())
                    System.out.print("O ");
                else
                    System.out.print("X ");
            }
            System.out.println();
        }
    }
}
