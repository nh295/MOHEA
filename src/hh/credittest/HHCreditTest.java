/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import com.sun.org.apache.bcel.internal.generic.AALOAD;
import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditaggregation.MeanCredits;
import hh.creditdefinition.CreditDefFactory;
import hh.creditdefinition.immediate.ImmediateEArchiveCredit;
import hh.creditdefinition.ICreditDefinition;
import hh.creditdefinition.aggregate.AggregateEArchiveCredit;
import hh.creditdefinition.aggregate.AggregateParetoFrontCredit;
import hh.creditdefinition.aggregate.AggregateParetoRankCredit;
import hh.creditdefinition.immediate.ParentDominationCredit;
import hh.creditdefinition.immediate.ImmediateParetoFrontCredit;
import hh.creditdefinition.immediate.ImmediateParetoRankCredit;
import hh.credithistory.CreditHistory;
import hh.credithistory.CreditHistoryWindow;
import hh.creditrepository.CreditHistoryRepository;
import hh.creditrepository.ICreditRepository;
import hh.heuristicselectors.AdaptivePursuit;
import hh.heuristicselectors.DMAB;
import hh.heuristicselectors.ProbabilityMatching;
import hh.heuristicselectors.RandomSelect;
import hh.hyperheuristics.HHFactory;
import hh.nextheuristic.INextHeuristic;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static ArrayList<Future<TestRun>> futures;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String[] problems = new String[]{"UF1", "UF2", "UF3", "UF4", "UF5", "UF6", "UF7", "UF8", "UF9", "UF10"};
//        String[] problems = new String[]{"UF1"};
//        String[] problems = new String[]{" "};
        String[] epsilons = new String[]{"0.001", "0.005", "0.0008", "0.005", "0.000001", "0.000001", "0.005", "0.0045", "0.008", "0.001"};

        for (int j = 0; j < problems.length; j++) {

            String path;
            if (args.length == 0) //                path = "/Users/nozomihitomi/Dropbox/MOHEA";
            {
                path = "C:\\Users\\SEAK2\\Nozomi\\MOHEA";
            } else {
                path = args[0];
            }

//            String probName = "UF"+args[1];
//             String probName = "UF3";
            String probName = problems[j];
            System.out.println(probName);
            Problem prob = ProblemFactory.getInstance().getProblem(probName);

            String eps = epsilons[j];
            int nobjs = prob.getNumberOfObjectives();
            String epsilon = "";
            double[] epsilonDouble = new double[nobjs];
            for (int i = 0; i < nobjs; i++) {
                epsilon += eps;
                if (i < nobjs - 1) {
                    epsilon += ",";
                }
                epsilonDouble[i] = Double.parseDouble(eps);
            }

            int numberOfSeeds = 30;
            int maxEvaluations = 300000;
            int windowSize = 300;

            //Setup heuristic selectors
            String[] selectors = new String[]{"PM", "AP"};
            TypedProperties hhProp = new TypedProperties();

            //setup credit definitions
            String[] creditDefs = new String[]{"ODP","IPF","IEA","APF","AEA"};
            TypedProperties credDefProp = new TypedProperties();

            //loop through the set of algorithms to experiment with
            for (String selector : selectors) {
                for (String credDefStr : creditDefs) {
                    pool = Executors.newFixedThreadPool(1);
                    //parallel process all runs
                    futures = new ArrayList<>();
                    for (int k = 0; k < numberOfSeeds; k++) {

                        //Setup algorithm parameters
                        Properties prop = new Properties();
                        prop.put("populationSize", "600");
//                      prop.put("alpha", args[2]);
                        prop.put("crediMemory", "1.0");
                        prop.put("epsilon", epsilon);
                        prop.put("HH",selector);
                        prop.put("CredDef",credDefStr);

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
                        ICreditAggregationStrategy creditAgg = new MeanCredits();

                        ICreditRepository credRepo = new CreditHistoryRepository(heuristics, new CreditHistoryWindow(windowSize));

                        TestRun test = new TestRun(path, prob, probName,
                                new TypedProperties(prop), creditAgg, credRepo,
                                epsilonDouble, maxEvaluations);

                        //benchmark built-in MOEA
//                      TestRunBenchmark test = new TestRunBenchmark(path, prob, probName, 
//                            prop, "NSGAII", epsilonDouble, maxEvaluations);
                        futures.add(pool.submit(test));
                    }
                    for (Future<TestRun> run : futures) {
                        try {
                            run.get();
                        } catch (InterruptedException | ExecutionException ex) {
                            Logger.getLogger(HHCreditTest.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    pool.shutdown();
                    System.out.println("Finished " + prob.getName() + "_" + selector + "_" + credDefStr + "\n\n");
                }
            }
        }
    }
}
