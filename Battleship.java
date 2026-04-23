// Map, game functionality (where boats are, who is winning/game state, )
// Turns

public class Battleship {
    int[][] map;
    public static final int totalBoatCoordinate = 20;
    Player p1 = new Player();
    Player p2 = new Player();
    public Battleship(int size) {
        map = initMap(size);


    }

    // [[]. []. []]

    private int[][] initMap(int size) {
        return new int[size][size];
    }

    





}






// create a thread (id), 
