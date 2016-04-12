/**
 * 实现代码文件
 * 
 * @author XXX
 * @since 2016-3-4
 * @version V1.0
 */
package com.routesearch.route;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import com.filetool.util.FileUtil;

public final class Route
{
	
	static final int MAXCOST = 15000; //因为路线权重为[1,20]内的整数，所以总权重最大为
    								  //20*600=12000，所以用15000表示无穷大，表示不可达路径。
	static Assist assist = new Assist(); //辅助函数对象
	static int backTrackingMinCost = MAXCOST; //记录找到路径的权重信息
	static String backTrackingPath = ""; //记录找到的路径边信息
	
	/**
     * 你需要完成功能的入口
     * 
     * @author 
     * @since 2016-3-4
     * @version V1
     */
    public static String searchRoute(String graphContent, String condition, String resultFilePath)
    {
    	
    	//输入信息解析
    	List<Object> resolverList = resolverInput(graphContent, condition);
    	int adjMatrix[][] = (int [][])resolverList.get(0); //图的邻接矩阵
    	int indexEdge[][] = (int[][])resolverList.get(1); //
    	int travelNodes[] = (int [])resolverList.get(2);
    	int source = (int)resolverList.get(3);
    	int destination = (int)resolverList.get(4);
    	int edgeNum = (int)resolverList.get(5);
    	int totalNodesNum = adjMatrix.length;
    	
    	System.out.println("edgeNum:"+edgeNum);
    	
    	if(edgeNum<350){
    		if(edgeNum > 200){ 
    			
    			int[][][] allPathSet = simplifiedTopo(adjMatrix,travelNodes,source,destination);
    			int[][][] costTbls = getCostTbls_V5(allPathSet,travelNodes,source,destination);
    			backTracking_V8(allPathSet,costTbls,travelNodes,source,destination,
            			totalNodesNum,indexEdge,edgeNum,resultFilePath);
            	
    			//bruteDFS_V2(adjMatrix,travelNodes,source,destination,indexEdge,edgeNum,resultFilePath);
    		}else{
    			bruteDFS(adjMatrix,travelNodes,source,destination,indexEdge,edgeNum,resultFilePath);
    		}
    	}else{
    		int[][][] allPathSet;
    		int[][][] costTbls;
    		if(edgeNum > 2000){
    			if(travelNodes.length > 30){ 
    				//通过Dij化简图算法得到costTbls
    				allPathSet = needNodesDij_V2(adjMatrix,travelNodes,source,destination);
    				costTbls = getCostTbls_V2(allPathSet,travelNodes,source,destination);

    			}else{
    				//通过simplifiedTopo化简图算法得到costTbls
    				allPathSet = simplifiedTopo(adjMatrix,travelNodes,source,destination);
    				costTbls = getCostTbls_V5(allPathSet,travelNodes,source,destination);
    			}
    		}else{
    			//通过Dij化简图算法得到costTbls
				allPathSet = needNodesDij_V2(adjMatrix,travelNodes,source,destination);
				costTbls = getCostTbls_V2(allPathSet,travelNodes,source,destination);

				
    		}
    		
    		backTracking_V8(allPathSet,costTbls,travelNodes,source,destination,
        			totalNodesNum,indexEdge,edgeNum,resultFilePath);
    	}
    	
    	
    	//bruteDFS(adjMatrix,travelNodes,source,destination,indexEdge,edgeNum,resultFilePath);
    	
    	return backTrackingPath;
    	
    }
    
