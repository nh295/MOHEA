/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package hh.credittest;

import bitoperators.RandomSearch;
import bitoperators.SingleBitMutation;
import bitoperators.SinglePointCrossover;
import hh.IO.IOCreditHistory;
import hh.IO.IOSelectionHistory;
import hh.creditaggregation.CreditAggregateSum;
import hh.creditaggregation.ICreditAggregationStrategy;
import hh.creditdefinition.ICreditDefinition;
import hh.creditdefinition.aggregate.AggregateEArchiveCredit;
import hh.creditdefinition.immediate.ImmediateEArchiveCredit;
import hh.creditdefinition.immediate.ImmediateParetoFrontCredit;
import hh.creditdefinition.immediate.ImmediateParetoRankCredit;
import hh.creditdefinition.immediate.ParentDominationCredit;
import hh.creditrepository.CreditHistory;
import hh.creditrepository.CreditHistoryRepository;
import hh.heuristicgenerators.HyperGA;
import hh.heuristicselectors.AdaptivePursuit;
import hh.heuristicselectors.DMAB;
import hh.heuristicselectors.ProbabilityMatching;
import hh.heuristicselectors.RandomSelect;
import hh.hyperheuristics.HeMOEA;
import hh.hyperheuristics.IHyperHeuristic;
import hh.nextheuristic.INextHeuristic;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import org.moeaframework.core.spi.OperatorFactory;
import org.moeaframework.core.spi.ProblemFactory;
import org.moeaframework.problem.CEC2009.UF5;
import org.moeaframework.util.TypedProperties;
import rbsa.eoss.EOSS;
import rbsa.eoss.Params;
import rbsa.eoss.operators.AddSynergy;
import rbsa.eoss.operators.AddToSmallSat;
import rbsa.eoss.operators.ImproveOrbit;
import rbsa.eoss.operators.RemoveFromBigSat;
import rbsa.eoss.operators.RemoveInterference;
import rbsa.eoss.operators.RemoveSuperfluous;

/**
 *
 * @author nozomihitomi
 */
public class HHEOSS {
    
    private static String save;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String path;
        
        if(args.length == 0 ){
            path = "/Users/nozomihitomi/Dropbox/Cornell/CEE 6660 Systems Engineering Under Uncertainty/HHCreditTest";
            save = "";
        }
        else{
            path = args[0];
            save = args[1];
        }
        
        String probName = "EOSS";
        Params params = new Params(path, "FUZZY-ATTRIBUTES", "test","normal","");
        EOSS prob = new EOSS();
        
        String epsilon = "10,0.1,0.1,0.1";
        double[] epsilonDouble = new double[]{10,0.1,0.1,0.1};
        
        
        //Setup algorithm parameters
        Properties prop = new Properties();
        prop.put("populationSize", "100");
        prop.put("epsilon", epsilon);
        prop.put("alpha","1");
        
        int numberOfSeeds = 1;
        int maxEvaluations = 5100;
        
//            domain specific
        
        ArrayList<Variation> heuristics = new ArrayList<>();
        heuristics.add(new SingleBitMutation());
        heuristics.add(new SinglePointCrossover());
        heuristics.add(new RandomSearch());
        heuristics.add(new AddSynergy());
        heuristics.add(new AddToSmallSat());
        heuristics.add(new ImproveOrbit());
        heuristics.add(new RemoveFromBigSat());
        heuristics.add(new RemoveInterference());
        heuristics.add(new RemoveSuperfluous());
        
        //setup algorithm
        double pmin = 0.05;
        CreditHistoryRepository creditRepo = new CreditHistoryRepository(heuristics, new CreditHistory());
        INextHeuristic heuristicSelector = new ProbabilityMatching(creditRepo, pmin);
//        INextHeuristic heuristicSelector = new AdaptivePursuit(creditRepo, pmin);
//        INextHeuristic heuristicSelector = new DMAB(creditRepo, 0.01, 0.1, 10);
//        INextHeuristic heuristicSelector = new RandomSelect(creditRepo);
//            ICreditDefinition creditDef = new ParentDominationCredit(1, 0, 0);
//        ICreditDefinition creditDef = new ImmediateParetoFrontCredit(1.0, 0.0);
//        ICreditDefinition creditDef = new ImmediateEArchiveCredit(1, 0,epsilonDouble);
//        ICreditDefinition creditDef = new ImmediateParetoRankCredit(5, 1.0, 0.0);
        ICreditDefinition creditDef = new AggregateEArchiveCredit(1, 0,epsilonDouble);
        IHyperHeuristic hh = newHeMOEA(new TypedProperties(prop),prob,heuristicSelector,creditDef,heuristics);
        
