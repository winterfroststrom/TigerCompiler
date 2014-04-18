package CodeGeneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphColor {
	public static <T> Map<T, String> graphColor(ArrayList<String> colors, Map<T, Set<T>> graph){
		Map<T, String> coloring = new HashMap<T, String>();
		Set<T> spilled = new HashSet<>();
		List<T> stack = new LinkedList<T>();
		Set<T> stackContains = new HashSet<>();
		while(coloring.size() + spilled.size() < graph.size()){ // determine if all nodes are colored or spilled
			coloring = new HashMap<T, String>();
			while(stack.size() + spilled.size() < graph.size()){ // find nodes to be colored
				T minCost = null;
				int maxDegree = -1;
				for(T key : graph.keySet()){ 
					if(!spilled.contains(key) && !stackContains.contains(key)){
						if(graph.get(key).size() < colors.size()){ // add all degree < k
							stackContains.add(key);
							stack.add(key);
						} else { // allow add in order of decreasing degree 
							int currentDegree = 0;
							for(T neighbor : graph.get(key)){
								if(!spilled.contains(neighbor) & !stackContains.contains(neighbor)){
									currentDegree++;
								}
							}
							if(currentDegree > maxDegree){
								minCost = key;
								maxDegree = currentDegree;
							}
						}
					}
				}
				if(minCost != null){
					stack.add(minCost);
					stackContains.add(minCost);
				}
			}
			while(!stack.isEmpty()){ // try to color all nodes
				T current = stack.remove(stack.size() - 1);
				stackContains.remove(current);
				Set<String> possibleColors = new HashSet<>();
				for(String color : colors){
					possibleColors.add(color);
				}
				for(T neighbor : graph.get(current)){ // determine color conflicts
					if(!stackContains.contains(neighbor)){
						possibleColors.remove(coloring.get(neighbor));
					}
				}
				if(possibleColors.size() < 1){
					spilled.add(current);
					break;
				} else {
					for(int i = 0; i < colors.size(); i++){ // find minimal order color
						if(possibleColors.contains(colors.get(i))){
							coloring.put(current, colors.get(i));
							break;
						}
					}
				}
			}
		}
		return coloring;
	}
	
	public static void main(String[] args){
		ArrayList<String> colors = new ArrayList<>();
		colors.add("$t1");
		colors.add("$t2");
		Map<Integer, Set<Integer>> graph = new HashMap<>();
		Set<Integer> list;
		list = new HashSet<>();
		list.add(1);
		list.add(3);
		list.add(5);
		list.add(7);
		graph.put(0, list);
		graph.put(2, list);
		graph.put(4, list);
		graph.put(6, list);
		list = new HashSet<>();
		list.add(0);
		list.add(2);
		list.add(4);
		list.add(6);
		graph.put(1, list);
		graph.put(3, list);
		graph.put(5, list);
		graph.put(7, list);
		System.out.println(graphColor(colors, graph));
	}
}
