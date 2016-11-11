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
class AntiFraud8{
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

                /* Best way would be to use the graph made till level2 and do a simple lookup. For 8gb ram, using just the O(n) operation during stream would be fine 
                As storing the level2 graph in memory with 8gb ram is very slow and would cause lots of collisions and become slower eventually
                But on 16gb level2 graph will working very well.
                */
                // For Level2
                startTime = System.currentTimeMillis();
                featureBidirectional(g,streamFileLocation,output2FileLocation);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.println("OutputText2Created"+elapsedTime);

                /* For Feature3- Doing bidirectional search at the time of stream as i cant precompute and store things in my 8gb ram laptop without collisions
                Will Work slow, but works much faster on a 16gb ram laptop with precomputation till depth2.
                */

                startTime = System.currentTimeMillis();
                feature3(g,streamFileLocation,output3FileLocation);
                endTime = System.currentTimeMillis();
                elapsedTime = endTime - startTime;
                System.out.println("OutputText3Created"+elapsedTime);

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
        br.close();
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

    // Simple function to check single degree relationship in the graph

    public static boolean checkSingle(Graph graph,int p1,int p2){
        return graph.getEdgeList(p1).contains(p2);
    }

    /* Simple function to check second degree relationship in the graph.
    That is birectional intersection */

    public static boolean checkDouble(Graph graph,int p1,int p2){
        HashSet<Integer> neighbours0 = graph.getEdgeList(p1);
        HashSet<Integer> neighbours1 = graph.getEdgeList(p2);
        HashSet<Integer> intersection = new HashSet(neighbours0); 
        intersection.retainAll(neighbours1);
        return (intersection.size() > 0);

    }

    /* Simple function to check fourth degree relationship in the graph.
    That is birectional search till depth2 on both the sides and then intersection */

    public static boolean checkFour(Graph graph,int p1,int p2){
        HashSet<Integer> neighbours0=graph.getEdgeList(p1);
        HashSet<Integer> neighbours1=graph.getEdgeList(p2);
        HashSet<Integer> intersection = new HashSet(neighbours0); 

        for(int num:neighbours0){
            intersection.addAll(graph.getEdgeList(num));
        }
                                
        for(int num:neighbours1){
            HashSet<Integer> intersection1=new HashSet(intersection);
            intersection1.retainAll(graph.getEdgeList(num));
            if(intersection1.size()>0)
                return true;
        }
        return false;
    }

    /* For the case with just 8 gb ram, this function will only be used for feature1. If we have more ram, this function
    is the only thing needed*/
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

    /* For the case with just 8 gb ram, this function will only be used for feature2. If we have more ram, this function
    can also be used for feature3 or nothing atall too*/
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


    /* For the case with just 8 gb ram, this function will only be used for feature3. But this computes depth+intersection during stream
    processing whihc can be really slow. So avoid Using this generally*/
    public static void feature3(Graph graph,String infilename,String outfilename){
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
                int[] payment = antifraud.preProcess(sb.toString());
                if(payment.length==2){
                    if(checkSingle(graph,payment[0],payment[1]) || checkDouble(graph,payment[0],payment[1]) || checkFour(graph,payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else out.println("unverified");      
                }
                else {
                   out.println("NO INFO");
                }
            }
            out.close();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }
    
}