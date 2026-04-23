// Places you've tried/hit,
// Your boat locations and boat health.

public class Player {
    int[] placesOfShips = new int[Battleship.totalBoatCoordinate];

    public Player() {
        placesOfShips = initPlaces();

    }


    public int[] initPlaces() {
        int lengthOfBoard = Battleship.totalBoatCoordinate;
        

        for (int i=0; i<4; i++) {
            // Random rand = new Random);
            switch (i) {
                
                case 0:
                    
                    int randomInRange = (int) (Math.random() * (lengthOfBoard/2));
                    
                    
                    break;

                case 1:
                    break;

                case 2:
                    break;
                
                case 3:
                    break;

                case 4:
                    break;

                
            }
           


        }




    }


}
