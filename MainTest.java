import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

	static Scanner scanner;
	static final int countGA = 1;
	static final int MaxCapacity = 100;// 默认车的容量为100
	static int clientNum;// 客户数目,染色体长度
	static List<Integer> x1;
	static List<Integer> y1;
	static List<Integer> weightArr;// 客户需求量
	static final VehicleRoutingProblem vehicleRoutingProblem = new VehicleRoutingProblem();
	static final Vehicle veh = new Vehicle();// 车辆实例
	static int xStore = 0;
	static int yStore = 0;
	static List<String> routePrePlan = new ArrayList<>();// 预计划行驶路线
	static List<Integer> pointFlagList = new ArrayList<>();// 0-仓库;1-目标小区;2-捐助小区
	static List<Integer> statusFlagList = new ArrayList<>();// 0-未结束;1-已结束
	static int nextSupRouter = 0;// 一级路由
	static int nextSubRouter = 0;// 二级路由
	static int curType = 0; //-1:路上;0:仓库;1:目标小区;2:捐款小区
	
	public static void main(String[] args) throws Exception {

		clientNum = 5;// 客户数目,染色体长度
		x1 = new ArrayList<Integer>();
		y1 = new ArrayList<Integer>();
		weightArr = new ArrayList<Integer>();// 客户需求量
		scanner = new Scanner(System.in);
		boolean isEnd = false;
		int countInit = 0;
		

		while (!isEnd) {

			
			String recli = scanner.nextLine();
			String[] items = recli.split("\\s+");
			countInit++;
			try{
				if (items[0].equals("G")) {
					int curX = veh.getvX();
					int curY = veh.getvY();
					int idxCurPoint = Main.getPointIdx(curX, curY);
					// 开始移动,一次移动一步
					// 制定移动策略
					// 1.更新车的实时位置和载重
					// 2.若到达目标小区或者到达捐助小区，则更新车的载重和更新目标小区或者捐助小区的需求量或者捐助量
					if (countInit == 7) {
						// 初始化信息输入完成
						routePrePlan = vehicleRoutingProblem.run(Main.countGA, Main.clientNum, getDoubleArr(x1),
								getDoubleArr(y1), getDoubleArr(weightArr), veh.getvCapacity());
						if (routePrePlan != null) {
							// 纠正路线(并更新下一个计划路线序列点)
							checkNextRoute(curX, curY, idxCurPoint);
						} else {
							//强制退出
							break;
						}
						x1.add(veh.getvX());
						y1.add(veh.getvY());
						weightArr.add(-100);
						pointFlagList.add(0);
						statusFlagList.add(0);
						Main.clientNum++;
					}
					int idxNextPoint = Main.getNextPointIdx();
					if (idxCurPoint == -1) {
						Main.curType = -1;// 路上
						// 路上,前往下一个路线序列点
						String nextFlag = getNextRouterFlag(curX, curY, x1.get(idxNextPoint), y1.get(idxNextPoint),
								idxNextPoint);
						
						System.out.println(nextFlag);

					} else {
						if (x1.get(idxCurPoint) == Main.xStore && y1.get(idxCurPoint) == Main.yStore) {
							// 仓库
							veh.setvCapacity(Main.MaxCapacity);// 满载
							Main.curType = 0;//仓库
						} else {
							// TODO:0429没有考虑返程问题
							if (weightArr.get(idxCurPoint) > 0) {
								// 目标小区
								// 1. 更新目标小区状态
								// 2. 更新车的载重
								Main.curType = 1;//目标小区
								if ((veh.getvCapacity() - weightArr.get(idxCurPoint)) >= 0) {
									veh.setvCapacity(veh.getvCapacity() - weightArr.get(idxCurPoint));
									Main.clientNum--;
									if (Main.clientNum == 1) {
										isEnd = true;
										if (curY == 0) {
											System.out.println("S");
											//System.out.print(" null=="+Main.routePrePlan+" x1"+Main.x1+" y1"+Main.y1+" weightArr"+Main.weightArr+" idxCurPoint"+idxCurPoint);
											//System.out.println(" null==curX"+curX+" curY"+curY+" nextX nextY"+idxNextPoint+" nextRouterIdx"+" nextSup"+Main.nextSupRouter+" "+Main.nextSubRouter + " veh"+Main.veh.toString());

										} else {
											System.out.println("N");
											//System.out.print(" null=="+Main.routePrePlan+" x1"+Main.x1+" y1"+Main.y1+" weightArr"+Main.weightArr);
											//System.out.println("null==curX"+curX+" curY"+curY+" nextX nextY"+idxNextPoint+" nextRouterIdx"+" nextSup"+Main.nextSupRouter+" "+Main.nextSubRouter + " veh"+Main.veh.toString());

										}
										scanner.close();
										break;
									}
									x1.remove(idxCurPoint);
									y1.remove(idxCurPoint);
									weightArr.remove(idxCurPoint);
									// 将当前坐标点设置为起点
									weightArr.set(0, 0);
								} else {
									weightArr.set(idxCurPoint, weightArr.get(idxCurPoint) - veh.getvCapacity());
									weightArr.set(0, weightArr.get(idxCurPoint) - veh.getvCapacity());
									veh.setvCapacity(0);
								}

							} else {
								if (weightArr.get(idxCurPoint) < 0) {
									// 捐助小区
									// 1. 更新捐助小区状态
									// 2. 更新车的载重
									Main.curType = 2;//捐助小区
									if (veh.getvCapacity() - weightArr.get(idxCurPoint) > Main.MaxCapacity) {
										weightArr.set(idxCurPoint, veh.getvCapacity() - weightArr.get(idxCurPoint) - Main.MaxCapacity);
										weightArr.set(0, veh.getvCapacity() - weightArr.get(idxCurPoint) - Main.MaxCapacity);
										veh.setvCapacity(Main.MaxCapacity);
									} else {
										veh.setvCapacity(veh.getvCapacity() - weightArr.get(idxCurPoint));
										x1.remove(idxCurPoint);
										y1.remove(idxCurPoint);
										weightArr.remove(idxCurPoint);
										// 将当前坐标点设置为起点
										weightArr.set(0, 0);
									}

								}
							}
						}
						x1.set(0, curX);
						y1.set(0, curY);
						// if( Main.isSubRouteLastPoint() ){
						// 重新生成路线
						routePrePlan = vehicleRoutingProblem.run(Main.countGA, Main.clientNum, getDoubleArr(x1), getDoubleArr(y1), getDoubleArr(weightArr), veh.getvCapacity());
						if (routePrePlan != null) {
							// 纠正路线(并更新下一个计划路线序列点)
							checkNextRoute(curX, curY, idxCurPoint);
							// 前往下一个路线序列点,更新下一个序列点坐标
							idxNextPoint = Main.getNextPointIdx();
							String nextFlag = getNextRouterFlag(curX, curY, x1.get(idxNextPoint), y1.get(idxNextPoint),
									idxNextPoint);
							// System.out.println(nextFlag);
							// if(Main.clientNum == 1){
							// isEnd = true;
							// scanner.close();
							// break;
							// }
							if (nextFlag == null) {
								checkNextRoute(curX, curY, idxCurPoint);
								idxNextPoint = Main.getNextPointIdx();
								if(idxNextPoint == idxCurPoint){
									//原地打转
									reCheckNextPoint(curX, curY, idxCurPoint);
									idxNextPoint = Main.getNextPointIdx();
								}
								nextFlag = getNextRouterFlag(curX, curY, x1.get(idxNextPoint), y1.get(idxNextPoint), idxNextPoint);
								System.out.println(nextFlag);
							} else {
								System.out.println(nextFlag);
							}
						} else {
							break;
						}
						// }else{
						// 更新序列 update nextSupRouter/nextSubRouter

						// }

					}
					
					
				} else {
					// 输入仓库或者小区坐标
					boolean isStore = items[0].equals("S") ? true : false;
					Integer x = Integer.parseInt(items[1]);
					Integer y = Integer.parseInt(items[2]);
					x1.add(x);
					y1.add(y);
					int w = isStore ? 0 : -Integer.parseInt(items[3]);
					weightArr.add(w);// 默认第一个是仓库,索引为0,容量为0
					if (w > 0) {
						pointFlagList.add(1);// 目标小区
					} else {
						if (w < 0) {
							pointFlagList.add(2);// 捐助小区
						} else {
							pointFlagList.add(0);// 仓库
						}
					}
					statusFlagList.add(0);// 未结束
					if (isStore) {
						veh.setvX(x);
						veh.setvY(y);
						veh.setvCapacity(Main.MaxCapacity);
						Main.xStore = x;
						Main.yStore = y;

					}

					if (countInit > 7 && w < 0 ) {
						int curX = veh.getvX();
						int curY = veh.getvY();
						int idxCurPoint = Main.getPointIdx(curX, curY);
						int idxNextPoint = Main.getNextPointIdx();
						int nextX = Main.x1.get(idxNextPoint);
						int nextY = Main.y1.get(idxNextPoint);
						boolean isOk = false;
						if( (x == curX || x == nextX) && (  ( y <= curY || y >= nextY ) || ( y >= curY || y <= nextY )  ) ){
							isOk = true;
						}else{
							if(  (y == curY || y == nextY) &&  (  ( x <= curX || x >= nextX ) || ( x >= curX || x <= nextX )  )){
								isOk = true;
							}
						}
						
						if(isOk){
							routePrePlan = vehicleRoutingProblem.run(Main.countGA, Main.clientNum, getDoubleArr(x1),
									getDoubleArr(y1), getDoubleArr(weightArr), veh.getvCapacity());
							if (routePrePlan != null) {
								// 纠正路线(并更新下一个计划路线序列点)
								checkNextRoute(curX, curY, idxCurPoint);
							} else {
								//强制退出
								break;
							}
						}
						
					}
					

				}
			}catch(Exception e){
				System.out.print(e.getMessage());
			}

		}

	}

	/**
	 * convert List to Array
	 * 
	 * @param list
	 * @return
	 */
	public static double[] getDoubleArr(List<Integer> list) {
		double[] arr = null;
		if (list != null) {
			arr = new double[list.size()];
			for (int i = 0, len = list.size(); i < len; i++) {
				arr[i] = list.get(i);
			}
		}
		return arr;
	}

	/**
	 * 获取坐标对应的索引
	 * 
	 * @param pX
	 * @param pY
	 * @return
	 */
	public static int getPointIdx(int pX, int pY) {

		int res = -1;
		for (int i = 0, Xlen = x1.size(); i < Xlen; i++) {
			if (pX == x1.get(i) && pY == y1.get(i)) {
				res = i;
			}

		}

		return res;// 非仓库or非目标小区or非捐助小区
	}

	/**
	 * TODO:获取行驶方向,并更新车的位置状态
	 * 
	 * @param curX
	 * @param curY
	 * @param nextX
	 * @param nextY
	 * @param nextRouterIdx
	 * @return
	 */
	public static String getNextRouterFlag(int curX, int curY, int nextX, int nextY, int nextRouterIdx) {
		if (curX > nextX) {
			veh.setvX(veh.getvX() - 1);
			return "N";
		} else {
			if (curX < nextX) {
				veh.setvX(veh.getvX() + 1);
				return "S";
			} else {
				if (curY > nextY) {
					veh.setvY(veh.getvY() - 1);
					return "W";
				} else {
					if (curY < nextY) {
						veh.setvY(veh.getvY() + 1);
						return "E";
					} else {
						//TODO:解决原地打转
						System.out.print("null=="+Main.routePrePlan+" x1"+Main.x1+" y1"+Main.y1+" weightArr"+Main.weightArr);
						System.out.println("null==curX"+curX+" curY"+curY+" nextX"+nextX+" nextY"+nextY+" nextRouterIdx"+nextRouterIdx+" nextSup"+Main.nextSupRouter+" "+Main.nextSubRouter + " veh"+Main.veh.toString());
						return null;
					}
				}
			}
		}
	}
	
	

	/**
	 * 获取下一个路线序列点的索引
	 * 
	 * @return
	 */
	public static int getNextPointIdx() {
		
		String seq = Main.routePrePlan.get(Main.nextSupRouter);
		return Integer.parseInt(seq.substring(nextSubRouter, nextSubRouter + 1));
	}

	/**
	 * 是否是当前路线序列的末尾节点
	 * 
	 * @return
	 */
	public static boolean isSubRouteLastPoint() {
		String seq = routePrePlan.get(nextSupRouter);
		if (seq.substring(nextSubRouter + 2, nextSubRouter + 3).equals("0")) {
			return true;
		}
		return false;
	}

	/**
	 * 获取就近的目标小区
	 * @param curX
	 * @param curY
	 * @return
	 */
	public static int getBestDistanceReIndex(int curX, int curY){
		int tmp = 0;
		int min = 0;
		int minIdx = 0;
		for(int i = 0, len = Main.weightArr.size(); i < len; i++){
			if( Main.weightArr.get(i) > 0 && curX != x1.get(i) && curY != y1.get(i)){
				tmp = (curX - x1.get(i))*(curX - x1.get(i)) + (curY - y1.get(i)*(curY - y1.get(i)));
				min = min < tmp ? min : tmp;
				minIdx = i;
			}
		}
		return minIdx;
	}
	
	/**
	 * 纠正路线序列
	 */
	public static void checkNextRoute(int curX, int curY, int idxCurPoint) {
		String tmp;
		int idxTmp;
		int idxStore = 0;
		int bestReIndex = getBestDistanceReIndex(Main.veh.getvX(), Main.veh.getvY());
		int nextSupRouterTmp = 0, nextSubRouterTmp = 0;
		boolean isOk = false;
		for (int i = 1, len = x1.size(); i < len; i++) {
			if (x1.get(i) == Main.xStore && y1.get(i) == Main.yStore) {
				idxStore = i;
				break;
			}
		}
		if ( Main.curType == 0 ) {
			// 仓库
			int len = routePrePlan.size();
			try {
				for (int i = 0; i < len; i++) {
					tmp = routePrePlan.get(i);

					idxTmp = Integer.parseInt(tmp.substring(2, 3));
					Main.nextSupRouter = i;
					Main.nextSubRouter = 2;
					if (Main.weightArr.get(idxTmp) > 0) {
						// 找目标小区
						isOk = true;
						break;
					} else {
						int bestReIndexTmp = tmp.indexOf("" + bestReIndex);
						if (bestReIndexTmp != -1) {
							nextSupRouterTmp = i;
							nextSubRouterTmp = bestReIndexTmp;
						}
					}

				}
			} catch (Exception e) {
				Main.nextSupRouter = 0;
				Main.nextSubRouter = 2;
			}
			if(!isOk){
				//寻找最近目标小区作为下一个序列点
				if(nextSupRouterTmp == 0 && nextSubRouterTmp == 0){
					for (int i = 0; i < len && isOk != true; i++) {
						nextSupRouterTmp = i;
						tmp = routePrePlan.get(i);
						int lenJ = tmp.length();
						for(int j = 2; j < lenJ; j++){
							int idxSub = Integer.parseInt( tmp.substring(j, j+1) );
							if( Main.x1.get(idxSub) != curX && Main.y1.get(idxSub) != curY ){
								if(Main.weightArr.get(idxSub) > 0 ){
									nextSubRouterTmp = j;
									isOk = true;
									break;
								}
							}
							j += 1;
						}
					}
				}
				Main.nextSupRouter = nextSupRouterTmp;
				Main.nextSubRouter = nextSubRouterTmp;
				
			}
		} else {
			if ( Main.curType == 2 ) {
				// 捐款小区
				int len = routePrePlan.size();
				try {
					for (int i = 0; i < len; i++) {
						tmp = routePrePlan.get(i);

						idxTmp = Integer.parseInt(tmp.substring(2, 3));
						Main.nextSupRouter = i;
						Main.nextSubRouter = 2;
						if (!(weightArr.get(idxTmp) == -100 && Main.x1.get(idxTmp) == Main.xStore
								&& Main.y1.get(idxTmp) == Main.yStore) && Main.x1.get(idxTmp) != curX
								&& Main.y1.get(idxTmp) != curY) {
							// 非仓库且禁止原地打转
							int sumCap = Main.veh.getvCapacity() - Main.weightArr.get(idxTmp);
							if (sumCap > Main.MaxCapacity) {
								// 下一个捐款小区失败
								int bestReIndexTmp = tmp.indexOf("" + bestReIndex);
								if (bestReIndexTmp != -1) {
									nextSupRouterTmp = i;
									nextSubRouterTmp = bestReIndexTmp;
									isOk = true;
								}
								continue;
							} else {
								// 目标小区或者捐款小区
								isOk = true;
								break;
							}
						}

					}
				} catch (Exception e) {
					Main.nextSupRouter = 0;
					Main.nextSubRouter = 2;
				}

				if(!isOk){
					//寻找最近目标小区作为下一个序列点
					if( nextSupRouterTmp == 0 && nextSubRouterTmp == 0 ){
						for (int i = 0; i < len && isOk != true; i++) {
							nextSupRouterTmp = i;
							tmp = routePrePlan.get(i);
							int lenJ = tmp.length();
							for(int j = 2; j < lenJ; j++){
								int idxSub = Integer.parseInt( tmp.substring(j, j+1) );
								if( Main.x1.get(idxSub) != curX && Main.y1.get(idxSub) != curY ){
									int sumCap = Main.veh.getvCapacity() - Main.weightArr.get(idxSub);
									if(sumCap < Main.MaxCapacity ){
										nextSubRouterTmp = j;
										isOk = true;
										break;
									}
								}
								j += 1;
							}
						}
					}else{
						Main.nextSupRouter = nextSupRouterTmp;
						Main.nextSubRouter = nextSubRouterTmp;
					}

					
				}
				
			} else {
				// 当前是目标小区
				int  len = routePrePlan.size();
				try {
					for (int i = 0; i < len && isOk != true; i++) {
						tmp = routePrePlan.get(i);
						idxTmp = Integer.parseInt(tmp.substring(2, 3));
						if (Main.x1.get(idxTmp) != curX && Main.y1.get(idxTmp) != curY) {
							Main.nextSupRouter = i;
							Main.nextSubRouter = 2;
							if (Main.veh.getvCapacity() < weightArr.get(idxTmp) && weightArr.get(idxTmp) > 0) {
								// 回仓库
								int idxStoreTmp = 0;
								for (int j = 0; j < len; j++) {
									tmp = routePrePlan.get(j);
									idxStoreTmp = tmp.indexOf("" + idxStore);
									if (idxStoreTmp != -1) {
										Main.nextSupRouter = j;
										Main.nextSubRouter = idxStoreTmp;
										isOk = true;
										break;
									}
								}

							} else {
								if (Main.veh.getvCapacity() >= weightArr.get(idxTmp)) {
									// 回仓库
									isOk = true;
									break;
								}
							}
						}

					}
				} catch (Exception e) {
					Main.nextSupRouter = 0;
					Main.nextSubRouter = 2;
				}

				if (!isOk) {
					// 没有找到,回仓库
					String tmpStr;
					int idxStoreTmp = 2;
					for(int j = 0; j < len; j++){
						tmpStr = routePrePlan.get(j);
						idxStoreTmp = tmpStr.indexOf(""+idxStore);
						if( idxStoreTmp != -1){
							Main.nextSupRouter = j;
							Main.nextSubRouter = idxStoreTmp;
							break;
						}
					}

				}
			}

			
		}
	}
	
	public static void reCheckNextPoint(int curX, int curY, int idxCurPoint){
		
		int idxStore = 0;
		String tmpStr = "";
		for (int i = 1, len = x1.size(); i < len; i++) {
			if (x1.get(i) == Main.xStore && y1.get(i) == Main.yStore) {
				idxStore = i;
				break;
			}
		}
		boolean isOk = false;
		if( Main.curType == 1 ){
			//目标小区,返回仓库
			for(int i = 0, len = Main.routePrePlan.size(); i < len; i++){
				Main.nextSupRouter = i;
				tmpStr = Main.routePrePlan.get(i);
				int idx = tmpStr.indexOf(""+idxStore);
				if( idx != -1 ){
					Main.nextSubRouter = idx;
					isOk = true;
					break;
				}
			}
			if( isOk != true ){
				Main.nextSupRouter = 0;
				Main.nextSubRouter = 2;
			}
		}else{
			
			if( Main.curType == 2 ){
				//捐款小区
				for(int i = 0, len = Main.routePrePlan.size(); i < len; i++){
					Main.nextSupRouter = i;
					tmpStr = Main.routePrePlan.get(i);
					int idx = tmpStr.indexOf(""+idxCurPoint);
					if( idx != -1 ){
						if( tmpStr.length() > (idx+3) ){
							isOk = true;
							Main.nextSubRouter = idx+2;
							break;
						}else{
							if( tmpStr.length() == (idx+2) ){
								if(i != len - 1){
									String tmpStrSub;
									for(int j = i; j < len; j++){
										tmpStrSub = Main.routePrePlan.get(j);
										for(int k = 0, lenK = tmpStrSub.length(); k < lenK; k++){
											int tmpK = Integer.parseInt( tmpStrSub.substring(k, k+1) );
											if( Main.weightArr.get(tmpK) > 0 ){
												//目标小区
												Main.nextSupRouter = j;
												Main.nextSubRouter = k;
												isOk = true;
												break;
											}
										}
										
									}
									break;
								}
							}
						}
					}
				}
				if( isOk != true){
					int idxTarget = getBestDistanceReIndex(curX, curY);
					String tmpStrSub = "";
					for(int j = 0, len = Main.routePrePlan.size(); j < len; j++){
						tmpStrSub = Main.routePrePlan.get(j);
						int idxSub = tmpStrSub.indexOf(""+idxTarget);
						if(idxSub != -1){
							Main.nextSupRouter = j;
							Main.nextSubRouter = idxSub;
							isOk = true;
							break;
						}
					}
				}
				
			}
		}
		
	}

}