    /**
     * 通过节点删除进行图结构的化简，将最后图化简得到的路径存到allPathSet
     * @return allPathSet[][][] 包含所有路径的路径存储矩阵，是三维矩阵，前两维分别代表路径的起点和终点
     */
    public static int[][][] simplifiedTopo(int[][] adjMatrix, int[] travelNodes, int source, 
    		int destination){
    	
    	int totalNodesNum = adjMatrix.length;
    	//初始化路径矩阵
    	int[][][] allPathSet = new int[totalNodesNum][totalNodesNum][3];//路径存放矩阵
    	for(int i=0;i<totalNodesNum;i++){
    		for(int j=0;j<totalNodesNum;j++){
    			int[] temp = new int[3];
    			temp[0] = i;
    			temp[1] = j;
    			temp[2] = adjMatrix[i][j];//最后一位代表权重
    			allPathSet[i][j] = temp;
    		}
    	}
    	
    	//构造源点，必经节点和终点的必访问点
    	int[] keyNodes = new int[travelNodes.length+2];
    	keyNodes[0] = source;
    	for(int i=0;i<travelNodes.length;i++){
    		keyNodes[i+1] = travelNodes[i];
    	}
    	keyNodes[keyNodes.length-1] = destination;
    	
    	int nodeId = 0;
    	while(nodeId<totalNodesNum){
    		//如果该节点为必须访问点，则不进行删除操作
    		if(assist.isContained(keyNodes, nodeId)){
    			nodeId++;
    			continue;
    		}
    		Stack<Integer> inNodesStack = new Stack<Integer>();
    		Stack<Integer> outNodesStack = new Stack<Integer>();
    		//通过访问adjMatrix获取当前nodeId对应的入度节点
    		for(int i=0;i<totalNodesNum;i++){
    			if(adjMatrix[i][nodeId] == MAXCOST || adjMatrix[i][nodeId] == 0){
    				continue;
    			}
    			int inNode = i;
    			inNodesStack.push(inNode);//保存当前nodeId的入度节点，用于最后的nodeId出入度节点删除
    			//获取当前nodeId对应的出度节点
    			for(int j=0;j<totalNodesNum;j++){
    				if(adjMatrix[nodeId][j] == MAXCOST || adjMatrix[nodeId][j] == 0){
    					continue;
    				}
    				int outNode = j;
    				outNodesStack.push(outNode);//保存当前nodeId的出度节点，用于最后的nodeId出入度节点删除
    				//如果入度节点等于出度节点，不进行操作
    				if(inNode == outNode){
    					continue;
    				}
    				int inPathSize = allPathSet[inNode][nodeId].length;
    				int outPathSize = allPathSet[nodeId][outNode].length;
    				int in2OutPathSize = allPathSet[inNode][outNode].length;
    				if(allPathSet[inNode][nodeId][inPathSize-1]+allPathSet[nodeId][outNode][outPathSize-1] 
    						< allPathSet[inNode][outNode][in2OutPathSize-1]){
    					//更新路径表	
    					int[] temp = new int[inPathSize+outPathSize-2];
    					int index = 0;
    					int curCost = 0;
    					for(int k1=0;k1<inPathSize-1;k1++){
    						temp[index++] = allPathSet[inNode][nodeId][k1];
    					}
    					for(int k2=1;k2<outPathSize-1;k2++){
    						temp[index++] = allPathSet[nodeId][outNode][k2];
    					}
    					curCost = allPathSet[inNode][nodeId][inPathSize-1]+
    							allPathSet[nodeId][outNode][outPathSize-1];
    					temp[index] = curCost;
    					allPathSet[inNode][outNode] = temp;
    					//System.out.println(Arrays.toString(allPathSet[inNode][outNode]));
    					//更新邻接矩阵adjMatrix出入度（连接）信息,新建inNode到outNode的连接
    					adjMatrix[inNode][outNode] = curCost;
    						
    				}
    			}
    		}
    		//更新邻接矩阵adjMatrix出入度信息，删除nodeId的所有出入度节点
    		for(int i=0;i<inNodesStack.size();i++){
    			adjMatrix[inNodesStack.get(i)][nodeId] = MAXCOST;	
    		}
    		for(int j=0;j<outNodesStack.size();j++){
    			adjMatrix[nodeId][outNodesStack.get(j)] = MAXCOST;
    		}
    		nodeId++;
    	}
    	return allPathSet;
    }
    
    /**
     * 对于简单网络，暴力深度优先求解
     * 
     */
    
    public static void bruteDFS(int[][] adjMatrix, int[] travelNodes, int source,
    		int destination, int[][] indexEdge,int edgeNum, String resultFilePath){
    	
    	Stack<Integer> stack = new Stack<Integer>();
    	int maxNode = adjMatrix.length;
    	int layersCounter = 0;//回溯控制模块跳跃层数计数器
    	int maxLayer = 0;//回溯控制模块层数阈值
    	if(edgeNum > 200 && edgeNum < 350){
    		maxLayer = 1000;
    	}else{
    		maxLayer = 1000;//如果图的边数不在上述范围，则默认取消回溯控制模块
    	}
    	int[] isVisited = new int[maxNode]; //节点访问记录数组
    	int[][] track = new int[maxNode][maxNode];//当前节点为源点的邻接表访问记录表
    	for(int i=0;i<maxNode;i++){
    		for(int j=0;j<maxNode;j++){
    			track[i][j] = 0;
    		}
    	}

    	stack.push(source);
    	isVisited[source] = 1;
    	
    	while(!stack.empty()){
    		
    		if(stack.peek() == destination){
    			//进行路径合法化判定
    			int counter = 0;
    			for(int i=1;i<stack.size()-1;i++){
    				if(assist.isContained(travelNodes, stack.get(i))){
    					++counter;
    				}
    			}
    			if(counter == travelNodes.length){
    				int[] path = new int[stack.size()];
    				for(int i=0;i<stack.size();i++){
    					path[i] = stack.get(i);
    				}
    				
    				int currentCost = 0;
    				for(int i=0;i<path.length-1;i++){
    					currentCost += adjMatrix[path[i]][path[i+1]];
    				}
    				if(currentCost<backTrackingMinCost){
    					backTrackingMinCost = currentCost;
    					backTrackingPath = node2Edge(indexEdge,path);
    					FileUtil.write(resultFilePath, backTrackingPath, false);
    					//System.out.println("minCost:"+backTrackingMinCost);
    					//System.out.println("nodePath:"+Arrays.toString(path));
    					//System.out.println("optimalPath:"+backTrackingPath);
    				}
    				
    			}
    			
    			isVisited[stack.peek()] = 0;
    			for(int i=0;i<track[stack.peek()].length;i++){
    				track[stack.peek()][i] = 0;
    			}
    			stack.pop();
    			
    		}
    		
    		boolean needPop = true;
    		
			//如果回溯指定层数达到阈值则进行重新选路入栈
    		if(stack.size() == maxLayer){
    			layersCounter += 1;
    		}
    		if(layersCounter == 5){
    			layersCounter = 0;
    			System.out.println("进入全出栈阶段！！！");
    			while(stack.peek() != source){
    				isVisited[stack.peek()] = 0;
    				for(int i=0;i<track[stack.peek()].length;i++){
    					track[stack.peek()][i] = 0;
    				}
    				stack.pop();
    			}
    		}
			
    		int beginNode = stack.peek();
    		
    		for(int i=0;i<maxNode;i++){
    			if(adjMatrix[beginNode][i] != MAXCOST && isVisited[i] == 0
    					&& track[beginNode][i] == 0){
    				isVisited[i] = 1;
    				track[beginNode][i] = 1;
    				stack.push(i);
    				needPop = false;
    				break;
    			}
    		}
    		if(needPop){
    			isVisited[stack.peek()] = 0;
    			for(int i=0;i<track[stack.peek()].length;i++){
    				track[stack.peek()][i] = 0;
    			}
    			stack.pop();
    		}
    	}

    }
    
