package com.example;

// Map, game functionality (where boats are, who is winning/game state, )
// Turns
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class Battleship {
    public int[][] mapOne;
    public int[][] mapTwo;
    static final int WATER    = 0;
    static final int SHIP     = 1;
    static final int HIT      = 2;
    static final int MISS     = 3;
    public static final int totalBoatCoordinate = 10;
    Player p1 = new Player();
    Player p2 = new Player();
    public Battleship(int size) {
        mapOne = initMap(size);
        mapTwo = initMap(size);

        p1.initPlaces(mapOne);
        p2.initPlaces(mapTwo);
    }

    private int[][] initMap(int size) {
        return new int[size][size];
    }


    // Shooting Logic
    public boolean shoot_grid(int row, int col, boolean isPlayerOne) {
        
        if (isPlayerOne) {

            int thingHit = mapTwo[row][col];
            switch (thingHit) {
                case WATER:
                    mapTwo[row][col] = MISS;
                    return false;
                case SHIP: 
                    mapTwo[row][col] = HIT;
                    return true;
            }

        } else if (!isPlayerOne) {

            int thingHit = mapOne[row][col];
            switch (thingHit) {
                case WATER:
                    mapOne[row][col] = MISS;
                    return false;
                case SHIP: 
                    mapOne[row][col] = HIT;
                    return true;
            }
        }
        

        
        return false;
        
        
        




    } 




    // Returns the game state of the function.
    public String getGameState(boolean isPlayerOne) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();

        // Serialize maps
        rootNode.putPOJO("mapOne", mapOne);
        rootNode.putPOJO("mapTwo", mapTwo);
        
        // Add metadata
        rootNode.put("isPlayerOne", isPlayerOne);
        // We could add more like whose turn it is
        
        try {
            return mapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            return "{}";
        }
    }
    


    


    

    





}






// create a thread (id), 
