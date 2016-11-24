package codevs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.lang.Math;

public class Main {
	
    public static void main(String[] args) {
        new Main().run();
    }
    
    static final String AI_NAME = "Monte_Carlo";
    static final int EMPTY = 0;
    static final int MINIMU_DELETE_BLOCK = 2;
    static final int SIMTIME = 4;		//simulating time
    static final int MAXROTATE = 4;
    static final int MAXPOSITION = 8;
    static final int FIRE = 50;
    static final int SIZE = 110;
    static final double K = 0.1;		//UCB constant
    static final int MAX_PLAYOUT = 10000;
    static final int MAX_USE_PACKS = 3;
    static final int SUCCESS_SCORE = 0;
    static final double FAIL = 0.0;
    static final double SUCCESS = 1000000;
    static final int THRESHOLD = 50;
    static final int ALL = MAXPOSITION * MAXROTATE;
    
    int[][] SET;
    ArrayList<Integer> sample;
    Random random = new Random();
    int turn = -1;
    Pack[] pack;
    int width;
    int height;
    int packSize = 3;
    int summation;
    int obstacle;
    int maxTurn;
    long millitime;
    int nodeCount = 0;
    int maxDeep = -1;
    int maxScore = 0;
    
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
        	if (block.size() > 1)
        		block.remove(block.size() - 1);
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
        