    /**
     * 采用百分比进行深度优先的层数控制
     */
    public static void bruteDFS_V2(int[][] adjMatrix, int[] travelNodes, int source,
    		int destination, int[][] indexEdge,int edgeNum, String resultFilePath){
    	
    	
    	Stack<Integer> stack = new Stack<Integer>();
    	int maxNode = adjMatrix.length;
    	double threshold = (double)(travelNodes.length+2)/maxNode;//计算路径放弃搜索时的百分比阈值
    	System.out.println("threshold:"+threshold);
    	double curPercent = 0;//路径当前的必经节点与总节点数的百分比
    	//构造包含源点，中间必经节点和终点的必经节点数组
    	int[] keyNodes = new int[travelNodes.length+2];
    	keyNodes[0] = source;
    	for(int i=0;i<travelNodes.length;i++){
    		keyNodes[i+1] = travelNodes[i];
    	}
    	keyNodes[keyNodes.length-1] = destination;
    	
    	int[] isVisited = new int[maxNode]; //节点访问记录数组
    	int[][] track = new int[maxNode][maxNode];//当前节点为源点的邻接表访问记录表
    	for(int i=0;i<maxNode;i++){
    		for(int j=0;j<maxNode;j++){
    			track[i][j] = 0;
    		}
    	}

    	stack.push(source);
    	isVisited[source] = 1;
    	
    	while(!stack.empty()){
    		
    		if(stack.peek() == destination){
    			//进行路径合法化判定
    			int counter = 0;
    			for(int i=1;i<stack.size()-1;i++){
    				if(assist.isContained(travelNodes, stack.get(i))){
    					++counter;
    				}
    			}
    			if(counter == travelNodes.length){
    				int[] path = new int[stack.size()];
    				for(int i=0;i<stack.size();i++){
    					path[i] = stack.get(i);
    				}
    				
    				int currentCost = 0;
    				for(int i=0;i<path.length-1;i++){
    					currentCost += adjMatrix[path[i]][path[i+1]];
    				}
    				if(currentCost<backTrackingMinCost){
    					backTrackingMinCost = currentCost;
    					backTrackingPath = node2Edge(indexEdge,path);
    					FileUtil.write(resultFilePath, backTrackingPath, false);
    					System.out.println("minCost:"+backTrackingMinCost);
    					System.out.println("nodePath:"+Arrays.toString(path));
    					System.out.println("optimalPath:"+backTrackingPath);
    				}
    				
    			}
    			
    			isVisited[stack.peek()] = 0;
    			for(int i=0;i<track[stack.peek()].length;i++){
    				track[stack.peek()][i] = 0;
    			}
    			stack.pop();
    			
    		}
    		
    		boolean needPop = true;
			//如果路径当前的必经节点数与总节点数的比小于指定阈值，则放弃搜索该路径
    		int keyNodesCounter = 0;
    		for(int i=0;i<stack.size()-1;i++){
				if(assist.isContained(keyNodes, stack.get(i))){
					keyNodesCounter++;
				}
			}
    		curPercent = (double)keyNodesCounter/stack.size();
    		//System.out.println("curPercent:"+curPercent);
    		if(curPercent < 0.33){
    			//System.out.println("当前路径不合理放弃继续搜索！！！");
    			if(stack.peek() != source){
    				isVisited[stack.peek()] = 0;
    				for(int i=0;i<track[stack.peek()].length;i++){
    					track[stack.peek()][i] = 0;
    				}
    				stack.pop();
    			}
    		}
			
    		int beginNode = stack.peek();
    		
    		for(int i=0;i<maxNode;i++){
    			if(adjMatrix[beginNode][i] != MAXCOST && isVisited[i] == 0
    					&& track[beginNode][i] == 0){
    				isVisited[i] = 1;
    				track[beginNode][i] = 1;
    				stack.push(i);
    				needPop = false;
    				break;
    			}
    		}
    		if(needPop){
    			isVisited[stack.peek()] = 0;
    			for(int i=0;i<track[stack.peek()].length;i++){
    				track[stack.peek()][i] = 0;
    			}
    			stack.pop();
    		}
    	}
    	
    }
    
    
    /**
     * 回溯法第八版，采用回溯法进行路径拼接，其中有层数返回控制模块
     * 
     */
    public static void backTracking_V8(int[][][] allPathSet, int[][][] costTbls, int[] travelNodes,
    		int source, int destination, int totalNodesNum, int[][] indexEdge, int edgeNum,
    		String resultFilePath){
    	
    	//构造源点，必经节点和终点的节点数组
    	int[] keyNodes = new int[travelNodes.length+2];
    	keyNodes[0] = source;
    	for(int i=0;i<travelNodes.length;i++){
    		keyNodes[i+1] = travelNodes[i];
    	}
    	keyNodes[keyNodes.length-1] = destination;
    	
    	//构造必经节点对的路径访问记录矩阵，costTbls中的每张表用track中的每行数据来表示
    	int sizeRow = costTbls.length;//表示共有多少张表
    	int sizeColumn = travelNodes.length;//每张表的行数
    	int[][] track = new int[sizeRow][sizeColumn];
    	for(int i=0;i<sizeRow;i++){
    		for(int j=0;j<sizeColumn;j++){
    			track[i][j] = 0;
    		}
    	}
    	//构造全部节点的访问记录数组
    	int[] isVisited = new int[totalNodesNum];
    	for(int i=0;i<totalNodesNum;i++){
    		isVisited[i] = 0;
    	}
    	//采用stack实现回溯法，对stack进行初始化
    	Stack<Integer> stack  = new Stack<Integer>();
    	stack.push(source);
    	isVisited[source] = 1;
    	
    	int counter = 0;//回溯到指定层的计数标志
    	int counterThreshold = 5;//层数计数阈值，默认为5
    	//返回层数阈值分情况设置
    	int maxLayer = 0;
    	if(edgeNum > 2000){
    		
    		if(travelNodes.length > 30){ 
    			maxLayer = 25;
    			//counterThreshold = 100;
    		}else if(travelNodes.length < 20){ 
    			maxLayer = 50;
    			//counterThreshold = 30;
    		}else{
    			maxLayer = 15;
    		}
    		
    	}else if(edgeNum > 1500){ 
    		maxLayer = 15;
    		//counterThreshold = 50;
    	}else{
    		maxLayer = 50;//超出上述判断范围，则默认取消路径跳跃搜索
    	}
    	
    	
    	while(!stack.empty()){
    		
    		boolean needPop = true;//是否需要出栈初始化为true
    		
    		//如果栈顶元素为终点则进行路径判断，看是否是满足要求的路径
    		if(stack.peek() == destination){
    			//如果源点，中间必经节点和终点都已访问则判断该条路径为合格路径
    			if(keyNodesCounter(isVisited,keyNodes) == keyNodes.length){
    				int[] printList = new int[stack.size()];
    				for(int i=0;i<stack.size();i++){
    					printList[i] = stack.get(i);
    				}
    				//通过searchEntirePath获取通过的所有节点
    				int[] optimalPathCost = searchEntirePath(allPathSet,printList);
    				if(optimalPathCost[optimalPathCost.length-1] <backTrackingMinCost){
    					//取出点集对应的边，即为要求的输出
    					backTrackingMinCost = optimalPathCost[optimalPathCost.length-1];
    					int[] nodeSet = new int[optimalPathCost.length-1];
    					for(int i=0;i<optimalPathCost.length-1;i++){
    						nodeSet[i] = optimalPathCost[i];
    					}
    					backTrackingPath = node2Edge(indexEdge,nodeSet);
    					FileUtil.write(resultFilePath, backTrackingPath, false);
    					//System.out.println("minCost:"+backTrackingMinCost);
    					//System.out.println("nodeTrack:"+Arrays.toString(nodeSet));
    					//System.out.println("optimalPath:"+backTrackingPath);
    				}
    			}
    			
    			//弹出中间必经节点最后一个节点,消除相关节点的访问记录,因为在此步骤中出栈元素为destination
    			//而其并没有邻接表，所以不对track二维数组进行操作
    			int curSour = stack.get(stack.size()-2);
    			int[] tblRowId = searchTblRowId(costTbls,curSour,destination);
    			int[] delPathItem = costTbls[tblRowId[0]][tblRowId[1]];
    			for(int i=1;i<delPathItem.length-1;i++){
    				isVisited[delPathItem[i]] = 0;
    			}
    			stack.pop();
    			
    		}
    		
    		/*
    		 * 当回溯指定层的次数超过阈值并且在这之前没有找到一条简单路径，则从源点开始重新寻找路径 
    		 */
    		if(stack.size() == maxLayer){
    			counter++;
    		}
    		if(counter == counterThreshold){
    			counter = 0;//回溯计数标志置0
    			//System.out.println("进入全出栈阶段！！！");
    			while(stack.peek() != source ){
    				int tblId = searchTblId(costTbls,stack.peek());
    				for(int i=0;i<track[tblId].length;i++){
    					track[tblId][i] = 0;
    				}
    				int curSour = stack.get(stack.size()-2);
    				int[] tblRowId = searchTblRowId(costTbls,curSour,stack.peek());
    				int[] popPathItem = costTbls[tblRowId[0]][tblRowId[1]];
    				for(int i=1;i<popPathItem.length-1;i++){
    					isVisited[popPathItem[i]] = 0;
    				}
    				stack.pop();
    			}
    		}
    		
    		int beginNode = stack.peek();
    		int tblId = searchTblId(costTbls,beginNode);
    		int[][] currentTbl = costTbls[tblId];
    		
    		for(int i=0;i<currentTbl.length;i++){
    			
    			if(track[tblId][i] == 1){ //如果该节点对已经被访问，直接跳过
    				continue;
    			}
    			int[] perPath = currentTbl[i];
    			int lastNode = perPath[perPath.length-2];
    			//路径合法性判断，包括当前节点是否已经走过和是否有环
    			boolean correctPath = true;
    			if(isVisited[lastNode] == 0){
    				for(int k=1;k<perPath.length-1;k++){
    					if(isVisited[perPath[k]] == 1){
    						correctPath = false;
    						break;
    					}
    				}
    			}else{
    				correctPath = false;
    			}
    			
    			if(correctPath && perPath[perPath.length-1]<MAXCOST){
    				for(int j=1;j<perPath.length-1;j++){
    					isVisited[perPath[j]] = 1;
    				}
    				track[tblId][i] = 1; //表示该节点对已被访问过
    				stack.push(lastNode);
    				needPop = false;
    				break;
    			}
    		}
    		if(needPop == true){
    			if(stack.size() >= 2){
    				//stack中索引是从栈底到栈顶
    				int currentDes = stack.peek();
    				int popIndex = searchTblId(costTbls,currentDes);
    				for(int i=0;i<track[popIndex].length;i++){
    					track[popIndex][i] = 0;
    				}
    				int currentSour = stack.get(stack.size()-2);
    				int[] delTblRowId = searchTblRowId(costTbls,currentSour,currentDes);
    				int[] delPathAndCost = costTbls[delTblRowId[0]][delTblRowId[1]];
    				for(int i=1;i<delPathAndCost.length-1;i++){
    					isVisited[delPathAndCost[i]] = 0;
    				}
    			}
    			stack.pop();
    		}
    	}
    	
    }
    