class VehicleRoutingProblem {
	static final int countGA = 40;// 默认遗传50代
	static final int max = 101;
	static final int maxqvehicle = 1024;
	static final int maxdvehicle = 1024;
	Random ra = new Random();
	int K;// 最多使用车数目
	int KK;// 实际使用车数
	int clientNum;// 客户数目,染色体长度
	double punishWeight;// W1, W2, W3;//惩罚权重
	double crossRate, mutationRate;// 交叉概率和变异概率
	int populationScale;// 种群规模
	int T;// 进化代数
	int t;// 当前代数
	int[] bestGhArr;// 所有代数中最好的染色体
	double[] timeGhArr;// 所有代数中最好的染色体
	double bestFitness;// 所有代数中最好的染色体的适应度
	int bestGenerationNum;// 最好的染色体出现的代数
	double decodedEvaluation;// 解码后所有车辆所走路程总和、
	double[][] vehicleInfoMatrix;// K下标从1开始到K，0列表示车的最大载重量，1列表示车行驶的最大距离，2列表示速度
	int[] decodedArr;// 染色体解码后表达的每辆车的服务的客户的顺序
	double[][] distanceMatrix;// 客户距离
	double[] weightArr;// 客户需求量
	int[][] oldMatrix;// 初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
	int[][] newMatrix;// 新的种群，子代种群
	double[] fitnessArr;// 种群适应度，表示种群中各个个体的适应度
	double[] probabilityArr;// 种群中各个个体的累计概率
	double[] x1;
	double[] y1;

