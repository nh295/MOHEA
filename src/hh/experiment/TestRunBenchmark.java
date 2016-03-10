/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.experiment;
import hh.hyperheuristics.IHyperHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
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
import org.moeaframework.core.Solution;
import org.moeaframework.core.indicator.jmetal.FastHypervolume;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.TypedProperties;

/**
 *
 * @author nozomihitomi
 */
public class TestRunBenchmark extends TestRun {

    private final String algorithm;
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
        prop.setDouble("updateUtility", 50.0);
        prop.setDouble("neighborhoodSize", 20.0);
        prop.setDouble("eta", 2.0);
        prop.setDouble("delta", 0.9);

        StandardAlgorithms sa = new StandardAlgorithms();
        Algorithm alg = sa.getAlgorithm(algorithm, prop.getProperties(), problem);
        
        double[] refPointObj = new double[problem.getNumberOfObjectives()];
        Arrays.fill(refPointObj, 1.1);
        
        Instrumenter instrumenter = new Instrumenter().withFrequency(maxEvaluations/100)
                .withProblem(probName)
                .attachAdditiveEpsilonIndicatorCollector()
                .attachGenerationalDistanceCollector()
                .attachInvertedGenerationalDistanceCollector()
                .attachHypervolumeJmetalCollector(new Solution(refPointObj))
                .withEpsilon(epsilonDouble)
                .attachElapsedTimeCollector();

        Algorithm instAlgorithm = instrumenter.instrument(alg);

        // run the executor using the listener to collect results
        String operatorName = prop.getProperties().getProperty("operator");
        System.out.println("Starting " + algorithm + " on " + problem.getName() + " with " + operatorName);
        long startTime = System.currentTimeMillis();
        while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
            instAlgorithm.step();
            if(Math.floorMod(instAlgorithm.getNumberOfEvaluations(),100) == 0)
                System.out.println(instAlgorithm.getNumberOfEvaluations());
        }
        alg.terminate();
        
        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        Accumulator accum = ((InstrumentedAlgorithm) instAlgorithm).getAccumulator();
        String name = String.valueOf(System.nanoTime());

        String filename = path + File.separator + prop.getProperties().getProperty("saveFolder") + File.separator + problem.getName() + "_"
                + algorithm + "_" + operatorName + "_" + name;
        
        if (Boolean.parseBoolean(properties.getProperties().getProperty("saveIndicators"))) {
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
                
                //also record the final HV
                NondominatedPopulation ndPop = instAlgorithm.getResult();
                FastHypervolume fHV = new FastHypervolume(problem, ProblemFactory.getInstance().getReferenceSet(probName), new Solution(refPointObj));
                double hv = fHV.evaluate(ndPop);
                writer.append("Final HV, " + hv + "\n");

                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(HHCreditTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (Boolean.parseBoolean(properties.getProperties().getProperty("saveFinalPopulation"))) {
            NondominatedPopulation ndPop = instAlgorithm.getResult();
            try {
                PopulationIO.writeObjectives(new File(filename + ".NDpop"), ndPop);
            } catch (IOException ex) {
                Logger.getLogger(TestRunBenchmark.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return null;
    }

}
