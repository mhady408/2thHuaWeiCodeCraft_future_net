package com.routesearch.route;

public class Assist {
	
	/**
	 *将字符串数字转化为整形
	 */
	
	public int String2Int(String s){
		int res = Integer.parseInt(String.valueOf(s.trim()));
		return res;
	}
	
	/**
	 * 将char形数字转化为整形
	 */
	
	public int Char2Int(char c){
		int res = String2Int(String.valueOf(c));
		return res;
	}
	
	/**
	 * 判断一个数组中是否包含指定值
	 */
	
	public boolean isContained(int input[], int target){
		for(int i=0;i<input.length;i++){
			if(input[i] == target){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 判断两个数组中是否含有相同的值
	 */
	
	public boolean containsSameOne(int [] list1, int begin1, int end1, 
			int []list2, int begin2, int end2){
		for(int i=begin1;i<=end1;i++){
			for(int j=begin2;j<=end2;j++){
				if(list1[i] == list2[j]){
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * 统计两个数组中相同元素个数
	 */
	
	public int countSameNodeNum(int[] list1, int begin1, int end1, 
			int[] list2, int begin2, int end2){
		int num = 0;
		for(int i=begin1;i<=end1;i++){
			for(int j=begin2;j<=end2;j++){
				if(list1[i] == list2[j]){
					num++;
				}
			}
		}
		return num;
	}
	
	/**
	 * 交换数组中两个指定位置的值
	 */
	
	public void swap(int [] a,int i, int j){
		int temp = a[i];
		a[i] = a[j];
		a[j] = temp;
	}
	
	/**
	 * 判断两个数组是否相同,包含源点和终点
	 */
	
	public boolean isSameArray(int[] list1, int start1, int last1, 
			int[] list2, int start2, int last2){
		
		int size1 = last1-start1;
		int size2 = last2-start2;
	
		if(size1 != size2){
			 return false;
		}
		for(int i=0;i<=size1;i++){
			if(list1[start1+i] != list2[start2+i]){
				return false;
			}
		}
		return true;
	}
	
}