	// 初始化函数
	void initData(int clientNUM, double px[], double[] py, double[] weights, int capacity) {
		int i, j;
		decodedEvaluation = 0;// 解码后所有车辆所走路程总和
		punishWeight = 300;// 车辆超额惩罚权重
		// clientNum = 6;// 客户数目,染色体长度
		clientNum = clientNUM;
		K = 10;// 最大车数目
		populationScale = 70;// 种群规模
		crossRate = 0.9;// 交叉概率
		mutationRate = 0.09;// 变异概率，实际为(1-Pc)*0.9=0.09
		T = 2000;// 进化代数
		bestFitness = 0;// 所有代数中最好的染色体的适应度
		vehicleInfoMatrix = new double[K + 2][3];// K下标从1开始到K，0列表示车的最大载重量，1列表示车行驶的最大距离，2列表示速度
		bestGhArr = new int[clientNum];// 所有代数中最好的染色体
		timeGhArr = new double[clientNum];// 所有代数中最好的染色体
		decodedArr = new int[clientNum + 1];// 染色体解码后表达的每辆车的服务的客户的顺序
		distanceMatrix = new double[clientNum + 1][clientNum + 1];// 客户距离
		weightArr = new double[clientNum + 1];// 客户需求量
		oldMatrix = new int[populationScale][clientNum];// 初始种群，父代种群，行数表示种群规模，一行代表一个个体，即染色体，列表示染色体基因片段
		newMatrix = new int[populationScale][clientNum];// 新的种群，子代种群
		fitnessArr = new double[populationScale];// 种群适应度，表示种群中各个个体的适应度
		probabilityArr = new double[populationScale];// 种群中各个个体的累计概率
		x1 = new double[clientNum + 1];
		y1 = new double[clientNum + 1];
		// 车辆最大载重和最大行驶
		vehicleInfoMatrix[1][0] = capacity;
		vehicleInfoMatrix[1][1] = VehicleRoutingProblem.maxdvehicle;
		for (int v = 2; v <= K; v++) {
			vehicleInfoMatrix[v][0] = capacity;
			vehicleInfoMatrix[v][1] = VehicleRoutingProblem.maxdvehicle;
		}
		// vehicleInfoMatrix[6][0] = maxqvehicle;// 限制最大
		// vehicleInfoMatrix[6][1] = maxdvehicle;

		// 客户坐标
		x1 = px;
		y1 = py;
		weightArr = weights;

		double x = 0, y = 0;
		// 客户之间距离
		int endIndex = clientNum + 1;
		for (i = 0; i < endIndex; i++) {
			for (j = 0; j < endIndex; j++) {
				x = x1[i] - x1[j];
				y = y1[i] - y1[j];
				distanceMatrix[i][j] = Math.sqrt(x * x + y * y);
			}
		}

	}

