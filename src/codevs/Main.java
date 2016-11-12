package codevs;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        new Main().run();
    }

    static final String AI_NAME = "allserach";

    static final int EMPTY = 0;

    Random random = new Random();

    int turn = -1;

    Pack[] pack;

    int width;

    int height;

    int packSize;

    int summation;

    int obstacle;

    int maxTurn;

    long millitime;

    Board my;

    Board op;

    class Board {

        int obstacleNum;

        int board[][];
        
        int simulateBoard[][];

        public Board(int width, int height, Scanner in) {
            obstacleNum = in.nextInt();
            board = new int[height][width];
            for (int i = 0; i < height; ++i) {
                for (int j = 0; j < width; ++j) {
                    board[i][j] = in.nextInt();
                }
            }
            in.next();
            this.makeSimulateBoard();
        }
        
        private void makeSimulateBoard() {
        	this.simulateBoard = new int[height + packSize][width];
        	for (int i = packSize; i < this.simulateBoard.length; i++) {
        		for (int j = 0; j < width; j++) {
        			this.simulateBoard[i][j] = this.board[i - packSize][j];
        		}
        	}
        }
    }
    
    class Pack {
    	int[][] pack = new int[width][height];
    	
    	public void packRotate(int rot) {
    		int[][] res = this.copyPack();
    		for (int i = 0; i < rot; i++) {
    			this.rot1();
    		}
    	}
    	
    	private void rot1() {
    		int[][] res = this.copyPack();
    		for (int i = 0; i < packSize; ++i) {
    			for (int j = 0; j < packSize; ++j) {
    				res[j][packSize - i - 1] = pack[i][j];
    			}
    		}
    	}
    	
    	//return myself copy
    	public int[][] copyPack() {
    		int[][] res = new int[packSize][];
    		for (int i = 0; i < packSize; ++i) {
    			res[i] = Arrays.copyOf(this.pack[i], packSize);
    		}
    		return res;
    	}
    }
    
    int[][] rot1(int[][] pack) {
        int[][] res = copyPack(pack);
        for (int i = 0; i < packSize; ++i) {
            for (int j = 0; j < packSize; ++j) {
                res[j][packSize - i - 1] = pack[i][j];
            }
        }
        return res;
    }
    
    int[][] copyPack(int[][] pack) {
        int[][] res = new int[packSize][];
        for (int i = 0; i < packSize; ++i) {
            res[i] = Arrays.copyOf(pack[i], packSize);
        }
        return res;
    }
    
    void run() {
        println(AI_NAME);
        try (Scanner in = new Scanner(System.in)) {
            width = in.nextInt();
            height = in.nextInt();
            packSize = in.nextInt();
            summation = in.nextInt();
            obstacle = summation + 1;
            maxTurn = in.nextInt();
            pack = new Pack[maxTurn];
            for (int i = 0; i < maxTurn; ++i) {
            	pack[i] = new Pack();
                for (int j = 0; j < packSize; ++j) {
                    for (int k = 0; k < packSize; ++k) {
                        pack[i].pack[j][k] = in.nextInt();
                    }
                }
                in.next();
           }
           while (true) { 
                turn = in.nextInt();
                millitime = in.nextLong();
                my = new Board(width, height, in);
                op = new Board(width, height, in);

                debug("turn : " + turn);

                int rot = random.nextInt(4);

                this.pack[turn].pack = fillObstaclePack(this.pack[turn].pack, my.obstacleNum);
                this.pack[turn].packRotate(rot);
                int left = 0, right = width - packSize;
                int pack[][] = this.pack[turn].pack;

                bad:
                for (int i = 0; i < packSize; ++i) {
                    for (int j = 0; j < packSize; ++j) {
                        if (pack[j][i] != EMPTY)
                            break bad;
                    }
                    --left;
                }
                bad:
                for (int i = 0; i < packSize; ++i) {
                    for (int j = packSize - 1; j >= 0; --j) {
                        if (pack[j][i] != EMPTY)
                            break bad;
                    }
                    ++right;
                }

                int col = random.nextInt(right - left) + left;
                println(col + " " + rot);
            }
        }
    }





    int[][] fillObstaclePack(int[][] pack, int obstacleNum) {
        int[][] res = copyPack(pack);
        for (int i = 0; i < packSize; ++i) {
            for (int j = 0; j < packSize; ++j) {
                if (obstacleNum > 0 && res[i][j] == EMPTY) {
                    --obstacleNum;
                    res[i][j] = obstacle;
                }
            }
        }
        return res;
    }



    void println(String msg) {
        System.out.println(msg);
        System.out.flush();
    }

    void debug(String msg) {
        System.err.println(msg);
        System.err.flush();
    }
}