    /**
     * 统计必经节点已访问的数目
     * 
     */
    public static int keyNodesCounter(int[] isVisited, int[] keyNodes){
    	
    	int counter = 0;
    	for(int i=0;i<keyNodes.length;i++){
    		if(isVisited[keyNodes[i]] == 1){
    			counter += 1;
    		}
    	}
    	return counter;
    }
    
    
    /**
     * 通过必经节点数组（包括源点和终点），获取包含所有通过节点的完整路径
     * 
     */
    public static int[] searchEntirePath(int[][][] allPathSet,int[] keyNodes){
    	StringBuffer sb = new StringBuffer();
    	String entirePath = null;
    	int minCost = 0;
    	for(int i=0;i<keyNodes.length-1;i++){
    		for(int j=0;j<allPathSet.length;j++){
    			if(keyNodes[i] == allPathSet[j][0][0]){
    				int[] perPath = allPathSet[j][keyNodes[i+1]];
    				for(int k=0;k<perPath.length-2;k++){
    					sb.append(perPath[k]);
    					sb.append("|");
    				}
    				minCost += perPath[perPath.length-1];
    				break;
    			}
    		}
    	}
    	sb.append(keyNodes[keyNodes.length-1]);
    	entirePath = sb.toString();

    	String[] pathItem = entirePath.split("\\D");
    	int[] pathAndCost = new int[pathItem.length+1];
    	for(int i=0;i<pathItem.length;i++){
    		pathAndCost[i] = assist.String2Int(pathItem[i]);
    	}
    	pathAndCost[pathAndCost.length-1] = minCost;
    	return pathAndCost;
    }
    
