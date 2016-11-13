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
    static final int SIMTIME = ;		//simulating time
    static final int MAXROTATE = 4;
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

    class Board implements Cloneable {

        public int obstacleNum;
        public int board[][];
        public int simulateBoard[][];

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
        	int[][] res = new int[this.board.length][];
        	for (int i = 0; i < res.length; i++) {
        		res[i] = Arrays.copyOf(this.board[i], width);
        	}
        	return res;
        }
        
        public int[][] copySimulateBoard() {
        	int[][] res = new int[this.simulateBoard.length][];
        	for (int i = 0; i < res.length; i++) {
        		res[i] = Arrays.copyOf(this.simulateBoard[i], width);
        	}
        	return res;
        }
        
        @Override
        public Object clone() {
        	try {
        		Board cpy = (Board)super.clone();
        		cpy.obstacleNum = this.obstacleNum;
        		cpy.board = this.copyBoard();
        		cpy.simulateBoard = this.copySimulateBoard();
        		return cpy;
        	} catch (CloneNotSupportedException e) {
        		
        	}
			return board;
        }
    }
    
    class Pack implements Cloneable {
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
    	
    	public int fillObstaclePack(int obstacleNum) {
    		for (int i = 0; i < packSize; i++) {
    			for (int j = 0; j < packSize; j++) {
    				if (obstacleNum > 0 && this.pack[i][j] == EMPTY) {
    					--obstacleNum;
    					this.pack[i][j] = obstacle;
    				}
    			}
    		}
    		return obstacleNum;
    	}
    	
    	@Override
    	public Object clone() {
    		Pack cpy = new Pack();
    		cpy.pack = this.copyPack();
    		return cpy;
    	}
    }
    
    public class AllSearch {
    	private Pack[] packs;
    	private Board board;
    	private int obstacle;
    	public AllSearch(Pack[] p, Board b, int obstacle) {
    		this.packs = p;
    		this.board = b;
    		this.obstacle = obstacle;
    	}
    	
    	public int[][] simulate() {
    		int maxScore = 0;
    		int[] block;
    		int[][] buf = new int[2][SIMTIME];
    		int[][] best = new int[2][SIMTIME];
    		int[][] insurance = new int[2][SIMTIME];
    		for (int i = 0; i < Math.pow(7, SIMTIME); i++) {
    			int[] position = shinsu(i, 7);
    			for (int j = 0; j < Math.pow(MAXROTATE, SIMTIME); j++) {
    				int[] rotate = shinsu(j, MAXROTATE);
    				Board b = (Board) this.board.clone();
    				int ojama = this.obstacle;
    				
    				//copy to buffer
    				for (int x = 0; x < SIMTIME; x++) {
    					buf[0][x] = rotate[x];
    					buf[1][x] = position[x];
    				}
    				
    				for (int k = 0; k < SIMTIME; k++ ) {
    					Pack nowPack = (Pack)this.packs[k].clone();
    					if (ojama > 0)
    						ojama = nowPack.fillObstaclePack(ojama); 
    					nowPack.packRotate(rotate[k]);
    					b.setPack(nowPack, j);
    					block = b.howManyChain();
    					if (b.dangerZone())
    						break;
    					
    					if (!b.dangerZone() && k == SIMTIME - 1){
    	    				for (int x = 0; x < SIMTIME; x++) {
    	    					insurance[0][x] = buf[0][x];
    	    					insurance[1][x] = buf[1][x];
    	    				}
    					}
    					int nowScore = score(block);
    					if (nowScore > maxScore) {
    						maxScore = nowScore;
    						for (int x = 0; x < SIMTIME; x++) {
    							best[0][x] = buf[0][x];
    							best[1][x] = buf[1][x];
    						}
    					}
    				}
    			}
    		}
    		if (maxScore <= 0)
    			return insurance;
			return best;
    	}
    }
    
    public int[] shinsu(int n, int shinsu) {
    	int[] ans = new int[SIMTIME];
    	for (int i = SIMTIME - 1; i >= 0; i--) {
    		ans[i] = n % shinsu;
    		n /= shinsu;
    	}
    	return ans;
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
           int[][] best = new int[2][SIMTIME];
           while (true) { 
                turn = in.nextInt();
                millitime = in.nextLong();
                my = new Board(width, height, in);
                op = new Board(width, height, in);
                int col = 0, rot = 0;
                debug("turn : " + turn);
                Pack[] packs = new Pack[SIMTIME];
                for (int i = 0; i < SIMTIME; i++) {
                	packs[i] = (Pack) pack[turn + i].clone();
                }
                AllSearch search = new AllSearch(packs, my, my.obstacleNum);
                if (turn % SIMTIME == 0 || my.obstacleNum > 0) {
                	best = search.simulate();
                }
                
                rot = best[0][turn % SIMTIME];
                col = best[1][turn % SIMTIME];
                
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
    
    public int score(int[] block) {
    	int sum = 0;
    	for (int i = 0; i < block.length; i++) {
    		sum += (int)(Math.floor(Math.pow(1.3, i)) * Math.floor(block[i] / 2));
    	}
    	return sum;
    }
}


