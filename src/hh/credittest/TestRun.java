/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.RewardDefFactory;
import hh.rewarddefinition.IRewardDefinition;
import hh.creditrepository.ICreditRepository;
import hh.hyperheuristics.HHFactory;
import hh.hyperheuristics.HeMOEA;
import hh.hyperheuristics.IHyperHeuristic;
import hh.nextheuristic.INextHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
public class TestRun implements Callable {

    protected TypedProperties properties;
    protected Problem problem;
    protected String probName;
    protected String path;
    private IRewardDefinition rewardDef;
    protected double[] epsilonDouble;
    protected int maxEvaluations;
    private final IQualityEstimation creditAgg;
    private final Collection<Variation> heuristics;
    private ICreditRepository creditRepo;

    public TestRun(String path, Problem problem, String probName, TypedProperties properties,
            IQualityEstimation creditAgg, ICreditRepository creditRepo,
            int maxEvaluations) {

        this.heuristics = creditRepo.getHeuristics();
        this.creditRepo = creditRepo;
        this.creditAgg = creditAgg;
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
        double crediMemory = properties.getDouble("creditMemory", 1.0);

        System.out.println("alpha:" + crediMemory);

        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        Population population = new Population();

        DominanceComparator comparator = new ParetoDominanceComparator();

        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);
                
        final TournamentSelection selection = new TournamentSelection(
                2, comparator);
        
        //Use default values for selectors
        INextHeuristic selector = HHFactory.getInstance().getHeuristicSelector(properties.getString("HH", null), new TypedProperties(),heuristics);
        rewardDef = RewardDefFactory.getInstance().getCreditDef(properties.getString("CredDef", null),  properties,problem);
                
        HeMOEA hemoea = new HeMOEA(problem, population, archive, selection,
            initialization, selector, rewardDef, creditRepo,
            creditAgg, crediMemory);

        return hemoea;
    }
    
    /**
     * Goes through one run of the algorithm. Returns the algorithm object. Can get the population from the algorithm object
     * @return the algorithm object. Can get the population from the algorithm object
     * @throws Exception 
     */
    @Override
    public IHyperHeuristic call() throws Exception {
        IHyperHeuristic hh = newHeMOEA();

        Instrumenter instrumenter = new Instrumenter().withFrequency(10000)
                .withProblem(probName)
                .attachAdditiveEpsilonIndicatorCollector()
                .attachGenerationalDistanceCollector()
                .attachInvertedGenerationalDistanceCollector()
                .attachHypervolumeCollector()
                .withEpsilon(epsilonDouble)
                .withReferenceSet(new File(path + File.separator + "pf" + File.separator + probName + ".dat"))
                .attachElapsedTimeCollector();

        Algorithm instAlgorithm = instrumenter.instrument(hh);

        // run the executor using the listener to collect results
        System.out.println("Starting "+ hh.getNextHeuristicSupplier() + rewardDef +" on " + problem.getName());

//            System.out.printf("Percent done: \n");
            while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
                instAlgorithm.step();
//                System.out.print("\b\b\b\b\b\b");
//                System.out.printf("%02.4f",(double)instAlgorithm.getNumberOfEvaluations()/(double)maxEvaluations);
            }
            
        hh.terminate();
        System.out.println("Done with optimization");

        Accumulator accum = ((InstrumentedAlgorithm) instAlgorithm).getAccumulator();

        hh.setName(String.valueOf(System.nanoTime()));
        String filename = path + File.separator + "results" + File.separator + problem.getName() + "_"
                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + hh.getName();
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

        return hh;
    }

}
