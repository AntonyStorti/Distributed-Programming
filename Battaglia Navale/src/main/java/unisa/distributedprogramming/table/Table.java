package unisa.distributedprogramming.table;
import java.util.HashMap;
import java.util.Map;


public class Table {

    private Map<Player, GameBoard> table = new HashMap<>();
    private Player player1, player2;

    public Table(Player user1, Player user2) {
        player1 = user1;
        player2 = user2;
        table.put(user1, new GameBoard());
        table.put(user2, new GameBoard());
    }

    public GameBoard getGameBoard(Player player) {
        return table.get(player);
    }

    public Player getOther(Player sender) {
        if (sender.equals(player1))
            return player2;
        else
            return player1;
    }

    @Override
    public String toString() {
        return "Table [player1=" + player1 + ", player2=" + player2 + "]";
    }

}
