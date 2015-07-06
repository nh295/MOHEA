/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import hh.IO.IOCreditHistory;
import hh.IO.IOQualityHistory;
import hh.IO.IOSelectionHistory;
import hh.creditdefinition.ICreditDefinition;
import hh.hyperheuristics.HeMOEA;
import hh.hyperheuristics.IHyperHeuristic;
import hh.nextheuristic.INextHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.InstrumentedAlgorithm;
import org.moeaframework.analysis.sensitivity.EpsilonHelper;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.util.TypedProperties;

/**
 * This sets up the experimental run. Need to define the problem, the
 * hyperheuristic components, and the search parameters
 *
 * @author nozomihitomi
 */
public class TestRun implements Runnable {

    protected TypedProperties properties;
    protected Problem problem;
    protected String probName;
    protected String path;
    private INextHeuristic heuristicSelector;
    private ICreditDefinition creditDef;
    private Collection<Variation> heuristics;
    protected double[] epsilonDouble;
    protected int maxEvaluations;

    public TestRun(String path, Problem problem, String probName, TypedProperties properties,
            INextHeuristic heuristicSelector, ICreditDefinition creditDef,
            Collection<Variation> heuristics, double[] epsilonDouble, int maxEvaluations) {

        this.properties = properties;
        this.creditDef = creditDef;
        this.problem = problem;
        this.probName = probName;
        this.heuristics = heuristics;
        this.heuristicSelector = heuristicSelector;
        this.epsilonDouble = epsilonDouble;
        this.maxEvaluations = maxEvaluations;
        this.path = path;
    }

    /**
     * Returns a new Hyper eMOEA instance.
     *
     * @param properties the properties for customizing the new {@code eMOEA}
     * instance
     * @param problem the problem
     * @return a new {@code eMOEA} instance
     */
    private IHyperHeuristic newHeMOEA(TypedProperties properties,
            Problem problem, INextHeuristic heuristicSelector,
            ICreditDefinition creditDef, Collection<Variation> heuristics) {

        int populationSize = (int) properties.getDouble("populationSize", 600);
        double alpha = properties.getDouble("alpha", 1.0);

        System.out.println("alpha:" + alpha);

        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        Population population = new Population();

        DominanceComparator comparator = new ParetoDominanceComparator();

        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(
                properties.getDoubleArray("epsilon",
                        new double[]{EpsilonHelper.getEpsilon(problem)}));

        final TournamentSelection selection = new TournamentSelection(
                2, comparator);

        HeMOEA hemoea = new HeMOEA(problem, population, archive,
                selection, heuristics, initialization,
                heuristicSelector, creditDef, alpha);

        return hemoea;
    }

    @Override
    public void run() {
        IHyperHeuristic hh = newHeMOEA(properties, problem, heuristicSelector, creditDef, heuristics);

        Instrumenter instrumenter = new Instrumenter().withFrequency(maxEvaluations)
                .withProblem(probName)
                .attachAdditiveEpsilonIndicatorCollector()
                .attachGenerationalDistanceCollector()
                .attachHypervolumeCollector()
                .withEpsilon(epsilonDouble)
                .withReferenceSet(new File(path + File.separator + "pf" + File.separator + probName + ".dat"))
                .attachElapsedTimeCollector();

        Algorithm instAlgorithm = instrumenter.instrument(hh);

        // run the executor using the listener to collect results
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        String stamp = dateFormat.format(new Date());
        System.out.println("Starting "+ heuristicSelector + creditDef +" on " + problem.getName() + "_" + stamp);

//            System.out.printf("Percent done: \n");
            while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
                instAlgorithm.step();
//                System.out.print("\b\b\b\b\b\b");
//                System.out.printf("%02.4f",(double)instAlgorithm.getNumberOfEvaluations()/(double)maxEvaluations);
            }
        System.out.println("Done with optimization");

        Accumulator accum = ((InstrumentedAlgorithm) instAlgorithm).getAccumulator();

        File results = new File(path + File.separator + "results" + File.separator + problem.getName() + "_"
                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + stamp + ".res");
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

        hh.reset();
    }

}