    /**
     * 通过节点对确定costTbl中对应路径的表索引和在该表中的行索引
     */
    public static int[] searchTblRowId(int[][][] costTbls,int firstNode,int lastNode){
    	int[] result = new int[2];
    	int tblIndex = 0;
    	int rowIndex = 0;
    	for(int i=0;i<costTbls.length;i++){
    		if(firstNode == costTbls[i][0][0]){ 
    			tblIndex = i;
    			break;
    		}
    	}
    	for(int j=0;j<costTbls[tblIndex].length;j++){
    		int[] perRow = costTbls[tblIndex][j];
    		if(lastNode == perRow[perRow.length-2]){//每行数组最后一个元素为权重，倒数第二才为该段路径的终点
    			rowIndex = j;
    			break;
    		}
    	}
    	result[0] = tblIndex;
    	result[1] = rowIndex;
    	return result;
    }
    
    /**
     * 通过起始节点确定costTbl中的表索引
     */
    
    public static int searchTblId(int[][][] costTbls, int firstNode){
    	int tblIndex = 0;
    	for(int i=0;i<costTbls.length;i++){
    		if(firstNode == costTbls[i][0][0]){
    			tblIndex = i;
    			break;
    		}
    	}
    	return tblIndex;
    }
    
    /**
     * 第二版getCostTbls，输入数据为needNodesDij_V2的三维数组，与采用Dij化简图的算法对应
     * @return 输出为必经节点数+1张表（包含源点），每张表为以源点和必经节点为起始点的路径数据，
     * 表内路径按照规定顺序排列，第一张表为源点对应的路径，后面的为必经节点在travelNodes中的次序排列,
     * 每张表中删除了路径中包含其他必经节点的路径
     */
    
