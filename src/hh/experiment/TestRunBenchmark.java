/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.experiment;

import hh.hyperheuristics.IHyperHeuristic;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.algorithm.StandardAlgorithms;
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
public class TestRunBenchmark extends TestRun {

    private final String algorithm;
    private final TypedProperties prop;

    /**
     *
     * @param path
     * @param problem
     * @param probName
     * @param referenceSet reference set to use to compute indicator values such
     * as IGD
     * @param properties
     * @param algorithm
     * @param maxEvaluations
     */
    public TestRunBenchmark(String path, Problem problem, String probName, NondominatedPopulation referenceSet, TypedProperties properties, String algorithm, int maxEvaluations) {
        super(path, problem, probName, referenceSet, properties, null, maxEvaluations);
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

        InstrumentedAlgorithm instAlgorithm = super.instrument(alg);

        // run the executor using the listener to collect results
        String operatorName = prop.getProperties().getProperty("operator");
        System.out.println("Starting " + algorithm + " on " + problem.getName() + " with " + operatorName);
        long startTime = System.currentTimeMillis();
        while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
            instAlgorithm.step();
            if (Math.floorMod(instAlgorithm.getNumberOfEvaluations(), 1000) == 0) {
                System.out.println(instAlgorithm.getNumberOfEvaluations());
            }
        }
        alg.terminate();

        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        String name = String.valueOf(System.nanoTime());

        String filename = path + File.separator + prop.getProperties().getProperty("saveFolder") + File.separator + problem.getName() + "_"
                + algorithm + "_" + operatorName + "_" + name;

        super.saveIndicatorValues(instAlgorithm, filename);

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