	// 染色体评价函数，输入一个染色体，得到该染色体评价值
	double caculateFitness(int[] Gh) {
		// 染色体从下标0开始到L-1；
		int i, j;// i车的编号，j客户编号
		int flag;// 超额使用的车数
		double cur_d, cur_q, evaluation;// 当前车辆行驶距离，载重量，评价值，即各车行驶总里程

		cur_d = distanceMatrix[0][Gh[0]];// Gh[0]表示第一个客户，
		cur_q = weightArr[Gh[0]];

		i = 1;// 从1号车开始，默认第一辆车能满足第一个客户的需求
		evaluation = 0;// 评价值初始为0
		flag = 0;// 表示车辆数未超额

		for (j = 1; j < clientNum; j++) {
			cur_q = cur_q + weightArr[Gh[j]];
			cur_d = cur_d + distanceMatrix[Gh[j]][Gh[j - 1]];

			// 如果当前客户需求大于车的最大载重，或者距离大于车行驶最大距离，调用下一辆车
			if (cur_q > vehicleInfoMatrix[i][0])// 还得加上返回配送中心距离
			{

				i = i + 1;// 使用下一辆车
				evaluation = evaluation + cur_d - distanceMatrix[Gh[j]][Gh[j - 1]] + distanceMatrix[Gh[j - 1]][0];// 回到配送中心
				cur_d = distanceMatrix[0][Gh[j]];// 从配送中心到当前客户j距离(重新配车：从配送中到客户j行驶)
				cur_q = weightArr[Gh[j]];
			}
		}

		evaluation = evaluation + cur_d + distanceMatrix[Gh[clientNum - 1]][0];// 加上最后一辆车走的距离
		flag = i - K;// 看车辆使用数目是否大于规定车数，最多只超一辆车
		if (flag < 0)
			flag = 0;// 不大于则不惩罚

		evaluation = evaluation + flag * punishWeight;// 超额车数乘以惩罚权重
		return 10 / evaluation;// 压缩评价值

	}