    public static int[][][] getCostTbls_V2(int[][][] allPathSet, int[] travelNodes, 
    		int source, int destination){
    	
    	//构造包含源点终点和中间必经点的访问节点
    	int[] keyNodes = new int[travelNodes.length+2];
    	keyNodes[0] = source;
    	for(int i=0;i<travelNodes.length;i++){
    		keyNodes[i+1] = travelNodes[i];
    	}
    	keyNodes[keyNodes.length-1] = destination;
    	
    	int[][][] costTbls = new int[travelNodes.length+1][travelNodes.length][travelNodes.length];
    	int index1 = 0;
    	for(int i=0;i<travelNodes.length;i++){
    		int size = allPathSet[0][travelNodes[i]].length;
    		//去掉中间路径中包含源点，必经节点和终点的最短路径
    		boolean needPath = true;
    		for(int j=1;j<size-2;j++){
    			if(assist.isContained(keyNodes,allPathSet[0][travelNodes[i]][j])){
    				needPath = false;
    				break;
    			}
    		}
    		if(allPathSet[0][travelNodes[i]][size-1] != MAXCOST && needPath){
    			costTbls[0][index1++] = allPathSet[0][travelNodes[i]];
    		}
    	}
    	//是否排序，不进行排序就删掉下面这段代码
    	
    	int[][] temp1 = new int[index1][travelNodes.length];
    	for(int i=0;i<index1;i++){
    		temp1[i] = costTbls[0][i];
    	}
    	costTbls[0] = sortCostTbl_V3(temp1);
    	
    	for(int i=0;i<travelNodes.length;i++){
    		int index2 = 0;
    		for(int j=1;j<keyNodes.length;j++){//添加中间节点到终点的路径（keyNodes中最后一位）
    			if(keyNodes[j] != travelNodes[i]){
    				int size = allPathSet[i+1][keyNodes[j]].length;
    				//去掉中间路径中包含源点，必经节点和终点的最短路径
    				boolean needPath = true;
    				for(int k=1;k<size-2;k++){
    					if(assist.isContained(keyNodes,allPathSet[i+1][keyNodes[j]][k])){
    						needPath = false;
    						break;
    					}
    				}
    				if(allPathSet[i+1][keyNodes[j]][size-1] != MAXCOST && needPath){
    					costTbls[i+1][index2++] = allPathSet[i+1][keyNodes[j]];
    				}
    			}
    		}
    		//是否排序
    		
    		int[][] temp2 = new int[index2][travelNodes.length];
    		for(int k=0;k<index2;k++){
    			temp2[k] = costTbls[i+1][k];
    		}
    		costTbls[i+1] = sortCostTbl_V3(temp2);
    		
    	}
    	return costTbls;
    }
    
    /**
     * getCostTbls第五版，通过simplifiedTopo得到的allPathSet求出必经节点邻接表costTbls，
     * 与无关节点删除算法化简图的算法对应
     * 
     */
    
    public static int[][][] getCostTbls_V5(int[][][] allPathSet,int[] travelNodes,int source,
    		int destination){
    	
    	//构造包含源点终点和中间必经点的访问节点
    	int[] keyNodes = new int[travelNodes.length+2];
    	keyNodes[0] = source;
    	for(int i=0;i<travelNodes.length;i++){
    		keyNodes[i+1] = travelNodes[i];
    	}
    	keyNodes[keyNodes.length-1] = destination;
    	
    	int[][][] costTbls = new int[travelNodes.length+1][travelNodes.length][travelNodes.length];
    	
    	for(int i=0;i<keyNodes.length-1;i++){
    		int index = 0;
    		//如果当前节点为源点就没有必要求源点到终点的路径，用lastNodePos控制需不需要访问destination
    		int lastNodePos = 0;
    		if(i == 0){
    			lastNodePos = keyNodes.length-1;
    		}else{
    			lastNodePos = keyNodes.length;
    		}
    		for(int j=1;j<lastNodePos;j++){
    			if(keyNodes[j] != keyNodes[i]){
    				int size = allPathSet[keyNodes[i]][keyNodes[j]].length;
    				//去掉中间路径中包含源点和终点的最短路径
    				boolean needPath = true;
    				for(int k=1;k<size-2;k++){
    					if(allPathSet[keyNodes[i]][keyNodes[j]][k] == destination
    							|| allPathSet[keyNodes[i]][keyNodes[j]][k] == source){
    						needPath = false;
    						break;
    					}
    				}
    				if(allPathSet[keyNodes[i]][keyNodes[j]][size-1] != MAXCOST && needPath){
    					costTbls[i][index++] = allPathSet[keyNodes[i]][keyNodes[j]];
    				}
    			}
    		}
    		//是否排序
    		
    		int[][] temp = new int[index][travelNodes.length];
    		for(int k=0;k<index;k++){
    			temp[k] = costTbls[i][k];
    		}
    		if(travelNodes.length > 25 && travelNodes.length <= 30){
    			if(i == 0){
    				//如果中间必经节点数大于25，则将第一条路径按节点数从大到小排序
    				costTbls[i] = sortCostTbl_V2(temp);
    			}else{
    				costTbls[i] = sortCostTbl_V4(temp);
    			}
    		}else{
    			costTbls[i] = sortCostTbl_V3(temp);
    		}
    		
    		
    	}
    	return costTbls;
    }
    
    /**
     * sortCostTbl第二版，邻接表的排列按照路径节点数从大到小排序，节点数相同按照路径权重从小到大排序
     */
    
    public static int[][] sortCostTbl_V2(int[][] costTbl){
    	
    	double[] indicator = new double[costTbl.length];//记录每条路径的节点数
    	double[] backup = new double[costTbl.length];//对length的备份，记录length的原始次序
    	int[][] result = new int[costTbl.length][costTbl.length];
    	for(int i=0;i<costTbl.length;i++){
    		
    		//按照路径节点数从大到小排序，在相同的节点数情况下按照路径权重从大到小排序
    		indicator[i] = (10000 - (costTbl[i].length-1)*100) - costTbl[i][costTbl[i].length-1];
    		//按照路径节点数从大到小排序，在相同节点数的情况下按照路径权重从小到大排序
    		//indicator[i] = (10000 - (costTbl[i].length-1)*100) + costTbl[i][costTbl[i].length-1];
    		backup[i] = indicator[i];
    	}
    	//System.out.println(Arrays.toString(indicator));
    	Arrays.sort(indicator); //调用排序函数
    	for(int i=0;i<costTbl.length;i++){
    		for(int j=0;j<costTbl.length;j++){
    			if(backup[i] == indicator[j] && result[j][result[j].length-1] == 0){
    				result[j] = costTbl[i];
    				break;
    			}
    		}
    	}
    	return result;
    	
    }
    
