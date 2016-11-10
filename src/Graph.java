import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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

	public void addEdgeList(int node,HashSet<Integer> edgeList){
		this.graph.put(node,edgeList);
	}

	public HashSet<Integer> getEdgeList(int node){
		if(graph.containsKey(node))
			return graph.get(node);
		return new HashSet<Integer>();
	}

	public Set<Integer> getKeySet(){
		return graph.keySet();
	}
	
	public int getSize(){
		return graph.size();
	}

}