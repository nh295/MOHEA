/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import hh.IO.IOCreditHistory;
import hh.IO.IOQualityHistory;
import hh.IO.IOSelectionHistory;
import hh.hyperheuristics.IHyperHeuristic;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.analysis.sensitivity.EpsilonHelper;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.TypedProperties;

/**
 *
 * @author nozomihitomi
 */
public class HHCreditTest {

    /**
     * pool of resources
     */
    private static ExecutorService pool;

    /**
     * List of future tasks to perform
     */
    private static ArrayList<Future<IHyperHeuristic>> futures;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        String[] problems = new String[]{"UF9", "UF10"};//,"UF11","UF12","UF13"};
//        String[] problems = new String[]{"UF1","UF2","UF3","UF4","UF5","UF6","UF7"};
        String[] problems = new String[]{"UF1","UF2","UF3","UF4","UF5","UF6","UF7","UF8","UF9","UF10"};

        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
//        pool = Executors.newFixedThreadPool(1);
        for (String problem : problems) {
            String path;
            if (args.length == 0) //                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            {
                path = "C:\\Users\\SEAK2\\Nozomi\\MOHEA";
//                path = "C:\\Users\\SEAK1\\Dropbox\\MOHEA";
//                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            } else {
                path = args[0];
                problem = args[1];
            }
            String probName = problem;
            System.out.println(probName);
            int numberOfSeeds = 30;
            int maxEvaluations = 300010;
            //Setup heuristic selectors
            String[] selectors = new String[]{"AP", "PM"};
//            String[] selectors = new String[]{"Random"};
//            setup credit definitions
//            String[] creditDefs = new String[]{"ODP","OPIAE","OPIR2",
//                "OPopPF", "OPopEA", "OPopIPFAE","OPopIPFR2","OPopIEAAE","OPopIEAR2",
//                "CPF", "CEA"};
            String[] creditDefs = new String[]{"OPDe","SIDe","CSDe",};
//            String[] creditDefs = new String[]{"OPIR2","OPopIPFR2","OPopIEAR2","CR2PF","CR2EA"};
//            String[] creditDefs = new String[]{"ODP","OPopPF", "OPopEA","CPF", "CEA","OPIR2","OPopIEAR2","CR2PF","CR2EA"};
//            String[] creditDefs = new String[]{"OPDe"};
           
            //for single operator MOEA
//            String[] ops = new String[]{"um","sbx+pm","de+pm","pcx+pm","undx+pm","spx+pm"};

            futures = new ArrayList<>();
            //loop through the set of algorithms to experiment with
            for (String selector : selectors) {
                for (String credDefStr : creditDefs) {
                    //parallel process all runs
                    futures.clear();
                    for (int k = 0; k < numberOfSeeds; k++) {

//                    for (String op : ops) {
                        Problem prob = ProblemFactory.getInstance().getProblem(probName);
                        double[] epsilonDouble = new double[prob.getNumberOfObjectives()];
                        for (int i = 0; i < prob.getNumberOfObjectives(); i++) {
                            epsilonDouble[i] = EpsilonHelper.getEpsilon(prob);
                        }

                        //Setup algorithm parameters
                        Properties prop = new Properties();
                        String popSize;
                        switch(prob.getNumberOfObjectives()){
                            case 2: popSize = "600";
                                    break;
                            case 3: popSize = "1000";
                                    break;
                            case 5: popSize = "1200";
                                break;
                            default: throw new UnsupportedOperationException("Unsupported test problem: Problems with 2,3, and 5 objectives are supported");
                        }
                        prop.put("populationSize", popSize);
                        prop.put("HH", selector);
                        prop.put("CredDef", credDefStr);

                        //Choose heuristics to be applied. Use default values (probabilities)
                        ArrayList<Variation> heuristics = new ArrayList<>();
                        OperatorFactory of = OperatorFactory.getInstance();
                        Properties heuristicProp = new Properties();
                        heuristicProp.put("um.rate",1.0 / prob.getNumberOfVariables());
                        
                        heuristicProp.put("sbx.rate", 1.0);
			heuristicProp.put("sbx.distributionIndex", 15.0);
                                                
                        heuristicProp.put("de.crossoverRate", 1.0);
			heuristicProp.put("de.stepSize", 0.5);
                        
                        heuristicProp.put("pcx.parents", 3);
			heuristicProp.put("pcx.eta", 0.1);
			heuristicProp.put("pcx.zeta", 0.1);
                        
                        heuristicProp.put("undx.zeta", 0.5);
			heuristicProp.put("undx.eta", 0.35);
                        heuristicProp.put("undx.parents", 3);
                        
                        heuristicProp.put("spx.epsilon", 3);
                        heuristicProp.put("spx.parents", prob.getNumberOfVariables()+1);
                        
                        heuristicProp.put("pm.rate", 1.0 / prob.getNumberOfVariables()); 
			heuristicProp.put("pm.distributionIndex", 20.0);
                        
                        heuristics.add(of.getVariation("um", heuristicProp, prob));
                        heuristics.add(of.getVariation("sbx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("de+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("pcx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("undx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("spx+pm", heuristicProp, prob));
                        
//                        heuristics.add(of.getVariation(op, heuristicProp, prob));

                        TypedProperties typeProp = new TypedProperties(prop);
                        typeProp.setDoubleArray("ArchiveEpsilon", epsilonDouble);
                        TestRun test = new TestRun(path, prob, probName,
                                typeProp, heuristics, maxEvaluations);
                        futures.add(pool.submit(test));

                        //benchmark built-in MOEA
//                            System.out.println(op);
//                            prop.put("operator", op);
//                            typeProp = new TypedProperties(prop);
//                            TestRunBenchmark test = new TestRunBenchmark(path, prob, probName,
//                                    typeProp, "eMOEA", maxEvaluations);

//                            futures.add(pool.submit(test));
//                        }
                    }
                    for (Future<IHyperHeuristic> run : futures) {
                        try {
                            IHyperHeuristic hh = run.get();
//                            
                            String name = path + File.separator + "results" + File.separator + probName + "_"
                                    + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName();

//                            //save the approximation set
//                            NondominatedPopulation ndPop = hh.getResult();
//                            try {
//                                PopulationIO.writeObjectives(new File(name + ".NDpop"), ndPop);
//                            } catch (IOException ex) {
//                                Logger.getLogger(TestRunBenchmark.class.getName()).log(Level.SEVERE, null, ex);
//                            }
//                            save selection history
//                            IOSelectionHistory.saveHistory(((IHyperHeuristic) hh).getSelectionHistory(),
//                                    name + ".hist",",");
                            //save credit history
//                          IOCreditHistory.saveHistory(((IHyperHeuristic) hh).getCreditHistory(),
//                          path + File.separator + "results" + File.separator + probName + "_"
//                          + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName() + ".credit");
//                          IOCreditHistory ioch = new IOCreditHistory();
//                          ioch.saveHistory(((IHyperHeuristic) hh).getCreditHistory(), name + ".creditcsv",",");
//                            save quality history
//                            IOQualityHistory.saveHistory(((IHyperHeuristic) hh).getQualityHistory(),
//                                    name + ".qual");
//                            hh.reset();
//                            hh = null;
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(HHCreditTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    System.out.println("Finished " + probName + "_" + selector + "_" + credDefStr + "\n\n");
                }
            }
        }
        pool.shutdown();
    }
}
