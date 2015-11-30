/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credittest;

import hh.hyperheuristics.IHyperHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.Instrumenter;
import org.moeaframework.algorithm.StandardAlgorithms;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.InstrumentedAlgorithm;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.util.TypedProperties;

/**
 *
 * @author nozomihitomi
 */
public class TestRunBenchmark extends TestRun{
    
    private String algorithm;
    private final TypedProperties prop;
    

    public TestRunBenchmark(String path, Problem problem, String probName, TypedProperties properties, String algorithm, int maxEvaluations) {
        super(path, problem, probName, properties, null, maxEvaluations);
        this.algorithm = algorithm;
        this.prop = properties;
    }
    
    @Override
    public IHyperHeuristic call() {
        //MOEAD properties
        prop.setDouble("de.crossoverRate", 1.0);
        prop.setDouble("updateUtility",50.0);
        prop.setDouble("neighborhoodSize", 20.0);
        prop.setDouble("eta", 2.0);
        prop.setDouble("delta", 0.9);
        
        StandardAlgorithms sa = new StandardAlgorithms();
        Algorithm alg = sa.getAlgorithm(algorithm, prop.getProperties(), problem);
        
        Instrumenter instrumenter = new Instrumenter().withFrequency(300000)
                .withProblem(probName)
                .attachAdditiveEpsilonIndicatorCollector()
                .attachGenerationalDistanceCollector()
                .attachInvertedGenerationalDistanceCollector()
                .attachHypervolumeCollector()
                .attachHypervolumeJmetalCollector()
                .withEpsilon(epsilonDouble)
//                .withReferenceSet(new File(path + File.separator + "pf" + File.separator + probName + ".dat"))
                .attachElapsedTimeCollector();

        Algorithm instAlgorithm = instrumenter.instrument(alg);

        // run the executor using the listener to collect results
        System.out.println("Starting "+ algorithm +" on " + problem.getName());
        long startTime = System.currentTimeMillis();
//            System.out.printf("Percent done: \n");
            while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
                instAlgorithm.step();
//                System.out.print("\b\b\b\b\b\b");
//                System.out.printf("%02.4f",(double)instAlgorithm.getNumberOfEvaluations()/(double)maxEvaluations);
            }
        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime-startTime)/1000) + "s");

        Accumulator accum = ((InstrumentedAlgorithm) instAlgorithm).getAccumulator();
        String name = String.valueOf(System.nanoTime());
        
        String filename = path + File.separator + "results" + File.separator + problem.getName() + "_"
                + algorithm + "_" + name;
        File results = new File(filename + ".res");
        System.out.println("Saving results");

        try (FileWriter writer = new FileWriter(results)) {
            Set<String> keys = accum.keySet();
            Iterator<String> keyIter = keys.iterator();
            while (keyIter.hasNext()) {
                String key = keyIter.next();
                int dataSize = accum.size(key);
                writer.append(key).append(",");
                for (int i = 0; i < dataSize; i++) {
                    writer.append(accum.get(key, i).toString());
                    if (i + 1 < dataSize) {
                        writer.append(",");
                    }
                }
                writer.append("\n");
            }

            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(HHCreditTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
//        NondominatedPopulation ndPop = instAlgorithm.getResult();
//        try {
//            PopulationIO.writeObjectives(new File(filename + ".NDpop"), ndPop);
//        } catch (IOException ex) {
//            Logger.getLogger(TestRunBenchmark.class.getName()).log(Level.SEVERE, null, ex);
//        }

        //save selection history
//        IOSelectionHistory.saveHistory(((IHyperHeuristic) hh).getSelectionHistory(),
//                path + File.separator + "results" + File.separator + problem.getName() + "_"
//                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + stamp + ".hist");

        //save credit history
//        IOCreditHistory.saveHistory(((IHyperHeuristic) hh).getCreditHistory(),
//                path + File.separator + "results" + File.separator + problem.getName() + "_"
//                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + stamp + ".credit");
        
        //save quality history
//        IOQualityHistory.saveHistory(((IHyperHeuristic) hh).getQualityHistory(),
//                path + File.separator + "results" + File.separator + problem.getName() + "_"
//                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + stamp + ".qual");

        alg.terminate();
        return null;
    }
    
}
