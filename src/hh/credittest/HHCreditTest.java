/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import hh.credithistory.RewardHistoryWindow;
import hh.creditrepository.ICreditRepository;
import hh.creditrepository.SlidingWindowRepository;
import hh.hyperheuristics.IHyperHeuristic;
import hh.qualityestimation.IQualityEstimation;
import hh.qualityestimation.MeanRewards;
import hh.qualityestimation.RankRewards;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.analysis.sensitivity.EpsilonHelper;
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
//        String[] problems = new String[]{"UF1", "UF2", "UF3" ,"UF4", "UF5", "UF6", "UF7", "UF8", "UF9", "UF10"};
        String[] problems = new String[]{"UF1"};
//        String[] problems = new String[]{" "};

        pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()-1);
//        pool = Executors.newFixedThreadPool(1);
        for (String problem : problems) {
            String path;
            if (args.length == 0) //                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            {
//                path = "C:\\Users\\SEAK2\\Nozomi\\MOHEA";
                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            } else {
                path = args[0];
            }
            String probName = problem;
            System.out.println(probName);
            int numberOfSeeds = 30;
            int maxEvaluations = 1000;
            int windowSize = 300;
            //Setup heuristic selectors
//            String[] selectors = new String[]{"PM", "AP"};
            String[] selectors = new String[]{"PM"};
            //setup credit definitions
//            String[] creditDefs = new String[]{"ODP",//,"OPIAE","OPIR2","OPIR3",
//                "OPopPF", "OPopEA", "OPopIPFAE","OPopIPFR2","OPopIPFR3","OPopIEAAE","OPopIEAR2","OPopIEAR3",
//                "CPF", "CEA"};
            String[] creditDefs = new String[]{"OPIAE"};

            futures = new ArrayList<>();
            //loop through the set of algorithms to experiment with
            for (String selector : selectors) {
                for (String credDefStr : creditDefs) {
                    //parallel process all runs
                    futures.clear();
                    for (int k = 0; k < numberOfSeeds; k++) {

                        Problem prob = ProblemFactory.getInstance().getProblem(probName);
                        double[] epsilonDouble = new double[prob.getNumberOfObjectives()];
                        for (int i = 0; i < prob.getNumberOfObjectives(); i++) {
                            epsilonDouble[i] = EpsilonHelper.getEpsilon(prob);
                        }
                        
                        //Setup algorithm parameters
                        Properties prop = new Properties();
                        prop.put("populationSize", "600");
                        prop.put("crediMemory", "1.0");
                        prop.put("HH", selector);
                        prop.put("CredDef", credDefStr);

                        //Choose heuristics to be applied. Use default values (probabilities)
                        ArrayList<Variation> heuristics = new ArrayList<>();
                        OperatorFactory of = OperatorFactory.getInstance();
                        Properties heuristicProp = new Properties();
                        heuristics.add(of.getVariation("um", heuristicProp, prob));
                        heuristics.add(of.getVariation("sbx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("de+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("pcx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("undx+pm", heuristicProp, prob));
                        heuristics.add(of.getVariation("spx+pm", heuristicProp, prob));

                        //Choose credit aggregation method
                        IQualityEstimation creditAgg = new RankRewards(1.0);

                        ICreditRepository credRepo = new SlidingWindowRepository(heuristics, new RewardHistoryWindow(windowSize), windowSize);

                        TypedProperties typeProp = new TypedProperties(prop);
                        typeProp.setDoubleArray("ArchiveEpsilon", epsilonDouble);
                        TestRun test = new TestRun(path, prob, probName,
                                typeProp, creditAgg, credRepo,
                                maxEvaluations);

                        //benchmark built-in MOEA
//                      TestRunBenchmark test = new TestRunBenchmark(path, prob, probName, 
//                            typeProp, "MOEAD", maxEvaluations);
                        futures.add(pool.submit(test));
                    }
                    for (Future<IHyperHeuristic> run : futures) {
                        try {
                            IHyperHeuristic hh = run.get();
                            
                            //save the approximation set
//                            NondominatedPopulation ndPop = instAlgorithm.getResult();
//                            try {
//                                PopulationIO.writeObjectives(new File(filename + ".NDpop"), ndPop);
//                            } catch (IOException ex) {
//                                Logger.getLogger(TestRunBenchmark.class.getName()).log(Level.SEVERE, null, ex);
//                            }
                            //save selection history
//                            IOSelectionHistory.saveHistory(((IHyperHeuristic) hh).getSelectionHistory(),
//                                    path + File.separator + "results" + File.separator + prob.getName() + "_"
//                                    + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName() + ".hist");

                            //save credit history
//                          IOCreditHistory.saveHistory(((IHyperHeuristic) hh).getCreditHistory(),
//                          path + File.separator + "results" + File.separator + problem.getName() + "_"
//                          + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName() + ".credit");
                            //save quality history
//                            IOQualityHistory.saveHistory(((IHyperHeuristic) hh).getQualityHistory(),
//                                    path + File.separator + "results" + File.separator + prob.getName() + "_"
//                                    + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName() + ".qual");
                            hh.reset();
                            hh=null;
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
