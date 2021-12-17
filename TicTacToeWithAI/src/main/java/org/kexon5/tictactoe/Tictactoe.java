package org.kexon5.tictactoe;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class Tictactoe {
    private final String X_WINS = "X wins";
    private final String O_WINS = "O wins";
    private final String MENU_START = "start";
    private final String MENU_EXIT = "exit";
    private final Scanner sc = new Scanner(System.in);
    private final Random rd = new Random();

    private String table;
    private boolean whoseMove;
    private int moveRemains;
    private List<Consumer<String>> playersStrategy = new ArrayList<>();
    private Function<String, Integer> typePlayer;
    private final int[] whoPlayer = new int[2];
    private final int[][] waysToWin = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, //hor
                                       {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, //ver
                                       {0, 4, 8},{2, 4, 6}}; //diag

    Tictactoe() {
        init();
    }

    private void init() {
        initNewGame();

        playersStrategy.add(x->getMove());
        playersStrategy.add(x->getMoveEasyRobot(true));
        playersStrategy.add(x->getMoveMediumRobot());
        playersStrategy.add(x->getMoveHardRobot());
        typePlayer = x -> x.equals("hard") ? 3 : x.equals("medium") ? 2 : x.equals("easy") ? 1 : 0;
    }

    private void initNewGame() {
        table = "         ";
        moveRemains = table.length();
        whoseMove = true; // first X
    }

    public void start() {
        String[] input;
        while (true) {
            initNewGame();
            System.out.print("Input command: > ");
            input = sc.nextLine().split(" ");
            if (input.length == 3 && input[0].equals(MENU_START) &&
                    (input[1].equals("user") || input[1].equals("easy") || input[1].equals("medium") || input[1].equals("hard")) &&
                    (input[2].equals("user") || input[2].equals("easy") || input[2].equals("medium") || input[2].equals("hard"))) {
                whoPlayer[0] = typePlayer.apply(input[1]);
                whoPlayer[1] = typePlayer.apply(input[2]);
                run();
            } else if (input.length == 1 && input[0].equals(MENU_EXIT)) {
                break;
            } else {
                System.out.println("Bad parameters!");
            }
        }
    }

    private void run() {
        while (true) {
            printCurrentTable();
            int res = checkWins(null);
            if (res == 0) {
                if (moveRemains != 0) {
                    playersStrategy.get(whoPlayer[whoseMove ? 0 : 1]).accept("");
                    whoseMove = !whoseMove;
                } else {
                    System.out.println("Draw");
                    break;
                }
            } else {
                System.out.println((res == 1) ? X_WINS : O_WINS);
                break;
            }
        }
    }

    private void getMoveEasyRobot(boolean print) {
        if (print)
            System.out.println("Making move level \"easy\"");
        while(true) {
            int pos = rd.nextInt(9);
            if (table.charAt(pos) == ' ') {
                table = table.substring(0, pos) + (whoseMove ? "X" : "O") + table.substring(pos + 1);
                moveRemains--;
                break;
            }
        }
    }

    private void getMoveMediumRobot() {
        System.out.println("Making move level \"medium\"");
        for (int[] indexes: waysToWin) {
            int[] counts = { 0, 0 };
            int index = 0;
            for (int ind: indexes) {
                if (table.charAt(ind) == 'O') {
                    counts[1]++;
                } else if (table.charAt(ind) == 'X') {
                    counts[0]++;
                } else {
                    index = ind;
                }
            }
            if (counts[0] == 2 && counts[1] == 0 || counts[0] == 0 && counts[1] == 2) {
                table = table.substring(0, index) + (whoseMove ? "X" : "O") + table.substring(index + 1);
                moveRemains--;
                return;
            }
        }
        getMoveEasyRobot(false);
    }

    private void getMoveHardRobot() {
        System.out.println("Making move level \"hard\"");
        int index = minimax(table, whoseMove ? 'X' : 'O').getFirst();
        table = table.substring(0, index) + (whoseMove ? "X" : "O") + table.substring(index + 1);
        moveRemains--;
    }

    private void printCurrentTable() {
        System.out.println("---------");
        for (int i = 0; i < 3; i++) {
            System.out.println("| " + table.charAt(3 * i) + " " + table.charAt(3 * i + 1) + " " + table.charAt(3 * i + 2) + " |");
        }
        System.out.println("---------");
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void getMove() {
        int moveRemainsLast = moveRemains;

        System.out.print("Enter the coordinates: > ");
        String[] str = sc.nextLine().split(" ");
        if (str.length == 2 && isNumeric(str[0]) && isNumeric(str[1])) {
            int x = Integer.parseInt(str[0]) - 1;
            int y = Integer.parseInt(str[1]) - 1;
            if (checkRange(x) && checkRange(y)) {
                if (table.charAt(3 * x + y) == ' ') {
                    table = table.substring(0, 3 * x + y) + (whoseMove ? "X" : "O") + table.substring(3 * x + y + 1);
                    moveRemains--;
                } else {
                    System.out.println("This cell is occupied! Choose another one!");
                }
            } else {
                System.out.println("Coordinates should be from 1 to 3!");
            }
        } else {
            System.out.println("You should enter numbers!");
        }
        if (moveRemainsLast == moveRemains) {
            getMove();
        }
    }

    private boolean checkRange(int pos) {
        return pos >= 0 && pos <= 2;
    }

    private int checkWins(String currentTable) {
        String selectTable = currentTable != null ? currentTable : table;
        char[] parts = { 'X', 'O' };
        for(char part: parts) {
            for (int[] indexes: waysToWin) {
                if (selectTable.charAt(indexes[0]) == part && selectTable.charAt(indexes[1]) == part && selectTable.charAt(indexes[2]) == part) {
                    return (part == 'X') ? 1 : -1;
                }
            }
        }
        return 0;
    }

    private List<Integer> getAvailSpots(String table) {
        List<Integer> availSpots = new ArrayList<>();
        for (int i = 0; i < table.length(); i++) {
            if (table.charAt(i) != 'X' && table.charAt(i) != 'O') {
                availSpots.add(i);
            }
        }
        return availSpots;
    }

    private Pair minimax(String tableNow, Character playerSign) {
        List<Integer> availSpots = getAvailSpots(tableNow);
        if (checkWins(tableNow) == 1){
            return new Pair(-100, whoseMove ? 10 : -10);
        }
        else if (checkWins(tableNow) == -1){
            return new Pair(-100, whoseMove ? -10 : 10);
        }
        else if (availSpots.isEmpty()){
            return new Pair(-100, 0);
        }
        Map<Integer, Integer> mapMove = new HashMap<>();
        for (int i: availSpots) {
            tableNow = tableNow.substring(0, i) + playerSign + tableNow.substring(i + 1);
            int result = minimax(tableNow, playerSign == 'X' ? 'O' : 'X').getSecond();
            tableNow = tableNow.substring(0, i) + " " + tableNow.substring(i + 1);
            mapMove.put(i, result);
        }
        int bestScore;
        Pair pair = null;
        if (playerSign == (whoseMove ? 'X' : 'O')) {
            bestScore = -10000;
            for (Map.Entry<Integer, Integer> entry: mapMove.entrySet()) {
                if (entry.getValue() > bestScore) {
                    bestScore = entry.getValue();
                    pair = new Pair(entry);
                }
            }
        } else {
            bestScore = 10000;
            for (Map.Entry<Integer, Integer> entry: mapMove.entrySet()) {
                if (entry.getValue() < bestScore) {
                    bestScore = entry.getValue();
                    pair = new Pair(entry);
                }
            }
        }
        return pair;
    }

    private class Pair {
        private final int first;
        private final int second;

        private Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        private Pair(Map.Entry<Integer, Integer> entry) {
            this.first = entry.getKey();
            this.second = entry.getValue();
        }

        private int getFirst() { return first; }

        private int getSecond() { return second; }
    }
}