	int[] getDecoding(int[] Gh) {
		int[] decodeArrTmp = new int[clientNum + 1];
		int i, j;// i车的编号，j客户编号
		double cur_d, cur_q, evaluation;// 当前车辆行驶距离，载重量，评价值，即各车行驶总里程
		cur_d = distanceMatrix[0][Gh[0]];// Gh[0]表示第一个客户，
		cur_q = weightArr[Gh[0]];
		i = 1;// 从1号车开始，默认第一辆车能满足第一个客户的需求
		decodeArrTmp[i] = 1;
		for (j = 1; j < clientNum; j++) {
			cur_q = cur_q + weightArr[Gh[j]];
			cur_d = cur_d + distanceMatrix[Gh[j]][Gh[j - 1]];
			// 如果当前客户需求大于车的最大载重，或者距离大于车行驶最大距离，调用下一辆车
			if (cur_q > vehicleInfoMatrix[i][0] || cur_d + distanceMatrix[Gh[j]][0] > vehicleInfoMatrix[i][1]) {
				i = i + 1;// 使用下一辆车
				decodeArrTmp[i] = decodeArrTmp[i - 1] + 1;
				cur_d = distanceMatrix[0][Gh[j]];// 从配送中心到当前客户j距离
				cur_q = weightArr[Gh[j]];
			} else {
				decodeArrTmp[i] = decodeArrTmp[i] + 1;//
			}
		}
		return decodeArrTmp;

	}

