/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.credittest;

import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.CreditDefFactory;
import hh.creditdefinition.ICreditDefinition;
import hh.creditrepository.ICreditRepository;
import hh.hyperheuristics.HHFactory;
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
import org.moeaframework.core.Selection;
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
    private ICreditDefinition creditDef;
    protected double[] epsilonDouble;
    protected int maxEvaluations;
    private final ICreditAggregationStrategy creditAgg;
    private final Collection<Variation> heuristics;
    private ICreditRepository creditRepo;

    public TestRun(String path, Problem problem, String probName, TypedProperties properties,
            ICreditAggregationStrategy creditAgg, ICreditRepository creditRepo,
            double[] epsilonDouble, int maxEvaluations) {

        this.heuristics = creditRepo.getHeuristics();
        this.creditRepo = creditRepo;
        this.creditAgg = creditAgg;
        this.properties = properties;
        this.problem = problem;
        this.probName = probName;
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
    private IHyperHeuristic newHeMOEA() {
        
        int populationSize = (int) properties.getDouble("populationSize", 600);
        double crediMemory = properties.getDouble("crediMemory", 1.0);

        System.out.println("alpha:" + crediMemory);

        Initialization initialization = new RandomInitialization(problem,
                populationSize);

        Population population = new Population();

        DominanceComparator comparator = new ParetoDominanceComparator();

        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(
                properties.getDoubleArray("epsilon",
                        new double[]{EpsilonHelper.getEpsilon(problem)}));

        final TournamentSelection selection = new TournamentSelection(
                2, comparator);
        
        //Use default values for selectors
        INextHeuristic selector = HHFactory.getInstance().getHeuristicSelector(properties.getString("HH", null), new TypedProperties(),heuristics);
        creditDef = CreditDefFactory.getInstance().getCreditDef(properties.getString("CredDef", null),  new TypedProperties());
                
        HeMOEA hemoea = new HeMOEA(problem, population, archive, selection,
            initialization, selector, creditDef, creditRepo,
            creditAgg, crediMemory);

        return hemoea;
    }
    
    /**
     * Goes through one run of the algorithm. Returns the algorithm object. Can get the population from the algorithm object
     * @return the algorithm object. Can get the population from the algorithm object
     * @throws Exception 
     */
    @Override
    public Object call() throws Exception {
        IHyperHeuristic hh = newHeMOEA();

        Instrumenter instrumenter = new Instrumenter().withFrequency(maxEvaluations)
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd--HH-mm-ss");
        String stamp = dateFormat.format(new Date());
        System.out.println("Starting "+ hh.getNextHeuristicSupplier() + creditDef +" on " + problem.getName() + "_" + stamp);

//            System.out.printf("Percent done: \n");
            while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
                instAlgorithm.step();
//                System.out.print("\b\b\b\b\b\b");
//                System.out.printf("%02.4f",(double)instAlgorithm.getNumberOfEvaluations()/(double)maxEvaluations);
            }
        System.out.println("Done with optimization");

        Accumulator accum = ((InstrumentedAlgorithm) instAlgorithm).getAccumulator();

        String filename = path + File.separator + "results" + File.separator + problem.getName() + "_"
                + hh.getNextHeuristicSupplier() + "_" + hh.getCreditDefinition() + "_" + stamp;
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
        
        //save the approximation set
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

        hh.terminate();
        hh = null;
        return hh;
    }

}
