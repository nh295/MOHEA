/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.experiment;

import hh.IO.IOCreditHistory;
import hh.IO.IOQualityHistory;
import hh.IO.IOSelectionHistory;
import hh.hyperheuristics.HHFactory;
import hh.hyperheuristics.HeMOEA;
import hh.hyperheuristics.IHyperHeuristic;
import hh.hyperheuristics.MOEADHH;
import hh.nextheuristic.INextHeuristic;
import hh.creditassigment.CreditFitnessFunctionType;
import hh.creditassigment.ICreditAssignment;
import hh.creditassigment.CreditDefFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.Instrumenter;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.analysis.collector.InstrumentedAlgorithm;
import org.moeaframework.analysis.sensitivity.EpsilonHelper;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.indicator.jmetal.FastHypervolume;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.util.TypedProperties;

/**
 * This sets up the experimental run. Need to define the problem, the
 * hyperheuristic components, and the search parameters
 *
 * @author nozomihitomi
 */
public class TestRun implements Callable {

    protected TypedProperties properties;
    protected Problem problem;
    protected String probName;
    protected String path;
    private ICreditAssignment rewardDef;
    protected double[] epsilonDouble;
    protected int maxEvaluations;
    private final Collection<Variation> heuristics;

    public TestRun(String path, Problem problem, String probName, TypedProperties properties,
            Collection<Variation> heuristics, int maxEvaluations) {

        this.heuristics = heuristics;
        this.properties = properties;
        this.problem = problem;
        this.epsilonDouble = properties.getDoubleArray("ArchiveEpsilon",
                new double[]{EpsilonHelper.getEpsilon(problem)});
        this.probName = probName;
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
    private IHyperHeuristic newHeMOEA() {

        int populationSize = (int) properties.getDouble("populationSize", 600);

        int injectionRate = (int) properties.getDouble("injectionRate", 0.25);

        //for injection
        int lagWindow = (int) properties.getDouble("lagWindow", 50);

        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        Population population = new Population();

        DominanceComparator comparator = new ParetoDominanceComparator();

        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

        final TournamentSelection selection = new TournamentSelection(
                2, comparator);

        //all other properties use default parameters
        INextHeuristic selector = HHFactory.getInstance().getHeuristicSelector(properties.getString("HH", null), properties, heuristics);


        HeMOEA hemoea = new HeMOEA(problem, population, archive, selection,
                initialization, selector, rewardDef, injectionRate, lagWindow);

        return hemoea;
    }

    /**
     * Returns a new Hyper eMOEA instance.
     *
     * @param properties the properties for customizing the new {@code eMOEA}
     * instance
     * @param problem the problem
     * @return a new {@code eMOEA} instance
     */
    private IHyperHeuristic newMOEADHH() throws IOException {

        int populationSize = (int) properties.getDouble("populationSize", 600);

        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        int neighborhoodSize = properties.getInt("neighborhood", 20);

        double delta = properties.getDouble("delta", 0.9);

        double eta = properties.getDouble("eta", 2.0);

        int updateUtility = properties.getInt("updateUtility", 50);

        
        INextHeuristic selector = HHFactory.getInstance().getHeuristicSelector(properties.getString("HH", null), properties, heuristics);

        MOEADHH moeadhh = new MOEADHH(problem, neighborhoodSize, initialization,
                delta, eta, updateUtility, selector, rewardDef);

        return moeadhh;
    }

    /**
     * Goes through one run of the algorithm. Returns the algorithm object. Can
     * get the population from the algorithm object
     *
     * @return the algorithm object. Can get the population from the algorithm
     * object
     * @throws Exception
     */
    @Override
    public IHyperHeuristic call() throws Exception {
        IHyperHeuristic hh;
        try {
            rewardDef = CreditDefFactory.getInstance().getCreditDef(properties.getString("CredDef", null), properties, problem);
        } catch (IOException ex) {
            Logger.getLogger(TestRun.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(rewardDef.getFitnessType()==CreditFitnessFunctionType.De)
             hh = newMOEADHH();
        else
            hh = newHeMOEA();
        
        double[] refPointObj = new double[problem.getNumberOfObjectives()];
        Arrays.fill(refPointObj, 1.1);

        Instrumenter instrumenter = new Instrumenter().withFrequency(maxEvaluations / 100)
                .withProblem(probName)
                .attachAdditiveEpsilonIndicatorCollector()
                .attachGenerationalDistanceCollector()
                .attachInvertedGenerationalDistanceCollector()
                .attachHypervolumeJmetalCollector(new Solution(refPointObj))
                .withEpsilon(epsilonDouble)
                .attachElapsedTimeCollector();

        InstrumentedAlgorithm instAlgorithm = instrumenter.instrument(hh);

        // run the executor using the listener to collect results
        System.out.println("Starting " + hh.getNextHeuristicSupplier() + rewardDef + " on " + probName + " with pop size: " + properties.getDouble("populationSize", 600));
        long startTime = System.currentTimeMillis();
        while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
            instAlgorithm.step();
        }

        hh.terminate();
        long finishTime = System.currentTimeMillis();
        System.out.println("Done with optimization. Execution time: " + ((finishTime - startTime) / 1000) + "s");

        Accumulator accum = instAlgorithm.getAccumulator();

        hh.setName(String.valueOf(System.nanoTime()));

        String filename = path + File.separator + properties.getProperties().getProperty("saveFolder") + File.separator + probName + "_" // + problem.getNumberOfObjectives()+ "_"
                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName();

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
                Arrays.fill(refPointObj, 1.1);
                FastHypervolume fHV = new FastHypervolume(problem, ProblemFactory.getInstance().getReferenceSet(probName), new Solution(refPointObj));
                double hv = fHV.evaluate(ndPop);
                writer.append("Final HV, " + hv + "\n");

                writer.flush();
            } catch (IOException ex) {
                Logger.getLogger(TestRunBenchmark.class.getName()).log(Level.SEVERE, null, ex);
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

        if (Boolean.parseBoolean(properties.getProperties().getProperty("saveOperatorCreditHistory"))) {
            IOCreditHistory ioch = new IOCreditHistory();
            ioch.saveHistory(((IHyperHeuristic) hh).getCreditHistory(), filename + ".credit");
        }

        if (Boolean.parseBoolean(properties.getProperties().getProperty("saveOperatorSelectionHistory"))) {
            IOSelectionHistory iosh = new IOSelectionHistory();
            iosh.saveHistory(((IHyperHeuristic) hh).getSelectionHistory(), filename + ".hist");
        }

        //save operator quality history
        if (Boolean.parseBoolean(properties.getProperties().getProperty("saveOperatorQualityHistory"))) {
            IOQualityHistory.saveHistory(((IHyperHeuristic) hh).getQualityHistory(), filename + ".qual");
        }
        
        return hh;
    }
}