	// 染色体解码函数，输入一个染色体，得到该染色体表达的每辆车的服务的客户的顺序
	void decoding(int[] Gh) {
		// 染色体从下标0开始到L-1；
		int i, j;// i车的编号，j客户编号
		double cur_d, cur_q, evaluation;// 当前车辆行驶距离，载重量，评价值，即各车行驶总里程
		cur_d = distanceMatrix[0][Gh[0]];// Gh[0]表示第一个客户，
		cur_q = weightArr[Gh[0]];
		i = 1;// 从1号车开始，默认第一辆车能满足第一个客户的需求
		decodedArr[i] = 1;
		evaluation = 0;
		for (j = 1; j < clientNum; j++) {
			cur_q = cur_q + weightArr[Gh[j]];
			cur_d = cur_d + distanceMatrix[Gh[j]][Gh[j - 1]];
			// 如果当前客户需求大于车的最大载重，或者距离大于车行驶最大距离，调用下一辆车
			if (cur_q > vehicleInfoMatrix[i][0] || cur_d + distanceMatrix[Gh[j]][0] > vehicleInfoMatrix[i][1]) {
				i = i + 1;// 使用下一辆车
				decodedArr[i] = decodedArr[i - 1] + 1;//
				evaluation = evaluation + cur_d - distanceMatrix[Gh[j]][Gh[j - 1]] + distanceMatrix[Gh[j - 1]][0];
				cur_d = distanceMatrix[0][Gh[j]];// 从配送中心到当前客户j距离
				cur_q = weightArr[Gh[j]];
			} else {
				decodedArr[i] = decodedArr[i] + 1;//
			}
		}
		decodedEvaluation = evaluation + cur_d + distanceMatrix[Gh[clientNum - 1]][0];// 加上最后一辆车走的距离
		KK = i;

	}

	// 初始化种群
	void initGroup() {
		int i, k;
		int randomNum = 0;
		for (k = 0; k < populationScale; k++)// 种群数
		{
			for (i = 0; i < clientNum; i++)
				oldMatrix[k][i] = i + 1;
			for (i = 0; i < clientNum; i++) {
				randomNum = ra.nextInt(clientNum);
				swap(oldMatrix[k], i, randomNum);
				// TODO:sjg0
				if (weightArr[oldMatrix[k][0]] < 0 && (vehicleInfoMatrix[1][0] - weightArr[oldMatrix[k][0]]) > 100) {
					swap(oldMatrix[k], randomNum, i);
				}
			}
		}

		// 显示初始化种群
		// System.out.println("///////////////显示初始种群开始(In initGroup
		// method)////////////////////");
		// for (k = 0; k < populationScale; k++)
		// System.out.println(Arrays.toString(oldMatrix[k]));
		// System.out.println("///////////////显示初始种群结束////////////////////");
	}

	public void swap(int arr[], int index1, int index2) {
		int temp = arr[index1];
		arr[index1] = arr[index2];
		arr[index2] = temp;
	}

	// 计算种群中各个个体的累积概率，前提是已经计算出各个个体的适应度Fitness[max]，作为赌轮选择策略一部分，Pi[max]
	void countRate() {
		int k;
		double sumFitness = 0;// 适应度总和

		for (k = 0; k < populationScale; k++) {
			sumFitness += fitnessArr[k];
		}

		// 计算各个个体累计概率
		probabilityArr[0] = fitnessArr[0] / sumFitness;
		for (k = 1; k < populationScale; k++) {
			probabilityArr[k] = fitnessArr[k] / sumFitness + probabilityArr[k - 1];
		}
	}

	// 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
	void copyChrosome(int k, int kk) {
		System.arraycopy(oldMatrix[kk], 0, newMatrix[k], 0, clientNum);
	}