    		double keisu = 0.01;
    		double tei = 1.3;
    		double score = 0;
    		for (int i = packSize; i < height + packSize; i++) {
    			for (int j = 0; j < width; j++) {
    					score += Math.pow(tei, Math.abs((width / 2.0) - j));
    				}
    			}
    		}
    		if (score == 0)
    			return 1.0;
    		return score * keisu;
    	}
        
    		double keisu = 0.01;
    		double tei = 1.2;
    		double score = 0;
    		for (int i = packSize; i < height + packSize; i++) {
    			for (int j = 0; j < width; j++) {
    					score += Math.pow(tei, i - packSize);
    				}
    			}
    		}
    		if (score == 0)
    			return 1.0;
    		return score * keisu;
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
    	int[][] pack = new int[packSize][packSize];
    	
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
    	int childCount = 0;
    	int[] set = null; 	//set[0] = pos, set[1] = rot
    	int playCount;
    	//double successSum = 0.0;
    	double successRate = 0.0;
    	double ucb = 0.0;
    	int turn;
    	int deep;
    	int id;
    	boolean isChain;
    	
    	public Node (Node parent, int[] s, int nowTurn, int deep) {
    		this.parent = parent;
    		this.turn = nowTurn;
    		this.playCount = 0;
    		this.set = s;
    		this.deep = deep;
    		if (this.deep > maxDeep) {
    			maxDeep = this.deep;
    		}
    		this.id = nodeCount++;
    		this.isChain = false;
    	}
    	
    	public void reverseSuccessRate() {
    		for (int i = 0; i < this.children.size(); i++) {
    			if (this.children.get(i).isChain)
    				this.children.get(i).successRate *= -1;
    		}
    	}
    	
    	public void update(Board board) {
    		double sum = 0;
    		//int play = 0;
    		for (int i = 0; i < this.children.size(); i++) {
    			Node c = children.get(i);
    			//play += c.playCount;
    			sum += c.successRate;
    		}
    		//this.playCount = play;
    		this.successRate = (sum / this.children.size());
    	}
    	
    	public void updateSuccess(double win) {
    		this.successRate = (this.playCount * this.successRate + win) / (this.playCount + 1);
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
    	
    	public void calcUCB() {
    		if (this.playCount == 0) {
    			this.ucb = 100.0 + random.nextDouble();
    		} else {
    			this.ucb = this.successRate 
    					+ K * Math.sqrt((Math.log((double)this.parent.playCount) * 2.0) / (double)this.playCount);
    		}
    	}
    	
    	public int getBestUcbIndex() {
    		int max = 0;
    		for (int i = 0; i < children.size(); i++) {
    			children.get(i).calcUCB();
    			if (children.get(i).ucb > children.get(max).ucb) {
    				max = i;
    			}
    		}
    		return max;
    	}
    	
    	public void reverseChildrenRate() {
    		for (int i = 0; i < this.children.size(); i++) {
    			if (children.get(i).isChain)
    				children.get(i).successRate *= -1;
    		}
    	}
    	
    	public void showNodeData() {
    		if (this.children != null) {
    			System.err.printf("id : %3d, best_ucb : %f, rate : %f, games : %d, playout : %d, node : %d\n",
    					this.id, children.get(this.getBestUcbIndex()).ucb, this.successRate, 
    					children.get(this.getBestUcbIndex()).playCount,
    					this.playCount, nodeCount);
    		} else {
    			System.err.printf("id : %3d, best_ucb : null, rate : %f, games : null, playout : %d, node : %d\n",
    					this.id, this.successRate,
    					this.playCount, nodeCount);
    		}
    	}
    	
    	public void showAllChildren() {
    		for (int i = 0; i < this.children.size(); i++) {
    			Node c = this.children.get(i);
    			System.err.printf("parentID : %3d, id : %3d, set : {%d, %d}, rate : %f, playout : %3d, ucb : %f, children : %d\n",
    					this.id, c.id, c.set[0], c.set[1], c.successRate, c.playCount, c.ucb, c.childCount);
    		}
    	}
    	
    	public int getMaxPlayoutIndex() {
    		int max = 0;
    		for (int i = 0; i < this.children.size(); i++) {
    			if (this.children.get(i).playCount > this.children.get(max).playCount) {
    				max = i;
    			}
    		}
    		return max;
    	}
    }
    
    public class MonteCarlo {
    	private Node root;
    	private Board board;		//now board
    	
    	public MonteCarlo(Board b, int nowTurn) {
    		maxDeep = -1;
    		nodeCount = 0;
    		this.root = new Node(null, null, nowTurn - 1, 0);
    		this.board = b;
    	}
    	
    	public double searchUCT(Board b, Node parent) {
    		double win = 0;
    		Node bestChild = null;
    		Board nextBoard = null;
    		for (int i = 0; i < ALL; i++) {
    			int index = parent.getBestUcbIndex();
    			try {
    				bestChild = parent.children.get(index);
    			} catch (IndexOutOfBoundsException e) {
    				System.err.println("IndexOutOfBoundsException");
    				println("0 0");  //lose
    			}
    			Pack p = (Pack)pack[bestChild.turn].clone();
    			nextBoard = (Board) b.clone();
    			nextBoard.obstacleNum = p.fillObstaclePack(nextBoard.obstacleNum);
    			p.packRotate(bestChild.set[1]);
    			nextBoard.setPack(p, bestChild.set[0]);
    			int[] block = nextBoard.howManyChain();
    			if (nextBoard.dangerZone()) {
    				parent.children.remove(index);
    				parent.childCount--;
    			} else {
    				if (score(block) > FIRE && parent.deep == 0) {
    					//debugArray(block);
    					bestChild.successRate += SUCCESS;
    					return SUCCESS;
    				}
    				if (block[0] > 0 && parent.deep == 0) {
    					if (score(block) > 2) {
    						parent.children.remove(index);
        					parent.childCount--;
    					}
    					bestChild.isChain = true;
    				} else {
    					break;
    				}
    			}
    		}
    		
    		if (bestChild.children != null) {
    			win = searchUCT((Board)nextBoard.clone(), bestChild);
    		} else {
    			if (bestChild.playCount <= THRESHOLD) {
    				win = onePlayout((Board) nextBoard.clone(), bestChild);
    			} else {
    				bestChild.addChild();
    				//bestChild.showNodeData();
    				win = searchUCT((Board) nextBoard.clone(), bestChild);
    			}
    		}
    		bestChild.successRate = ((bestChild.successRate * bestChild.playCount + win) 
    				/ (bestChild.playCount + 1));
    		parent.playCount++;
    		bestChild.playCount++;
    		return win;
    		//parent.update(nextBoard);
    	}
    	
    	//main
    	public int[] getBest() {
    		Board b;
    		root.addChild();
    		for (int i = 0; i < MAX_PLAYOUT; i++) {
    			b = (Board) this.board.clone();
    			this.searchUCT(b, root);
    		}
			root.reverseChildrenRate();
    		int max = root.getBestUcbIndex();
    		
    		System.err.printf("TURN : %d\n", turn);
    		//root.showAllChildren();
    		root.showNodeData();
    		root.showAllChildren();
//    		for (int i = 0; i < root.children.size(); i++) {
//    			if (root.children.get(i).children != null) {
//    				root.children.get(i).showNodeData();
//    				root.children.get(i).showAllChildren();
//    			}
//    		}
//    		System.err.printf("deep : %d\n", maxDeep);
    		return root.children.get(max).set;
    	}
    	
    	public double higherPoint(Board board) {
    		double keisu = 0.01;
    		double tei = 1.2;
    		double score = 0;
    		for (int i = packSize; i < height + packSize; i++) {
    			for (int j = 0; j < width; j++) {
    				if (board.simulateBoard[i][j] != 0) {
    					score += Math.pow(tei, i - packSize);
    				}
    			}
    		}
    		if (score == 0)
    			return 1.0;
    		return score * keisu;
    	}
    	
    	public double onePlayout(Board board, Node n) {
    		Board bo = board;
    		Board buf = null;
    		int nowTurn = n.turn + 1;
    		int[] block = null;
    		//int score = 0;
    		for (int t = 0; t < MAX_USE_PACKS; t++) {
    			Collections.shuffle(sample);
    			for (int i = 0; i < ALL; i++) {
    				buf = (Board) bo.clone();
    				Pack p = (Pack) pack[nowTurn].clone();
    				buf.obstacleNum = p.fillObstaclePack(buf.obstacleNum);
    				p.packRotate(SET[sample.get(i)][1]);
    				buf.setPack(p, SET[sample.get(i)][0]);
    				block =  buf.howManyChain();
    				//score = score(block);
    				if (!buf.dangerZone()) {
    					break;
    				} else {
    					if (i < ALL - 1)
    						continue;
    					else 
    						return FAIL;
    				}
    			}
    			
	    		if (chain(block) > SUCCESS_SCORE) {
	    			//System.err.printf("turn : %d, score : %d\n", turn, score);
	    			//System.err.printf("turn : %d, score : %d\n", turn + MAX_USE_PACKS, score);
	    			return (chain(block) / 80.0);
	    		}
    			bo = buf;
    			if (turn >= maxTurn)
    				return FAIL;
    			nowTurn++;
    		}

    		return FAIL;
    	}
    }
    
    public class Time {
    	private long start;
    	private long end;
    	private long average;
    	private int num;
    	public Time() {
    		this.start = 0;
    		this.end = 0;
    		this.average = 0;
    		this.num = 0;
    	}
    	
    	public void start() {
    		this.start = System.currentTimeMillis();
    	}
    	
    	public void end() {
    		this.end = System.currentTimeMillis();
    	}
    	
    	public void showTime() {
    		long time = end - start;
    		this.average = (this.average * this.num + time) / (this.num + 1);
    		this.num++;
    		System.err.printf("time : %d, average : %d\n", time ,this.average);
    	}
    }
    
    void run() {
        println(AI_NAME);
        Time time = new Time();
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
           makeSample();
           while (true) {
        	   time.start();
                turn = in.nextInt();
                millitime = in.nextLong();
                my = new Board(width, height, in);
                op = new Board(width, height, in);
                //chainCheck((Board)my.clone(), (Pack)pack[turn].clone());
                int col = 0, rot = 0;
                //debugArray(pack[turn].pack);
                MonteCarlo monte = new MonteCarlo((Board)my.clone(), turn);
                best = monte.getBest();

                rot = best[1];
                col = best[0];
                
                println(col + " " + rot);
                time.end();
                time.showTime();
            }
        }
    }
    
    void chainCheck(Board board, Pack nowPack) {
    	int max = -1;
    	for (int i = 0; i < MAXPOSITION; i++) {
    		for (int j = 0; j < MAXROTATE; j++) {
    			Board b  = (Board) board.clone();
    			Pack p = (Pack) nowPack.clone();
    			p.packRotate(j);
    			b.setPack(p, i);
    			int[] block = b.howManyChain();
    			int score = score(block);
    			if (score > max) {
    				max = score;
    			}
    		}
    	}
    	System.err.printf("turn : %d, score : %d\n", turn, max);
    }
    
    void makeSample() {
        sample = new ArrayList<Integer>();
        SET = new int[ALL][2];
        for (int i = 0; i < MAXPOSITION * MAXROTATE; i++) {
     	   sample.add(i);
     	   for (int j = 0; j < MAXPOSITION; j++) {
     		   SET[i][0] = j;
     	   }
     	   for (int j = 0; j < MAXROTATE; j++) {
     		   SET[i][1] = j;
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
    
    public int[] shinsu(int n, int shinsu, int length) {
    	int x = n;
    	int[] ans = new int[length];
    	for (int i = length - 1; i >= 0; i--) {
    		ans[i] = x % shinsu;
    		x /= shinsu;
    	}
    	return ans;
    }
}