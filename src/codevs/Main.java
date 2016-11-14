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
    static final int FIRE = 100;
    static final int SIZE = 110;
    
    static final int POS = 8;
    static final int ROT = 4;
    
    static final double UCBCONSTANT = 0.1;
    
    //MonteCarlo
    static final int MAX_SIMULATE =	50;
    static final int EXPANDCOUNT = 5;		//
    static final int SIMULATE_NUMBER = 32;
    static final int FINISH_PLAYOFF = 100;	//score
    static final int PLAYOUT_COUNT = 2500;	//do playout times
    
    
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
        
        public int[] testHMC() {
        	int sum = 0;
        	ArrayList<Integer> block = new ArrayList<Integer>(1);
        	block.add(this.deleteBlock());
        	while (block.get(sum) > 0) {
        		//this.showSimulateBoard();
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
        
        public int testDeleteBlock() {
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
        	this.showSimulateBoard();
        	for (int i = 0; i < this.simulateBoard.length; i++) {
        		for (int j = 0; j < width; j ++) {
        			if (this.simulateBoard[i][j] < 0) {
        				this.simulateBoard[i][j] = EMPTY;
        			}
        		}
        	}
        	return deleteCount;
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

    public class Node {
    	Board board;
    	int turn;
    	
    	Node parent = null;
    	Node children[] = null;
    	int childCount = 0;
    	int PlayOutCount = 0;
    	
    	//set[0] = setPosition, set[1] = rotate;
    	int set[] = null;
    	
    	double successCount = 0;
    	double success = 0.0;
    	
    	public Node(Board board) {
    		this.board = board;
    	}
    	
    	public Node(Node parent, int set[], Board board) {
    		this.board = board;
    		this.parent = parent;
    		this.parent.childCount++;
    		this.set = set;
    	}
    
    	
    	public void addSuccess(double success) {
    		this.PlayOutCount++;
    		this.successCount += success;
    		this.success = this.successCount / this.PlayOutCount;
    		this.recountSeccess();
    	}

    	public void recountSeccess() {
    		for (Node pr = this; pr != null; pr = pr.parent) {
    			if (pr.childCount == 0)  //if pr do not has children
    				continue;
    			pr.PlayOutCount = 0;
    			for (Node nd : pr.children) {
    				if (nd != null) 
    					pr.PlayOutCount += nd.PlayOutCount;
    			}
    			double max = -1.0;
    			for (int i = 0; i < pr.children.length; i++) {
    				Node ch = pr.children[i];
    				if (ch == null)
    					continue;
    				if (ch.success > max) {
    					pr.success = max = ch.success;
    				}
    				pr.success = max;
    			}
    		}
    	}
    	
    	public void set(int[] a) {
    		this.set[0] = a[0];
    		this.set[1] = a[1];
    	}
    	
    	public double getUCB() {
    		return success + UCBCONSTANT * Math.sqrt(2.0 * Math.log(parent.PlayOutCount / PlayOutCount));
    	}
    }
    
    public class MonteCarlo {
    	private Board board;
    	private int obstacle;
    	Node root;
    	int turn;
    	
    	public MonteCarlo(Board b, int obstacle, int turn) {
    		this.board = b;
    		this.obstacle = obstacle;
    		this.root = new Node((Board) this.board.clone());
    		this.turn = turn;
    	}
    	
    	public int[] playOutTree(int playOutCount, int nowTurn) {
    		if (this.board.dangerZone()) {
    			System.err.printf("danger\n");
    			return null;
    		}
    		
    		while (root.PlayOutCount < playOutCount) {
    			this.playOut(root, nowTurn);
    		}
    		
    		int maxCount = -1;
    		Node maxnd = null;
    		if (this.root.childCount > 0) {
    			for (Node nd : root.children) {
    				if (nd == null)
    					continue;
    				double ucb = nd.getUCB();
    				if (ucb > maxCount) {
    					maxCount = nd.PlayOutCount;
    					maxnd = nd;
    				}
    			}
    		}
    		
    		if (maxnd == null) {
    			System.err.printf("maxnd == null\n");
    			return null;
    		}
    		
    		return maxnd.set;
    	}
    	
    	public void playOut(Node node, int nowTurn) {
    		if (node.childCount == 0) {
    			if (node.PlayOutCount + 1 >= EXPANDCOUNT) {
    				makeBranch(node, nowTurn);
    			} else {
    				double sc = simplePlayOut(node, nowTurn);
    				node.addSuccess(sc);
    			}
    		} else {
    			Node bestNode = null;
    			double max = -1;
    			for (int i = 0; i < node.children.length; i++) {
    				if (node.children[i] == null)
    					continue;
    				if (max < node.children[i].success) {
    					max = node.children[i].success;
    					bestNode = node.children[i];
    				}
    			}
    			playOut(bestNode, turn + 1);
    		}
    	}
    	
    	public void makeBranch(Node parent, int nowTurn) {
    		parent.children = new Node[SIMULATE_NUMBER];
    		parent.childCount = 0;
    		parent.PlayOutCount++;
    		
    		for (int i = 0; i < SIMULATE_NUMBER; i++) {
    			int[] set = new int[2];
    			set[1] = i % ROT;
    			set[0] = i / 4;
    			nextTurn((Board)parent.board.clone(), nowTurn, set);
    			Node nd = new Node(parent, set, (Board) parent.board.clone());
    			parent.children[i] = nd;
    			
    			double sr = simplePlayOut(nd, nowTurn + 1);
    			nd.addSuccess(sr);
    		}
    		
    		parent.childCount = SIMULATE_NUMBER;
    	}
    	
    	//do playOut one time
    	private double simplePlayOut(Node node, int nowTurn) {
    		Board b = (Board) node.board.clone();
    		Pack p = null;
    		int pos = 0, rot = 0;
    		int[] block;
    		for (int i = 0; i < MAX_SIMULATE; i++) {
    			block = null;
    			if (nowTurn + i > maxTurn)
    				break;
    			//make next position and rotate
    			pos = random.nextInt(8);
    			rot = random.nextInt(4);
    			
    			p = (Pack) pack[nowTurn + i].clone();
    			b.obstacleNum = p.fillObstaclePack(b.obstacleNum);
    			p.packRotate(rot);
    			b.setPack(p, pos);
    			block = b.howManyChain();
    			if (b.dangerZone())
    				return 0.0;
    			if (score(block) >= FINISH_PLAYOFF) 
    				return 1.0;
    		}
    		return 0.0;
    	}
    }
    
    public void nextTurn(Board board, int turn, int[] set) {
    	Pack p = (Pack) pack[turn].clone();
    	if (board.obstacleNum > 0)
    		board.obstacleNum = p.fillObstaclePack(board.obstacleNum);
    	p.packRotate(set[1]);
    	board.setPack(p, set[0]);
    	board.howManyChain();
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
                MonteCarlo monte = new MonteCarlo((Board) my.clone(), my.obstacleNum, turn);
                best = monte.playOutTree(PLAYOUT_COUNT, turn);

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