	// 挑选某代种群中适应度最高的个体，直接复制到子代中，前提是已经计算出各个个体的适应度Fitness[max]
	void selectBestChrosome() {
		int k, maxid;
		double maxevaluation;
		maxid = 0;
		maxevaluation = fitnessArr[0];
		for (k = 1; k < populationScale; k++) {
			if (maxevaluation < fitnessArr[k]) {
				maxevaluation = fitnessArr[k];
				maxid = k;
			}
		}

		if (bestFitness < maxevaluation) {
			bestFitness = maxevaluation;
			bestGenerationNum = t;// 最好的染色体出现的代数;
			System.arraycopy(oldMatrix[maxid], 0, bestGhArr, 0, clientNum);
			// TODO:sjg1
			// for (int i = 0; i < bestGhArr.length; i++) {
			// System.out.print(bestGhArr[i] + " ");
			// }
			// System.out.println();
		}
		// 复制染色体，k表示新染色体在种群中的位置，kk表示旧的染色体在种群中的位置
		copyChrosome(0, maxid);// 将当代种群中适应度最高的染色体k复制到新种群中，排在第一位0
	}

	// 产生随机数

	int select() {
		int k;
		double ran1;
		ran1 = Math.abs(ra.nextDouble());
		for (k = 0; k < populationScale; k++) {
			if (ran1 <= probabilityArr[k]) {
				break;
			}
		}
		return k;
	}

	// 类OX交叉算子,交叉算子不够优秀
	void oxCrossover(int k1, int k2) {
		int i, j, k, flag;
		int ran1, ran2, temp;
		int[] Gh1 = new int[clientNum];
		int[] Gh2 = new int[clientNum];
		ran1 = ra.nextInt(clientNum);
		ran2 = ra.nextInt(clientNum);
		while (ran1 == ran2)
			ran2 = ra.nextInt(clientNum);
		if (ran1 > ran2)// 确保ran1<ran2
		{
			temp = ran1;
			ran1 = ran2;
			ran2 = temp;
		}
		flag = ran2 - ran1 + 1;// 删除重复基因前染色体长度

		for (i = 0, j = ran1; i < flag; i++, j++) {
			Gh1[i] = newMatrix[k2][j];
			Gh2[i] = newMatrix[k1][j];
		}
		// 已近赋值i=ran2-ran1个基因
		for (k = 0, j = flag; j < clientNum; j++)// 染色体长度
		{
			i = 0;
			while (i != flag) {
				Gh1[j] = newMatrix[k1][k++];
				i = 0;
				while (i < flag && Gh1[i] != Gh1[j])
					i++;
			}
		}

		for (k = 0, j = flag; j < clientNum; j++)// 染色体长度
		{
			i = 0;
			while (i != flag) {
				Gh2[j] = newMatrix[k2][k++];
				i = 0;
				while (i < flag && Gh2[i] != Gh2[j])
					i++;
			}
		}
		System.arraycopy(Gh1, 0, newMatrix[k1], 0, clientNum);
		System.arraycopy(Gh2, 0, newMatrix[k2], 0, clientNum);
	}

	// 对种群中的第k个染色体进行变异
	void mutation(int k) {
		int ran1, ran2;
		ran1 = ra.nextInt(clientNum);
		ran2 = ra.nextInt(clientNum);
		while (ran1 == ran2)
			ran2 = ra.nextInt(clientNum);
		swap(newMatrix[k], ran1, ran2);

	}

	// 进化函数，保留最优
	void evolution() {
		int k, selectId;
		double r;// 大于0小于1的随机数
		// 挑选某代种群中适应度最高的个体
		selectBestChrosome();
		// 赌轮选择策略挑选scale-1个下一代个体
		for (k = 1; k < populationScale; k++) {
			selectId = select();
			copyChrosome(k, selectId);
		}
		for (k = 1; k + 1 < populationScale / 2; k = k + 2) {
			r = Math.abs(ra.nextDouble());
			// crossover
			if (r < crossRate) {
				oxCrossover(k, k + 1);// 进行交叉
			} else {
				r = Math.abs(ra.nextDouble());
				if (r < mutationRate) {
					mutation(k);
				}
				r = Math.abs(ra.nextDouble());
				if (r < mutationRate) {
					mutation(k + 1);
				}
			}
		}
		if (k == populationScale / 2 - 1)// 剩最后一个染色体没有交叉L-1
		{
			r = Math.abs(ra.nextDouble());
			if (r < mutationRate) {
				mutation(k);
			}
		}

	}