    /**
     * sortCostTbl第三版，邻接表的排列按照路径节点数从小到大排序，节点数相同按照权重从小到大排序
     * 
     */
    
    public static int[][] sortCostTbl_V3(int[][] costTbl){
    	
    	double[] indicator = new double[costTbl.length];//记录每条路径的节点数
    	double[] backup = new double[costTbl.length];//对length的备份，记录length的原始次序
    	int[][] result = new int[costTbl.length][costTbl.length];
    	for(int i=0;i<costTbl.length;i++){
    		
    		//按照路径节点数从小到大排序，节点数相同按照权重从小到大排序
    		indicator[i] = 100*(costTbl[i].length-1)+costTbl[i][costTbl[i].length-1];
    		backup[i] = indicator[i];
    	}
    	Arrays.sort(indicator); //调用排序函数
    	for(int i=0;i<costTbl.length;i++){
    		for(int j=0;j<costTbl.length;j++){
    			if(backup[i] == indicator[j] && result[j][result[j].length-1] == 0){
    				result[j] = costTbl[i];
    				break;
    			}
    		}
    	}
    	return result;
    	
    }
    
    /**
     * sortCostTbl第4版，邻接表的排列只按照权重从小到大排序
     * 
     */
public static int[][] sortCostTbl_V4(int[][] costTbl){
    	
    	double[] indicator = new double[costTbl.length];//记录每条路径的节点数
    	double[] backup = new double[costTbl.length];//对length的备份，记录length的原始次序
    	int[][] result = new int[costTbl.length][costTbl.length];
    	for(int i=0;i<costTbl.length;i++){
    		
    		//按照权重从小到大排序
    		indicator[i] = costTbl[i][costTbl[i].length-1];
    		backup[i] = indicator[i];
    	}
    	Arrays.sort(indicator); //调用排序函数
    	for(int i=0;i<costTbl.length;i++){
    		for(int j=0;j<costTbl.length;j++){
    			if(backup[i] == indicator[j] && result[j][result[j].length-1] == 0){
    				result[j] = costTbl[i];
    				break;
    			}
    		}
    	}
    	return result;
    	
    }
    
    /**
     * 输入数据解析成邻接矩阵和设计的必经节点格式
     * @param graphContent 关于图的相关信息
     * @param condition 起始点，目标点和中间必经节点信息
     * @return List，其中包含解析后的各类信息，类型为int或者int[]
     * 
     */
    
    public static List<Object> resolverInput(String graphContent, String condition){
    	
    	List<Object> result= new ArrayList<Object>();
    	
    	String splitGraphContent[] = graphContent.split("\\D");
    	String splitCondition[] = condition.split("\\D");
    	int adjMatrix[][] = null;
    	int indexEdge[][] = null;
    	int travelNodes[] = null;
    	int sizeNodes = 0;
    	int source = 0;
    	int destination = 0;
    	int edgeNum = splitGraphContent.length/4;

    	for(int i=0;i<splitGraphContent.length/4;i++){

    		int firNode = assist.String2Int(splitGraphContent[4*i+1]);
    		int secNode = assist.String2Int(splitGraphContent[4*i+2]);
    		int currentSize = firNode>secNode?firNode:secNode;
    		sizeNodes = sizeNodes>currentSize?sizeNodes:currentSize;
    	}
    	adjMatrix = new int[sizeNodes+1][sizeNodes+1];
    	indexEdge = new int[sizeNodes+1][sizeNodes+1];
    	
    	for(int i=0;i<=sizeNodes;i++){
    		for(int j=0;j<=sizeNodes;j++){
    			adjMatrix[i][j] = MAXCOST;
    			indexEdge[i][j] = 0;
    		}
    	}

    	for(int i=0;i<splitGraphContent.length/4;i++){
    		
    		int index= assist.String2Int(splitGraphContent[4*i+0]);
    		int row= assist.String2Int(splitGraphContent[4*i+1]);
    		int column= assist.String2Int(splitGraphContent[4*i+2]);
    		int cost= assist.String2Int(splitGraphContent[4*i+3]);
    		
    		if(adjMatrix[row][column] == MAXCOST 
    				|| adjMatrix[row][column]>cost){
    			adjMatrix[row][column] = cost;
    			indexEdge[row][column] = index;
    		}
    		
    	}
    	travelNodes = new int[splitCondition.length-2];
    	for(int i=0;i<splitCondition.length;i++){

    		int node = assist.String2Int(splitCondition[i]);
    		if(i == 0){
    			source = node;
    		}else if( i == 1){
    			destination = node;
    		}else{
    			travelNodes[i-2] = node;
    			
    		}
    		
    	}
    	
    	result.add(adjMatrix);
    	result.add(indexEdge);
    	result.add(travelNodes);
    	result.add(source);
    	result.add(destination);
    	result.add(edgeNum);
    	
    	return result;
    	
    }

