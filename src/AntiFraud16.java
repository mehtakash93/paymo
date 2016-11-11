import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.lang.StringBuffer;

class AntiFraud16{
    public static void main(String[] args){
        String batchFileLocation=args[0];
        String streamFileLocation=args[1];
        String output1FileLocation=args[2];
        String output2FileLocation=args[3];
        String output3FileLocation=args[4];

        try {
    			// Step 1: Call to build the inital and simple graph.
    			Graph g=buildGraph(batchFileLocation);
    			System.out.println("Initial Graph Created");
    			/* Step2 : For Feature 1. Simple lookup into any one(source or destination) adjecency lists. O(1) operation for each datapoint in the stream file.
    			So it is not affected by the size of batch processed graph.
    			*/
                long startTime = System.currentTimeMillis();
                featureUniDirectional(g,streamFileLocation,output1FileLocation);
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                System.out.println("OutputText1 Created"+elapsedTime);


               /* For Feature3- Creating a graph with adjacency list till depth 2. Then using that depth 2 graph to get simple intersection of the adjacency lists.For each stream data point this would be a simple O(n) process
                Better way would have been to create a graph of adjacency list haveing depth 4 and then doing a simple lookup in O(1) for each stream entry. But is taking program memory more than I have. 
                If there is a 32gb ram or something like that we can make this into simpe unidirectional search just by doing
                Graph depthFour=buildNDepthGraph(g,4);
               	featureUniDirectional(depthFour,streamFileLocation,output3FileLocation);
    			*/
                startTime = System.currentTimeMillis();
                Graph depthTwo=buildNDepthGraph(g,2);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.println("Graph depth 2 created"+elapsedTime);

                startTime = System.currentTimeMillis();
               	featureBidirectional(depthTwo,streamFileLocation,output3FileLocation);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.println("OutputText3Created"+elapsedTime);
               	/* Best way would be to use the graph made till level2 and do a simple lookup. As i am able to create adjecency lists till depth2 in memory i would use the unidirectionl lookup here O(1).
               	I cant use unidirectional for feature3 because the graph for depth4 graph is much bigger than the memory i have. 
               	I want to try to keep it O(1) as much as possible as then it wont depend on size of initial batch graph at the time of processing stream.
               	*/
               	startTime = System.currentTimeMillis();
               	featureUniDirectional(depthTwo,streamFileLocation,output2FileLocation);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.println("OutputText2Created"+elapsedTime);
               	depthTwo=null; //For garbage collecion


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Function for Building the graph. This would run just once/rarely on batch data
    public static Graph buildGraph(String batchFileLocation) throws IOException{
       BufferedReader br = new BufferedReader(new FileReader(batchFileLocation));
       Graph graph=new Graph();
        br.readLine(); // For ignoring the first Line        
        String line="";
        while ((line = br.readLine()) != null) {
            int[] payment = preProcess(line);
            if(payment.length==2){
                graph.addEdge(payment[0],payment[1]);
                graph.addEdge(payment[1],payment[0]);
            }
        }
        return graph;
    }
    
	// Function for basic preprocessing
    public static int[] preProcess(String inputLine){
        int[] edge=new int[2];
        String[] row=inputLine.split(",");
        if(row.length>=3){
            try {
                    edge[0] = Integer.parseInt(row[1].trim());
                    edge[1] = Integer.parseInt(row[2].trim());
                } catch (NumberFormatException e) {
                    return edge;
                }
        }
        return edge;
    }


    public static boolean checkSingle(Graph graph,int p1,int p2){
        if(graph.getEdgeList(p1).contains(p2))
            return true;
        return false;

    }

    public static boolean checkDouble(Graph graph,int p1,int p2){
        HashSet<Integer> neighbours0=graph.getEdgeList(p1);
        HashSet<Integer> neighbours1=graph.getEdgeList(p2);
        HashSet<Integer> intersection = new HashSet(neighbours0); 
        intersection.retainAll(neighbours1);
        if(intersection.size()>0)
            return true;
        return false;

    }

    public static void featureUniDirectional(Graph graph,String infilename,String outfilename){
        BufferedReader br;
        StringBuffer sb;
        try{
            sb=new StringBuffer();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
            br=new BufferedReader(new FileReader(infilename));
            br.readLine();
            while (true) {
                // creating string till reaching the \n character
                sb.setLength(0);
                int ch;
                while((ch = br.read()) != -1 && ch != '\n'){
                    sb.append((char)ch);
                }
                if(ch==-1)
                    break;

                //preprocssing the single payment string 
                int[] payment = preProcess(sb.toString());
                if(payment.length==2){
                     if(checkSingle(graph,payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else 
                        out.println("unverified");
                } 
                else{
                    out.println("NO INFO");
                }
            }
            out.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }    


    public static void featureBidirectional(Graph graph,String infilename,String outfilename){
        BufferedReader br;
        StringBuffer sb;
        try{
            sb=new StringBuffer();
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
            br=new BufferedReader(new FileReader(infilename));
            br.readLine();
            while (true) {
                // creating string till reaching the \n character
                sb.setLength(0);
                int ch;
                while((ch = br.read()) != -1 && ch != '\n'){
                    sb.append((char)ch);
                }
                if(ch==-1)
                    break;

                //preprocssing the single payment string 
                int[] payment = preProcess(sb.toString());
                if(payment.length==2){
                     if(checkSingle(graph,payment[0],payment[1]) || checkDouble(graph,payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else 
                        out.println("unverified");
                } 
                else{
                    out.println("NO INFO");
                }
            }
            out.close();
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } 
    }



    public static Graph buildNDepthGraph(Graph graph,int n) throws Exception{
        Graph level2Graph = new Graph();
        for(int num:graph.getKeySet()){
            level2Graph.addEdgeList(num,bfs(graph,n,num));
        }
        return level2Graph;
    }



    public static HashSet<Integer> bfs(Graph graph,int level, int p1){
        Queue<Integer> q = new LinkedList();
        HashSet<Integer> finalSet=new HashSet();
        finalSet.add(p1);
        q.add(p1);
        while(level>0 && !q.isEmpty()){  
            level--;
            int size=q.size();
                //List<Integer> temp=new LinkedList<Integer>();

            for(int i=0;i<size;i++){
                int currentElem=q.remove();
                for(int num:graph.getEdgeList(currentElem)){
                    if(!finalSet.contains(num)){
                        finalSet.add(num);
                        q.add(num);
                    }
                }

            }
        }
        return finalSet;
   }

}