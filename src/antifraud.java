import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.HashSet;

class antifraud{
	public static Graph g;
    public static void main(String[] args){
        String batchFileLocation=args[0];
        String streamFileLocation=args[1];
        String output1FileLocation=args[2];
        String output2FileLocation=args[3];
        String output3FileLocation=args[4];

        g=new Graph();
        try {
			// Step 1: Call to build the graph.
			buildGraph(batchFileLocation);
			
            feature1(streamFileLocation,output1FileLocation);
            System.out.println("OutputText1 Created");
            feature2(streamFileLocation,output2FileLocation);
            System.out.println("OutputText2 Created");
            feature3(streamFileLocation,output3FileLocation);
            System.out.println("OutputText3 Created");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Function for Building the graph. This would run just once to  up with the historic data
    public static void buildGraph(String batchFileLocation) throws Exception{
       BufferedReader br = new BufferedReader(new FileReader(batchFileLocation));
        br.readLine(); // For ignoring the first Line        
        String line="";
        while ((line = br.readLine()) != null) {
            int[] payment = preProcess(line);
            if(payment.length==2){
                g.addEdge(payment[0],payment[1]);
                g.addEdge(payment[1],payment[0]);
            }
        }
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

    public static void feature1(String infilename,String outfilename){
        //BufferedReader br = null;
        String line="";
        BufferedReader br;
        try{
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
            br=new BufferedReader(new FileReader(infilename));
            br.readLine();
            while ((line = br.readLine()) != null) {
                    int[] payment = antifraud.preProcess(line);
                    if(payment.length==2){ 
                    if(checkSingle(payment[0],payment[1]))
                        out.println("trusted");
                    else
                        out.println("unverified");
                    }else {   
                     out.println("NO INFO");
                    }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }

    public static void feature2(String infilename,String outfilename){
        //BufferedReader br = null;
        String line="";
        BufferedReader br;
        try{
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
            br=new BufferedReader(new FileReader(infilename));
            br.readLine();
            while ((line = br.readLine()) != null) {
                int[] payment = antifraud.preProcess(line);
                if(payment.length==2){
                    if(checkSingle(payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else if(checkDouble(payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else out.println("unverified");
                } 
                else{
                    out.println("NO INFO");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } 
    }


    public static void feature3(String infilename,String outfilename){
        //BufferedReader br = null;
        String line="";
        BufferedReader br;
        try{
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outfilename)));
            br=new BufferedReader(new FileReader(infilename));
            br.readLine();
            while ((line = br.readLine()) != null) {
                int[] payment = antifraud.preProcess(line);
                if(payment.length==2){
                    if(checkSingle(payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else if(checkDouble(payment[0],payment[1])){
                        out.println("trusted");
                    }
                    else if(checkFour(payment[0],payment[1])) {
                        out.println("trusted");
                    }

                    out.println("unverified");      
                }
                else {
                   out.println("NO INFO");
                }
            }

        } catch (Exception e) {
            //exception handling left as an exercise for the reader
            e.printStackTrace();
        } 
    }

    public static boolean checkSingle(int p1,int p2){
        if(g.getEdgeList(p1).contains(p2))
            return true;
        return false;

    }

    public static boolean checkDouble(int p1,int p2){
        HashSet<Integer> neighbours0=g.getEdgeList(p1);
        HashSet<Integer> neighbours1=g.getEdgeList(p2);
        HashSet<Integer> intersection = new HashSet(neighbours0); 
        intersection.retainAll(neighbours1);
        if(intersection.size()>0)
            return true;
        return false;

    }

    public static boolean checkFour(int p1,int p2){
        HashSet<Integer> neighbours0=g.getEdgeList(p1);
        HashSet<Integer> neighbours1=g.getEdgeList(p2);
        HashSet<Integer> intersection = new HashSet(neighbours0); 

        for(int num:neighbours0){
            intersection.addAll(g.getEdgeList(num));
        }
                                
        for(int num:neighbours1){
            HashSet<Integer> intersection1=new HashSet(intersection);
            intersection1.retainAll(g.getEdgeList(num));
            if(intersection1.size()>0)
                return true;
        }
        return false;
    }



}