        //loop through the set of algorithms to experiment with
        run(path, prob, probName, hh, prop,epsilonDouble, numberOfSeeds, maxEvaluations);
        
    }
    
    public static void run(String path,Problem prob, String probName, IHyperHeuristic hh, Properties prop,double[] epsilonDouble,int numberOfSeeds,int maxEvaluations){
        for(int k=0;k<numberOfSeeds;k++){
            //Setup run-time metric collector
            Instrumenter instrumenter = new Instrumenter().withFrequency(5000)
                    .withProblem(probName)
                    .attachAdditiveEpsilonIndicatorCollector()
                    .attachGenerationalDistanceCollector()
                    .attachHypervolumeCollector()
                    .withEpsilon(epsilonDouble)
                    .withReferenceSet(new File(path+File.separator+"pf"+File.separator + "EOSSref.ref"))
                    .attachElapsedTimeCollector();
            
            Algorithm instAlgorithm = instrumenter.instrument(hh);
            
            // run the executor using the listener to collect results
            System.out.println("Starting optimization on "+ prob.getName() + "_" + String.valueOf(k));
            
            System.out.printf("Percent done: \n");
            while (!instAlgorithm.isTerminated() && (instAlgorithm.getNumberOfEvaluations() < maxEvaluations)) {
                instAlgorithm.step();
                System.out.print("\b\b\b\b\b\b");
                System.out.printf("%02.4f",(double)instAlgorithm.getNumberOfEvaluations()/(double)maxEvaluations);
            }
            System.out.println("\nDone with optimization");
            
            Accumulator accum = ((InstrumentedAlgorithm)instAlgorithm).getAccumulator();
            
            File results = new File(path+File.separator+"results"+File.separator+prob.getName() + "_" +
                    hh.getNextHeuristicSupplier() +"_"+hh.getCreditDefinition()+"_"+String.valueOf(k)+"_"+save+".res");
            System.out.println("Saving results");
            
            try (FileWriter writer = new FileWriter(results)){
                Set<String> keys = accum.keySet();
                Iterator<String> keyIter = keys.iterator();
                while(keyIter.hasNext()){
                    String key = keyIter.next();
                    int dataSize = accum.size(key);
                    writer.append(key).append(",");
                    for(int i=0;i<dataSize;i++){
                        writer.append(accum.get(key, i).toString());
                        if(i+1<dataSize)
                            writer.append(",");
                    }
                    writer.append("\n");
                }
                
                writer.flush();
            }
            catch(IOException ex){
                Logger.getLogger(HHCreditTest.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            //save selection history
            IOSelectionHistory.saveHistory(((IHyperHeuristic)hh).getSelectionHistory(),
                    path+File.separator+"results"+File.separator+prob.getName() + "_" +
                            hh.getNextHeuristicSupplier() +"_"+hh.getCreditDefinition()+"_"+String.valueOf(k)+"_"+save+".hist");
            
            //save credit history
            IOCreditHistory.saveHistory(((IHyperHeuristic)hh).getCreditHistory(),
                    path+File.separator+"results"+File.separator+prob.getName() + "_" +
                            hh.getNextHeuristicSupplier() +"_"+hh.getCreditDefinition()+"_"+String.valueOf(k)+"_"+save+".credit");
            
            //reset history and credits embedded in hyperheuristic
            ((IHyperHeuristic)hh).reset();
            
        }
    }
    
    /**
     * Returns a new Hyper eMOEA instance.
     *
     * @param properties the properties for customizing the new {@code eMOEA}
     *        instance
     * @param problem the problem
     * @return a new {@code eMOEA} instance
     */
    private static IHyperHeuristic newHeMOEA(TypedProperties properties,
            Problem problem,INextHeuristic heuristicSelector,
            ICreditDefinition creditDef, Collection<Variation> heuristics) {
        int populationSize = (int)properties.getDouble("populationSize", 100);
        
        double alpha = properties.getDouble("alpha", 1);
            System.out.println("alpha:" + alpha);
        
        Initialization initialization = new RandomInitialization(problem,
                populationSize);
        
        Population population = new Population();
        
        DominanceComparator comparator = new ParetoDominanceComparator();
        
        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(
                properties.getDoubleArray("epsilon",
                        new double[] { EpsilonHelper.getEpsilon(problem) }));
        
        final TournamentSelection selection = new TournamentSelection(
                2, comparator);
        
        HeMOEA hemoea = new HeMOEA(problem, population, archive,
                selection, heuristics, initialization,
                heuristicSelector, creditDef,alpha);
        
        return hemoea;
    }
}
