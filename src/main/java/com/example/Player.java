package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

// Places you've tried/hit,
// Your boat locations and boat health.

// Save initial coordiante of boat instead of each boat value individually.
public class Player {
    int[] placesOfShips = new int[Battleship.totalBoatCoordinate];

    boolean isPlayerOne;
    public Player() {
        // Default constructor
    }

    public Player(boolean playerOne) {
        isPlayerOne = playerOne;
    }

    private void placeShipV(int[][] board, int row, int col, int len, int val) {
        for (int i = 0; i < len && row + i < 10; i++) {
            board[row + i][col] = val;
        }
    }

    private void placeShipH(int[][] board, int row, int col, int len, int val) {
        for (int i = 0; i < len && col + i < 10; i++) {
            board[row][col + i] = val;
        }
    }

    private boolean canPlaceShip(int[][] board, int row, int col, int len, boolean horizontal) {
        if (horizontal) {
            if (col + len > 10) return false;
            for (int i = 0; i < len; i++) {
                if (board[row][col + i] != Battleship.WATER) return false;
            }
        } else {
            if (row + len > 10) return false;
            for (int i = 0; i < len; i++) {
                if (board[row + i][col] != Battleship.WATER) return false;
            }
        }
        return true;
    }

    public void placeAllShips(int[][] board) {
        int[] shipLengths = {5, 4, 3, 3, 2};
        for (int len : shipLengths) {
            boolean placed = false;
            while (!placed) {
                int row = (int) (Math.random() * 10);
                int col = (int) (Math.random() * 10);
                boolean horizontal = Math.random() < 0.5;
                if (canPlaceShip(board, row, col, len, horizontal)) {
                    if (horizontal) {
                        placeShipH(board, row, col, len, Battleship.SHIP);
                    } else {
                        placeShipV(board, row, col, len, Battleship.SHIP);
                    }
                    placed = true;
                }
            }
        }
    }


    public void initPlaces(int[][] board) {
        placeAllShips(board);
    }


}
