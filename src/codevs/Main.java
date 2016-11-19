package codevs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.lang.Math;

public class Main {
	
    public static void main(String[] args) {
        new Main().run();
    }
    
    static final String AI_NAME = "Monte_Carlo";
    static final int EMPTY = 0;
    static final int SIMTIME = 4;		//simulating time
    static final int MAXROTATE = 4;
    static final int MAXPOSITION = 8;
    static final int FIRE = 100;
    static final int SIZE = 110;
    static final double K = 0.5;		//UCB constant
    static final int MAX_PLAYOUT = 1000;
    static final int SUCCESS_SCORE = 150;
    static final double FAIL = 0.0;
    static final double SUCCESS = 1.0;
    
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
    int nodeCount = 0;
    
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
        	System.err.printf("turn:%d, show simulate\n", turn);
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
        	int maxHeight = 1;
        	int buf = 1;
        	while (true) {
        		int sum = 0;
        		for (int i = this.simulateBoard.length - 1; i >= maxHeight; i--) {
        			for (int j = 0; j < width; j++) {
        				if (this.simulateBoard[i][j] == 0 && this.simulateBoard[i - 1][j] > 0) {
        					buf = i;
        					sum++;
        					this.simulateBoard[i][j] = this.simulateBoard[i - 1][j];
        					this.simulateBoard[i - 1][j] = 0;
        				}	
        			}
        		}
        		maxHeight = buf;
        		if (sum == 0)
        			break;
        	}
        }
        
        public int deleteBlock() {
        	int deleteCount = 0;
        	int sum = 0;
        	boolean[] flag = new boolean[4];
        	for (int i = this.simulateBoard.length - 1; i > 0; i--) {
        		for (int j = 0; j < width - 1; j++) {
        			//when there is no block or obstacle
        			if (this.simulateBoard[i][j] == 0 || this.simulateBoard[i][j] == obstacle) 
        				continue;
        			
        			//reset flag
        			for (int k = 0; k < flag.length; k++) 
        				flag[k] = true;
        			
        			for (int x = 2; x <= summation; x++) {
        				if (!flag[0] && !flag[1] && !flag[2] && !flag[3])
        					break;
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
        							this.simulateBoard[i][j + k] = -Math.abs(this.simulateBoard[i][j + k]);
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
        					if (flag[1] && sum == summation) {
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
        					if (flag[2] && sum == summation) {
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
        					if (flag[3] && sum == summation) {
        						for (int k = 0; k < x; k++) {
        							deleteCount++;
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
    				this.pack[j][packSize - 1 - i] =  res[i][j];
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

    public class AdditionalSimulate {
    	private Pack[] packs;
    	private Board board;
    	private int obstacle;
    	private int bestWay = 0;
    	public AdditionalSimulate(Pack[] p, Board b, int obstacle) {
    		this.packs = p;
    		this.board = b;
    		this.obstacle = obstacle;
    	}
    	
    	public int[] fire() {
    		int simulateTimes = 1;
    		int nowScore = 0;
    		int[] rotate;
    		int[] position;
    		for (int i = 0; i < (int)Math.pow(8, simulateTimes); i++) {
    			position = shinsu(i, 8, simulateTimes);
    			
    			for (int j = 0; j < (int)Math.pow(4, simulateTimes); j++) {
    				rotate = shinsu(j, 4, simulateTimes);
    				Board b = (Board) this.board.clone();
    				int ojama = this.obstacle;
    				nowScore = 0;
    				
    				for (int k = 0; k < simulateTimes; k++ ) {
    					Pack nowPack = (Pack)this.packs[k].clone();
    					if (ojama > 0) {
    						ojama = nowPack.fillObstaclePack(ojama);
    					}
    					nowPack.packRotate(rotate[k]);
    					b.setPack(nowPack, position[k]);
    					nowScore = score(b.howManyChain());
    					
    					if (b.dangerZone()) {
    						break;
    					}
    					
    					if (nowScore > FIRE) {
							int[] fire = {rotate[0], position[0]};
							System.err.printf("rota:%d, pos:%d, Score:%d\n", rotate[0], position[0], nowScore);
							return fire;
						}
    				}
    			}
    		}
    		return null;
    	}
    	
    	public int[][] simulateTwo() {
    		bestWay = 0;
    		int simulateTimes = 2;
    		int[][] way = new int[2][SIZE];
    		int maxScore = 0;
    		int nowScore = 0;
    		int[] rotate;
    		int[] position;
    		int[][] best = new int[2][simulateTimes];
    		int[][] insurance = new int[2][simulateTimes];
    		for (int i = 0; i < (int)Math.pow(7, simulateTimes); i++) {
    			position = shinsu(i, 8, simulateTimes);
    			
    			for (int j = 0; j < (int)Math.pow(4, simulateTimes); j++) {
    				rotate = shinsu(j, 4, simulateTimes);
    				Board b = (Board) this.board.clone();
    				int ojama = this.obstacle;
    				nowScore = 0;
    				
    				for (int k = 0; k < simulateTimes; k++ ) {
    					Pack nowPack = (Pack)this.packs[k].clone();
    					if (ojama > 0) {
    						ojama = nowPack.fillObstaclePack(ojama);
    					}
    					nowPack.packRotate(rotate[k]);
    					b.setPack(nowPack, position[k]);
    					int[] block = b.howManyChain();
    					nowScore = score(block);
    					if (b.dangerZone()) {
    						break;
    					}
    					
						/*if (nowScore > FIRE && k == 0) {
							int[][] fire = {{rotate[0]}, {position[0]}};
							System.err.printf("rota:%d, pos:%d, Score:%d\n", rotate[0], position[0], nowScore);
							debugArray(block);
							return fire;
						}*/
						
						if (k < simulateTimes - 1)
							continue;
					
    					if (nowScore > maxScore) {
    						bestWay = 0;
    						way = new int[2][SIZE];
    						maxScore = nowScore;
    						best[0] = Arrays.copyOf(rotate, rotate.length);
    						best[1] = Arrays.copyOf(position, position.length);
    					}
    					
    					if (nowScore == maxScore) {
    						way[0][bestWay] = rotate[0];
    						way[1][bestWay] = position[0];
    						bestWay ++;
    					}
    				}
    			}
    		}
			return way;
    	}
    	
    	public int[] simulate() {
    		int addSimTime = 3;
    		int[][] way = this.simulateTwo();
    		int maxScore = 0;
    		int nowScore = 0;
    		int[] rotate;
    		int[] position;
    		boolean insuranceFlag = true;
    		int[] best = new int[2];
    		int[] insurance = new int[2];
    		int[] re = { way[0][0], way[1][0] };
    		if (bestWay == 1)
    			return re;
    			
    		for (int i = 0; i < bestWay; i++) {
    			int ojama = this.obstacle;
    			Board b = (Board) this.board.clone();
    			Pack p = (Pack) this.packs[0].clone();
    			if (ojama > 0)
    				p.fillObstaclePack(ojama);
    			p.packRotate(way[0][i]);
    			b.setPack(p, way[1][i]);
    			b.howManyChain();
    			for (int j = 0; j < (int)Math.pow(8, addSimTime); j++) {
        			position = shinsu(j, 8, addSimTime);
        			
        			for (int k = 0; k < (int)Math.pow(4, addSimTime); k++) {
        				rotate = shinsu(k, 4, addSimTime);
        				Board nowBoard = (Board) b.clone();
        				nowScore = 0;
        				for (int l = 0; l < addSimTime; l++) {
        					Pack nowPack = (Pack)this.packs[l + 1].clone();
        					if (ojama > 0) {
        						ojama = nowPack.fillObstaclePack(ojama);
        					}
        					nowPack.packRotate(rotate[l]);
        					nowBoard.setPack(nowPack, position[l]);
        					int[] block = nowBoard.howManyChain();
        					nowScore = score(block);
        					
        					if (nowBoard.dangerZone()) {
        						break;
        					}
        					
    						if (l < addSimTime - 1)
    							continue;
    						
    						if (insuranceFlag) {
    							insurance[0] = way[0][i];
        						insurance[1] = way[1][i];
        						insuranceFlag = false;
    						}
    					
        					if (nowScore > maxScore) {
        						maxScore = nowScore;
        						best[0] = way[0][i];
        						best[1] = way[0][i];
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
    
    public class Node {
    	Node parent = null;
    	ArrayList<Node> children = null;
    	int childCount = 0;
    	
    	int[] set = null; 	//set[0] = pos, set[1] = rot
    	
    	int playCount = 0;
    	
    	double successSum = 0.0;
    	double successRate = 0.0;
    	double ucb = 0.0;
    	int turn;
    	public Node (int[] s, int turn) {
    		this.turn = turn;
    		if (s == null)
    			nodeCount = 0;
    		this.set = s;
    		nodeCount++;
    	}
    	
    	public void reloadSuccess() {
    		double sum = 0;
    		for (int i = 0; i < children.size(); i++) {
    			sum += children.get(i).successSum;
    		}
    		this.successSum = sum;
    		this.successRate = sum / (double)this.playCount;
    	}
    	
    	public void addChild() {
    		this.children = new ArrayList<Node>(0);
    		for (int i = 0; i < MAXPOSITION; i++) {
    			for (int j = 0; j < MAXROTATE; j++) {
    				int[] s = {i, j};
    				this.children.add(new Node(s, this.turn + 1));
    				this.childCount++;
    			}
    		}
    	}
    	
    	public void calcUCB() {
    		if (this.playCount == 0)
    			this.ucb = 10000 + random.nextInt(100);
    		else {
    			this.ucb = this.successRate 
    					+ K * Math.sqrt(Math.log((double)this.parent.playCount) * 2 / (double)this.playCount);
    		}
    	}
    	
    	public int getBestUCB() {
    		int max = 0;
    		for (int i = 0; i < children.size(); i++) {
    			children.get(i).calcUCB();
    			if (children.get(i).ucb >= children.get(max).ucb) {
    				max = i;
    			}
    		}
    		return max;
    	}
    }
    
    public class MonteCarlo {
    	private Node root;
    	private Board board;
    	
    	public MonteCarlo(Board b, int turn) {
    		this.root = new Node(null, turn);
    		this.board = b;
    	}
    	
    	public void searchUCT(Board b, Node parent) {
    		
    	}
    	
    	//main
    	public int[] getBest() {
    		Board b;
    		root.addChild();
    		for (int i = 0; i < MAX_PLAYOUT; i++) {
    			b = (Board) this.board.clone();
    			this.searchUCT(b, root);
    		}
    		int max = root.getBestUCB();
    		
    		return root.children.get(max).set;
    	}
    	
    	public double onePlayout(Board board, Node n, int turn) {
    		int t = turn;
    		Board b = board;
    		while (true) { 
    			int rot = random.nextInt(MAXROTATE);
    			int pos = random.nextInt(MAXPOSITION);
    			Pack p = (Pack) pack[t++].clone();
    			b.obstacleNum = p.fillObstaclePack(b.obstacleNum);
    			p.packRotate(rot);
    			b.setPack(p, pos);
    			int[] block =  b.howManyChain();
    			if (b.dangerZone())
    				return FAIL;
    			if (score(block) >= SUCCESS_SCORE)
    				return SUCCESS;
    			if (t > maxTurn)
    				return FAIL;
    		}
    	}
    }
    
    public int[] shinsu(int n, int shinsu, int length) {
    	int x = n;
    	int[] ans = new int[length];
    	for (int i = length - 1; i >= 0; i--) {
    		ans[i] = x % shinsu;
    		x /= shinsu;
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
           int[] best = new int[2];
           while (true) {
                turn = in.nextInt();
                millitime = in.nextLong();
                my = new Board(width, height, in);
                op = new Board(width, height, in);
                int col = 0, rot = 0;
                Pack[] packs = new Pack[SIMTIME];
                for (int i = 0; i < SIMTIME; i++) {
                	packs[i] = (Pack) pack[turn + i].clone();
                }
                AdditionalSimulate search = new AdditionalSimulate(packs, my, my.obstacleNum);
                best = search.fire();
                if (best == null)
                	best = search.simulate();

                rot = best[0];
                col = best[1];
                
                println(col + " " + rot);
            }
        }
    }
    
    int chain(int[] block) {
    	int chain = 0;
    	for (int i = 0; i < block.length; i++) {
    		if (block[i] > 0)
    			chain++;
    	}
    	return chain;
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
    		sum += (int)(Math.floor(Math.pow(1.3, i + 1)) * Math.floor(block[i] / 2));
    	}
    	return sum;
    }
    
    void debugArray(int[][] a) {
    	System.err.println("test");
    	for (int i = 0; i < a.length; i++) {
    		System.err.printf("[");
    		for (int j = 0; j < a[0].length; j++) {
    			System.err.printf("%d, ", a[i][j]);
    		}
    		System.err.printf("]\n");
    	}
    }
    
    void debugArray(int[] a) {
    	System.err.println("test");
    	System.err.printf("[");
    	for (int i = 0; i < a.length; i++) {
    		System.err.printf("%d, ", a[i]);	
    	}
    	System.err.println("]");
    }
}