    /**
     * dijkstra第二版，输出为二维数组的形式，期望提高效率，经测试这一版本时间效率最优
     * @return 返回结果为二维数组，每行数据表示输入source到图中所有节点的路径和权重，
     * 		   到source本节点的权重记为0；没有通路的点权重记为15000（MAXCOST），
     * 		   每行数据按照终止节点序号从小到大排列
     */
    public static int[][] dijkstra_V2(int[][] adjMatrix, int source){
    	
    	int maxNode = adjMatrix.length;
    	int minCost[] = new int[maxNode];
    	int isVisited[] = new int[maxNode];
    	
    	int[][] shortestPath = new int[maxNode][maxNode];
    	for(int i=0;i<maxNode;i++){
    		int[] temp = new int[2];
    		temp[0] = source;
    		temp[1] = i;
    		shortestPath[i] = temp;//通过这种方式初始化，实现每行数组规模的动态改变，此时每行数组长度为2
    	}
    	
    	minCost[source] = 0;
    	isVisited[source] = 1;
    	
    	for(int i=1; i<maxNode;i++){
    		int label = -1;
    		int minCostTmp = Integer.MAX_VALUE;
    		for(int j=0;j<maxNode;j++){
    			if(isVisited[j] == 0 && adjMatrix[source][j]<minCostTmp){
    				minCostTmp = adjMatrix[source][j];
    				label = j;
    			}
    		}
 
    		minCost[label] = minCostTmp;
    		isVisited[label] = 1;
    		
    		for(int k=0;k<maxNode;k++){
    			if(isVisited[k] == 0 
    					&& (adjMatrix[source][label]+adjMatrix[label][k])<adjMatrix[source][k]){
    				adjMatrix[source][k] = adjMatrix[source][label]+adjMatrix[label][k];
    				//路径信息更改
    				int[] temp = shortestPath[label];
    				int[] newArray = new int[temp.length+1];
    				for(int index = 0;index<temp.length;index++){
    					newArray[index] = temp[index];
    				}
    				newArray[newArray.length-1] = k;
    				shortestPath[k] = newArray;
    			}
    		}

    	}
    	//每行数据末尾添加权重信息
    	for(int i=0;i<maxNode;i++){
    		int[] temp = new int[shortestPath[i].length+1];
    		for(int j=0;j<shortestPath[i].length;j++){
    			temp[j] = shortestPath[i][j];
    		}
    		temp[temp.length-1] = minCost[i];
    		shortestPath[i] = temp;
    	}
    	return shortestPath;	
    }
    
    /**
     * 第二版needNodesDij,计算以源点和中间必经节点为起始点的最短路径，此版本与dijkstra_V2对应
     * @return allPathSet[][][] 三维数组中每页的数据即为同一个源点到所有其他节点的路径，
     * 		   第一页表示源点到其他节点的路径，后面的页面组织次序与travelNodes数组中必经节点的
     *         存放次序相同
     */
    
    public static int[][][] needNodesDij_V2(int[][] adjMatrix, int[] travelNodes, 
    		int source, int destination){
    	
    	int[][][] allPathSet = new int[travelNodes.length+1][adjMatrix.length][adjMatrix.length];
    	
    	int[][] firAdjMatrix = new int[adjMatrix.length][adjMatrix.length];//源点到中间必经节点的矩阵信息
    	int[][] secAdjMatrix = new int[adjMatrix.length][adjMatrix.length];//中间必经节点对和到终止节点的矩阵信息
    	for(int j=0;j<adjMatrix.length;j++){
    		for(int k=0;k<adjMatrix.length;k++){
    			firAdjMatrix[j][k] = adjMatrix[j][k];
    			secAdjMatrix[j][k] = adjMatrix[j][k];
    		}
    	}
    	/*
    	 * 求源点到中间必经节点的最短路径时去掉终点，
    	 * 求中间必经节点对和到终止节点的最短路径时去掉源点，初步防止环路
    	 * 去点操作用过给出入路径权重赋值为MAXCOST完成
    	 */
    	for(int j=0;j<adjMatrix.length;j++){
    		firAdjMatrix[destination][j] = MAXCOST;//设置源点到中间必经节点的邻接矩阵信息，设置发出边权重
    		secAdjMatrix[source][j] = MAXCOST;//设置中间必经节点对和到终止节点的邻接矩阵信息
    	}
    	for(int j=0;j<adjMatrix.length;j++){
    		firAdjMatrix[j][destination] = MAXCOST; //设置接受边权重
    		secAdjMatrix[j][source] = MAXCOST;
    	}
    	//求源点到中间必经节点之间的最短路径
    	int[][] firPathAndCost = dijkstra_V2(firAdjMatrix,source); 
    	allPathSet[0] = firPathAndCost;
    	//求中间节点对之间的最短路径
    	for(int i=0;i<travelNodes.length;i++){	
    		int[][] perPathAndCost = dijkstra_V2(secAdjMatrix, travelNodes[i]);
    		allPathSet[i+1] = perPathAndCost;
    	}
    	return allPathSet;
    	
    }
    
    /**
     * 通过节点数组找到对应的连接边编号
     * @param indexEdge 节点连接边的索引矩阵
     * @param nodeSet 需要对应到边索引的节点集
     * @return 边索引构成的字符串，按要求两个索引值之间加"|"
     */
    
    public static String node2Edge(int [][] indexEdge, int [] nodeSet){
    	int sizeNodeSet = nodeSet.length;
    	int index = 0;
    	StringBuffer sb = new StringBuffer();
    	for(int i=0;i<sizeNodeSet-1;i++){
    		int firNode = nodeSet[i];
    		int secNode = nodeSet[i+1];
    		index = indexEdge[firNode][secNode];
    		sb.append(index);
    		if(i<sizeNodeSet-2){
    			sb.append("|");
    		}
    	}
    	return sb.toString();
    }
    
    
}