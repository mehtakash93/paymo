import java.util.HashMap;
import java.util.HashSet;

public class Graph{
	private final HashMap<Integer,HashSet<Integer>> graph;
	public Graph(){
		graph=new HashMap();
	}

	public void addEdge(int p1,int p2){
		if (graph.containsKey(p1)){
			graph.get(p1).add(p2);
		} else {
	        graph.put(p1, new HashSet<Integer>());
			graph.get(p1).add(p2);
		}
	}

	public HashSet<Integer> getEdgeList(int node){
		if(graph.containsKey(node))
			return graph.get(node);
		return new HashSet<Integer>();
	}
	
	public int getSize(){
		return graph.size();
	}

}