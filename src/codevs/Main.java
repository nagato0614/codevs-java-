package codevs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.awt.List;
import java.lang.Math;

public class Main {
	
    public static void main(String[] args) {
        new Main().run();
    }
    
    static final String AI_NAME = "allserach";
    static final int EMPTY = 0;
    static final int SIMTIME = 3;		//simulating time
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
        
        public void showSimulateBoard() {
        	for (int i = 0; i < this.simulateBoard.length; i++) {
        		System.err.printf("[");
        		for (int j = 0; j < width; j++) {
        			System.err.printf("%d, ", this.simulateBoard[i][j]);
        		}
        		System.err.printf(" ]\n");
        	}
        }
        
        public void setPack(Pack p, int setPos) {
        	for (int i = 0; i < packSize; i++) {
        		for (int j = 0; j < packSize; j++) {
        			this.simulateBoard[i][j + setPos] = p.pack[i][j];
        		}
        	}
        	this.fallBlock();
        }
        
        public int[] howManyChain() {
        	int sum = 0;
        	ArrayList<Integer> block = new ArrayList<Integer>(1);
        	block.add(this.deleteBlock());
        	while (block.get(sum) > 0) {
        		sum++;
        		this.fallBlock();
        		block.add(this.deleteBlock());
        	}
        	int[] b = new int[block.size()];
        	for (int i = 0; i < block.size(); i ++) {
        		b[i] = block.get(i);
        	}
        	return b;
        }
        
        public void fallBlock() {
        	while (true) {
        		int sum = 0;
        		for (int i = this.simulateBoard.length - 1; i > 0; i--) {
        			for (int j = 0; j < width; j++) {
        				if (this.simulateBoard[i][j] == 0 && this.simulateBoard[i - 1][j] > 0) {
        					sum++;
        					this.simulateBoard[i][j] = this.simulateBoard[i - 1][j];
        					this.simulateBoard[i - 1][j] = 0;
        				}	
        			}
        		}
        		if (sum == 0)
        			break;
        	}
        }
        
        public int deleteBlock() {
        	int deleteCount = 0;
        	int sum = 0;
        	boolean[] flag = new boolean[4];
        	for (int i = this.simulateBoard.length - 1; i > 0; i--) {
        		for (int j = 0; j < width; j++) {
        			//when there is no block or obstacle
        			if (this.simulateBoard[i][j] == 0 || this.simulateBoard[i][j] == obstacle) 
        				continue;
        			
        			//reset flag
        			for (int k = 0; k < flag.length; k++) 
        				flag[k] = true;
        			
        			for (int x = 0; x <= summation; x++) {
        				//horizontal
        				if (j + (x - 1) < width && flag[0]) {
        					sum = 0;
        					for (int k = 0; k < x; k++) {
        						if (this.simulateBoard[i][j + k] != 0) {
        								sum += Math.abs(this.simulateBoard[i][j + k]);
        						} else {
        							flag[0] = false;
        							break;
        						}
        						if (sum > summation)
        							break;
        					}
        					if (flag[0] && sum == summation) {
        						for (int k = 0; k < x; k++) {
        							deleteCount++;
        							this.simulateBoard[i][j + 1] = -Math.abs(this.simulateBoard[i][j + 1]);
        						}
        					}
        				}
        				
        				//vertical
        				if (i - (x - 1) > 0 && flag[1]) {
        					sum = 0;
        					for (int k = 0; k < x; k++) {
        						if (this.simulateBoard[i - k][j] != 0) {
        							sum += Math.abs(this.simulateBoard[i - k][j]);
        						} else {
        							flag[1] = false;
        							break;
        						}
        						if (sum > summation)
        							break;
        					}
        					if (sum == summation) {
        						for (int k = 0; k < x; k++) {
        							deleteCount++;
        							this.simulateBoard[i - k][j] = -Math.abs(this.simulateBoard[i - k][j]);
        						}
        					}
        				}
        				
        				//diagonally to the right
        				if (flag[2] && i - (x - 1) > 0 && j + (x - 1) < width) {
        					sum = 0;
        					for (int k = 0; k < x; k++) {
        						if (this.simulateBoard[i - k][j + k] != 0) {
        							sum += Math.abs(this.simulateBoard[i - k][j + k]);
        						} else {
        							flag[2] = false;
        							break;
        						}
        						if (sum > summation)
        							break;
        					}
        					if (sum == summation) {
        						for (int k = 0; k < x; k++) {
        							deleteCount++;
        							this.simulateBoard[i - k][j + k] = -Math.abs(this.simulateBoard[i - k][j + k]);
        						}
        					}
        				}
        				
        				//diagonally to the left
        				if (flag[3] && i - (x - 1) > 0 && j - (x - 1) >= 0) {
        					sum = 0;
        					for (int k = 0; k < x; k++) {
        						if (this.simulateBoard[i - k][j - k] != 0) { 
        							sum += Math.abs(this.simulateBoard[i - k][j - k]);
        						} else {
        							flag[3] = false;
        							break;
        						}
        						if (sum > summation)
        							break;
        					}
        					if (sum == summation) {
        						for (int k = 0; k < x; k++) {
        							deleteCount += 1;
        							this.simulateBoard[i - k][j - k] = -Math.abs(this.simulateBoard[i - k][j - k]);
        						}
        					}
        				}
        			}
        		}
        	}
        	for (int i = 0; i < this.simulateBoard.length; i++) {
        		for (int j = 0; j < width; j ++) {
        			if (this.simulateBoard[i][j] < 0) {
        				this.simulateBoard[i][j] = EMPTY;
        			}
        		}
        	}
        	return deleteCount;
        }
        
        public boolean dangerZone() {
        	for (int i = 0; i < packSize; i++) {
        		for (int j = 0; j < width; j++) {
        			if (this.simulateBoard[i][j] != 0)
        				return true;
        		}
        	}
        	return false;
        }
        
        public int[][] copyBoard() {
        	int[][] res = new int[this.simulateBoard.length][];
        	for (int i = 0; i < res.length; i++) {
        		res[i] = Arrays.copyOf(this.simulateBoard[i], width);
        	}
        	return res;
        }
    }
    
    class Pack {
    	int[][] pack = new int[width][height];
    	
    	public void packRotate(int rot) {
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
    	
    	public void fillObstaclePack(int obstacleNum) {
    		for (int i = 0; i < packSize; i++) {
    			for (int j = 0; j < packSize; j++) {
    				if (obstacleNum > 0 && this.pack[i][j] == EMPTY) {
    					--obstacleNum;
    					this.pack[i][j] = obstacle;
    				}
    			}
    		}
    	}
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

                this.pack[turn].fillObstaclePack(my.obstacleNum);
                this.pack[turn].packRotate(rot);
                int left = 0, right = width - packSize;
                int pack[][] = this.pack[turn].pack;

                /*
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
                */

                int col = random.nextInt(8);
                println(col + " " + rot);
            }
        }
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


