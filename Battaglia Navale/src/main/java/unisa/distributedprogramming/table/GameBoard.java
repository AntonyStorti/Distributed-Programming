package unisa.distributedprogramming.table;
import java.util.HashMap;
import java.util.Map;


public class GameBoard {

    public final static int NUMROWS = 10, NUMCOLS = 11;

    private Map <String, Integer> ships;
    private char[][] matrix = new char[NUMROWS][NUMCOLS];

    private boolean placementCompleted = false;
    private int hits;
    private int totHits;

    /**
     * Popola la matrice.
     */
    public GameBoard() {

        ships = new HashMap <> ();
        ships.put("AircraftCarrier", 5);
        ships.put("Battleship", 4);
        ships.put("Destroyer", 3);
        ships.put("Submarine", 3);
        ships.put("PatrolBoat", 2);

        for(int i=0; i<NUMROWS; i++)
            for(int j=0; j<NUMCOLS; j++)
                matrix[i][j] = '-';

        hits = 0;
        totHits = 0;

    }

    /**
     * Permette di posizionare una nave nel tabellone di gioco.
     * @param name
     * @param startRow
     * @param startCol
     * @param length
     * @param orientation
     * @return
     */
    public boolean placeShip(String name, int startRow, char startCol, int length, String orientation) {

        if (ships.containsKey(name)) {

            ships.remove(name);

            insertShip(startRow, startCol, length, orientation);

            if (ships.size()<=0) placementCompleted = true;

            return true;

        }else {

            System.out.println("Nave non valida");

            return false;

        }
    }

    /**
     * Metodo di utilità per inserire una nave.
     * @param startRow
     * @param startCol
     * @param length
     * @param orientation
     */
    private void insertShip(int startRow, char startCol, int length, String orientation) {

        int rowIndex = convertRowToIndex(startRow), colIndex = convertCharToIndex(startCol);

        if(orientation.equals("horizontal")) {

            for(int i=colIndex; i<colIndex+length; i++)
                if (matrix[rowIndex][i] != 'S') {
                    matrix[rowIndex][i] = 'S';
                    totHits++;
                }

        } else if (orientation.equals("vertical")) {

            for(int i=rowIndex; i<rowIndex+length; i++)
                if (matrix[i][colIndex] != 'S') {
                    matrix[i][colIndex] = 'S';
                    totHits++;
                }

        }

    }

    /**
     * Permette di effettuare una mossa e restituisce il risultato come stringa.
     * @param col
     * @param row
     * @return
     */
    public String makeMove(char col, int row) {

        int rowIndex = convertRowToIndex(row), colIndex = convertCharToIndex(col);

        if (matrix[rowIndex][colIndex] == 'S') {
            matrix[rowIndex][colIndex] = 'X';
            hits++;
            return "hit";
        } else if (matrix[rowIndex][colIndex] == '-') {
            matrix[rowIndex][colIndex] = '0';
            return "miss";
        }

        return "invalid";
    }

    private int convertRowToIndex(int row) {
        return NUMROWS - row - 1;
    }

    private int convertCharToIndex(char col) {
        return col - 'a';
    }

    public int getTotHits() {
        return totHits;
    }

    public boolean isPlacementCompleted() {
        return placementCompleted;
    }

    public boolean isLoser() {
        return (hits == totHits);
    }

}