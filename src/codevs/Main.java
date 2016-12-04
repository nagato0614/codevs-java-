import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;
import java.lang.Math;

public class Main {
	
    public static void main(String[] args) {
        new Main().run();
    }
    
    static final String AI_NAME = "allserach_V2";
    static final int EMPTY = 0;
    static final int SIMTIME = 3;		//simulating time
    static final int MAXROTATE = 4;
    static final int MAXPOSITION = 8;
    static final int FIRE = 300;
    static final int MINIMUN_CHAIN_BLOCK = 2;
    static final int ALL = MAXROTATE * MAXPOSITION;
    
    int maxDeep = 0;
    int nodeCount;
    
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
    int[] rott;
    Board my;
    Board op;
    int miniFire = 0;

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
				for (int j = height + packSize - 1; j >= 0; j--) {
					if (this.simulateBoard[j][setPos + i] == EMPTY) {
						for (int k = packSize - 1; k >= 0; k--) {
							if (p.pack[k][i] != EMPTY) {
								this.simulateBoard[j][setPos + i] = p.pack[k][i];
								j--;
							}
						}
						break;
					}
				}
			}
		}
        
		public int[] howManyChain() {
			int sum = 0;
			ArrayList<Integer> block = new ArrayList<Integer>(1);
			block.add(this.deleteBlock(0));
			while (block.get(sum) > 0) {
				sum++;
				block.add(this.deleteBlock(this.fallBlock()));
			}
			int[] b = new int[block.size() - 1];
			for (int i = 0; i < block.size() - 1; i ++) {
				b[i] = block.get(i);
			}
			return b;
		}
        
		public int fallBlock() {
			boolean flag = false;
			int maxHeight = height + packSize;
			while (true) {
				int sum = 0;
				for (int i = 0; i < width; i++) {
					flag = false;
					for (int j = height + packSize - 1; j > 0; j--) {
						if (this.simulateBoard[j][i] == EMPTY) {
							for (int k = j - 1; k >= 0; k--) {
								if (this.simulateBoard[k][i] != EMPTY) {
									this.simulateBoard[j][i] = this.simulateBoard[k][i];
									this.simulateBoard[k][i] = EMPTY;
									break;
								}
								if (k == 0)
									flag = true;
							}
						}
						if (flag) 
							break;
						if (j < maxHeight)
							maxHeight = j;
					}
				}
				if (sum == 0)
					break;
			}
			return maxHeight;
		}
        
		public int deleteBlock(int height) {
			int deleteCount = 0;
			int sum = 0;
			boolean[] flag = new boolean[4];
			for (int i = this.simulateBoard.length - 1; i >= height; i--) {
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
    	Node parent = null;
    	ArrayList<Node> children = null;
    	Board board;
    	int childCount = 0;
    	int[] set = null; 	//set[0] = pos, set[1] = rot
    	int turn;
    	int deep;
    	int id;
    	boolean isChain;
    	double maxScore = 0;
    	
    	public Node (Node parent, int[] s, int nowTurn, int deep) {
    		this.parent = parent;
    		this.turn = nowTurn;
    		this.set = s;
    		this.deep = deep;
    		if (this.deep > maxDeep) {
    			maxDeep = this.deep;
    		}
    		this.id = nodeCount++;
    		this.isChain = false;
    	}
    	
    	public void setBoard(Board b) {
    		if (this.board == null)
    			this.board = b;
    	}
    	
    	public void addChild() {
    		this.children = new ArrayList<Node>(32);
    		for (int i = 0; i < MAXPOSITION; i++) {
    			for (int j = 0; j < MAXROTATE; j++) {
    				int[] s = {i, j};
    				this.children.add(new Node(this, s, this.turn + 1, this.deep + 1));
    				this.childCount++;
    			}
    		}
    	}
    	
    	public void updateMaxScore() {
    		for (Node n = this; n.parent != null; n = n.parent) {
    			if (n.parent.maxScore < n.maxScore) 
    				n.parent.maxScore = n.maxScore;
    			else 
    				break;
    		}
    	}
    	
    	public int maxScoreChildIndex() {
    		int max = 0;
    		for (int i = 0; i < this.children.size(); i++) {
    			if (this.children.get(i).maxScore > this.children.get(max).maxScore) {
    				max = i;
    			}
    		}
    		return max;
    	}
    	
    	public void showAllChildren() {
    		for (int i = 0; i < this.children.size(); i++) {
    			Node child = this.children.get(i);
    			System.err.printf("id : %2d, maxScore : %3f, children : %2d\n"
    					,child.id , child.maxScore, child.childCount);
    		}
    	}
    } 
    
    public class AllSearch {
    	private Pack[] packs;
    	private Board board;
    	private int obstacle;
    	private Node root;
    	private ArrayDeque<Node> queue;
    	public AllSearch(Pack[] p, Board b, int obstacle) {
    		this.packs = p;
    		this.board = b;
    		this.obstacle = obstacle;
    		root = new Node(null, null, turn - 1, 0);  //now status
    		root.setBoard(this.board);
    		queue = new ArrayDeque<Node>(32);
    		root.addChild();
    		for (int i = 0; i < ALL; i++) {
    			queue.addLast(root.children.get(i));
    		}
    	}
    	
    	public int[] simulateOneTurn(Board board, Pack pack, int[] set) {
    		board.obstacleNum = pack.fillObstaclePack(board.obstacleNum);
    		pack.packRotate(set[1]);
    		board.setPack(pack, set[0]);
    		return board.howManyChain();
    	}
    	
    	public int oneBlockFall(Board board) {
    		int max = 0;
    		for (int i = 0; i < width; i++) {
    			for (int j = 0; j < height; j++) {
    				if (board.simulateBoard[j][i] != 0){
    					if (j == 0)
    						break;
    					
    					for (int k = 1; k < 9; k++) {
    						board.simulateBoard[j - 1][i] = k;
    						int score = score(board.howManyChain());
    						if (score > max)
    							max = score;
    					}
    				}
    			}
    		}
    		return max;
    	}
    	
    	public int[] breadthFirstSearch() {
    		int[] block;
    		double score = 0;
    		while (this.queue.size() > 0) {
    			Node n = this.queue.removeFirst();
    			Board b = (Board) n.parent.board.clone();
    			block = this.simulateOneTurn(b, (Pack)pack[n.turn].clone(), n.set);
    			score = (double)score(block);
    			if (b.dangerZone()) {
    				n.parent.children.remove(n);
    				continue;
    			}
    			
    			if (n.deep == 1) {
    				if (miniFire != 0) {
    					if (score > miniFire) {
    						System.err.println("FIRE");
    						return n.set;
    					}
    				} else {
    					if (score > FIRE) {
    						System.err.println("FIRE");
    						return n.set;
    					}
    				}
//    				if (block.length < 2) {
    					n.setBoard(b);
    					n.addChild();
    					for (int i = 0; i < n.children.size(); i++){
    						queue.addLast(n.children.get(i));
    					}
//    				}
    			} else {
    				n.maxScore = (score / Math.pow(1.1, n.deep));
    				n.updateMaxScore();
    				if (n.deep < SIMTIME) {
    					n.setBoard(b);
    					n.addChild();
    					for (int i = 0; i < ALL; i++) {
    						queue.addLast(n.children.get(i));
    					}
    				}
    			}
    		}
    		root.showAllChildren();
    		try {
    			return root.children.get(root.maxScoreChildIndex()).set;
    		} catch (IndexOutOfBoundsException e) {
    			int[] s = {0, 0};
    			return (s);
    		}
    		
    	}
    	
    	public double chainQuality(int[] block) {
    		if (block.length == 0)
    			return 1.0;
    		int sum = 0;
    		for (int i = 0; i < block.length; i++) {
    			if (block[i] > 0)
    				sum += block[i];
    			else 
    				break;
    		}
    		return Math.pow(1.00000001, sum / block.length - 2);
    	}
    }
    
    public int[] shinsu(int n, int shinsu) {
    	int x = n;
    	int[] ans = new int[SIMTIME];
    	for (int i = SIMTIME - 1; i >= 0; i--) {
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
        	   System.err.println("TURN : " + (turn + 1));
        	   nodeCount = 0;
               turn = in.nextInt();
               millitime = in.nextLong();
               my = new Board(width, height, in);
               op = new Board(width, height, in);
               int col = 0, rot = 0;
               Pack[] packs = new Pack[SIMTIME];
               for (int i = 0; i < SIMTIME; i++) {
            	   packs[i] = (Pack) pack[turn + i].clone();
               }
               if (my.obstacleNum > 10) {
            	   miniFire = 5 * 10;
               }
               AllSearch search = new AllSearch(packs, my, my.obstacleNum);
               best = search.breadthFirstSearch();
               col = best[0];
               rot = best[1];
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