	public BestResult solveVrp(int clientNUM, double[] px, double[] py, double[] weights, int capacity,
			boolean isLast) {
		int i, j, k;
		BestResult bestResult = new BestResult();
		// 初始化数据，不同问题初始化数据不一样
		initData(clientNUM, px, py, weights, capacity);

		// 初始化种群
		initGroup();
		int[] tempGA = new int[clientNum];

		// 计算初始化种群适应度，Fitness[max]
		for (k = 0; k < populationScale; k++) {
			for (i = 0; i < clientNum; i++) {
				tempGA[i] = oldMatrix[k][i];
			}

			fitnessArr[k] = caculateFitness(tempGA);
		}

		// 计算初始化种群中各个个体的累积概率，Pi[max]
		countRate();
		for (t = 0; t < T; t++) {
			evolution();// 进化函数，保留最优
			// 将新种群newMatrix复制到旧种群oldMatrix中，准备下一代进化
			for (k = 0; k < populationScale; k++) {
				// TODO:sjg0
				if (weightArr[newMatrix[k][0]] < 0) {
					continue;
				} else {
					int tek;
					int curCapacity = (int) vehicleInfoMatrix[1][0];
					int[] decodeArrTmp = getDecoding(newMatrix[k]);
					for (int h = 1; h <= KK; h++) {
						curCapacity = (int) vehicleInfoMatrix[1][0];
						tek = decodeArrTmp[h - 1];
						for (int f = tek; f < decodeArrTmp[h]; f++) {
							curCapacity -= weightArr[newMatrix[k][f]];
							if (curCapacity < 0) {
								curCapacity = 0;
							}
							if (curCapacity > 100) {
								break;
							}
						}
						if (curCapacity > 100) {
							break;
						}

					}
					if (curCapacity > 100) {
						continue;
					} else {
						System.arraycopy(newMatrix[k], 0, oldMatrix[k], 0, clientNum);
					}
				}
				// System.arraycopy(newMatrix[k], 0, oldMatrix[k], 0,
				// clientNum);
			}
			// 计算种群适应度，Fitness[max]
			for (k = 0; k < populationScale; k++) {
				System.arraycopy(oldMatrix[k], 0, tempGA, 0, clientNum);
				fitnessArr[k] = caculateFitness(tempGA);
			}
			// 计算种群中各个个体的累积概率，Pi[max]
			countRate();
			// 进度条
		}
		// 最后种群
		// System.out.println("//////////////////////////////////");
		// for (k = 0; k < populationScale; k++)
		// System.out.println(Arrays.toString(oldMatrix[k]));
		// System.out.println("\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\t");
		// 出现代数
		// System.out.println("最好的代数出现在：" + bestGenerationNum + "代");
		// 染色体评价值
		// System.out.println("最好的结果为：" + (10 / bestFitness) + "或" +
		// bestFitness);
		// 最好的染色体
		// System.out.println(Arrays.toString(bestGhArr));
		// 最好的染色体解码
		decoding(bestGhArr);
		// 使用车数
		// System.out.println("使用车数：" + KK);
		// 解码
		// System.out.println("车辆解码：" + Arrays.toString(decodedArr));
		// System.out.println("车辆行驶距离解码：" + decodedEvaluation);
		String tefa = "";
		int tek;
		int[] templ = new int[max];

		// TODO(sjg):收到遗传到最后一代的消息，进一步返回路线序列
		List<String> resultOrdered = null;
		if (isLast == true) {
			resultOrdered = new ArrayList<>();
		}
		for (i = 1; i <= KK; i++) {
			templ[1] = 0;
			tefa = "0-";
			tek = decodedArr[i - 1];
			for (j = tek, k = 2; j < decodedArr[i]; j++, k++) {
				tefa = tefa + bestGhArr[j] + "-";
				templ[k] = bestGhArr[j];
			}
			if (resultOrdered != null) {
				resultOrdered.add(tefa + "0");
			}
			templ[k] = 0;
			templ[0] = k;
			tefa = k + "-" + tefa + "0";
			// System.out.println(tefa);
		}
		//bestResult.setBestFitness(10 / bestFitness);
		//bestResult.setBestGenerationNum(bestGenerationNum);
		bestResult.setRouteOrdered(resultOrdered);
		return bestResult;
	}

	public List<String> run(int numGA, int countClient, double[] px, double[] py, double[] weights, int capacity) {
		//int count = numGA == 0 ? countGA : numGA;
		//double generationNum = 0;
		//double totalFitness = 0;
		VehicleRoutingProblem.BestResult bestResult = null;
		bestResult = this.solveVrp(countClient, px, py, weights, capacity, true);
		//for (int i = 0; i < count; i++) {
			// System.out.println("/////////the " + (i + 1) + "iteration
			// start...////////");
			//bestResult = this.solveVrp(countClient, px, py, weights, capacity, i == (count - 1));
			//totalFitness += bestResult.getBestFitness();
			//generationNum += bestResult.getBestGenerationNum();
			// System.out.println("/////////the " + (i + 1) + "iteration
			// end...////////");
			// System.out.println();
		//}
		// System.out.println("平均在第" + (generationNum / count) + "代找到最有解。");
		// System.out.println("平均的路成为：" + (totalFitness / count));
		return bestResult.getRouteOrdered();
	}

	public class BestResult {
		private double bestFitness;
		private int bestGenerationNum;
		private List<String> routeOrdered = new ArrayList<>();

		public int getBestGenerationNum() {
			return bestGenerationNum;
		}

		public void setBestGenerationNum(int bestGenerationNum) {
			this.bestGenerationNum = bestGenerationNum;
		}

		public double getBestFitness() {
			return bestFitness;
		}

		public void setBestFitness(double bestFitness) {
			this.bestFitness = bestFitness;
		}

		public List<String> getRouteOrdered() {
			return routeOrdered;
		}

		public void setRouteOrdered(List<String> routeOrdered) {
			this.routeOrdered = routeOrdered;
		}

	}

}

class Vehicle {
	private int vX;
	private int vY;
	private int vCapacity = Main.MaxCapacity;

	public int getvX() {
		return vX;
	}

	public void setvX(int vX) {
		this.vX = vX;
	}

	public int getvY() {
		return vY;
	}

	public void setvY(int vY) {
		this.vY = vY;
	}

	/**
	 * 获取车的当前已载重量
	 * 
	 * @return
	 */
	public int getvCapacity() {
		return vCapacity;
	}

	/**
	 * 设置车的载重
	 * 
	 * @param vCapacity
	 */
	public void setvCapacity(int vCapacity) {
		this.vCapacity = vCapacity;
	}

	@Override
	public String toString() {
		return "Vehicle [vX=" + vX + ", vY=" + vY + ", vCapacity=" + vCapacity + "]";
	}
}
