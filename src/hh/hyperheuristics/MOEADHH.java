/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;


import hh.history.CreditHistory;
import hh.nextheuristic.INextHeuristic;
import hh.history.OperatorQualityHistory;
import hh.rewarddefinition.IRewardDefinition;
import hh.rewarddefinition.Reward;
import hh.history.OperatorSelectionHistory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import org.moeaframework.algorithm.MOEAD;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 *
 * @author SEAK2
 */
public class MOEADHH extends MOEAD implements IHyperHeuristic {

    /**
     * The type of heuristic selection method
     */
    private final INextHeuristic heuristicSelector;

    /**
     * The Credit definition to be used that defines how much credit to receive
     * for certain types of solutions
     */
    private final IRewardDefinition creditDef;


    /**
     * The history that stores all the heuristics selected by the hyper
     * heuristics. History can be extracted by getSelectionHistory(). Used for
     * analyzing the results to see the dynamics of heuristics selected
     */
    private OperatorSelectionHistory heuristicSelectionHistory;


    /**
     * The set of heuristics that the hyper heuristic is able to work with
     */
    private final Collection<Variation> heuristics;

    /**
     * The learning rate for the decaying credit value
     */
    private final double alpha;

    /**
     * The history of the heuristics' qualities over time. Used for analyzing
     * the results to see the dynamics of the heuristic qualities
     */
    private OperatorQualityHistory qualityHistory;

    /**
     * parallel purpose random generator
     */
    private final ParallelPRNG pprng;

    /**
     * Iteration count
     */
    private int iteration;

    /**
     * Name to id the hyper-heuristic
     */
    private String name;

    /**
     * Pareto Front
     */
    private NondominatedPopulation paretoFront;

    /**
     * The population contribution rewards from the previous iteration
     */
    private HashMap<Variation, Reward> prevPopContRewards;
    
    /**
     * Probability that an offspring will mate with neighbors
     */
    private double delta;
    
     /**
     * Indices for the population
     */
    private final List<Integer> popIndices;

    
    /**
     * crossover rate
     */
    private final double cr;

    public MOEADHH(Problem problem, int neighborhoodSize,
            Initialization initialization, double delta, double eta, int updateUtility,
            INextHeuristic heuristicSelector, IRewardDefinition creditDef,
            double alpha, double crossoverRate) {
        super(problem, neighborhoodSize, initialization, heuristicSelector.getHeuristics().iterator().next(), delta, eta, updateUtility);
        this.heuristics = heuristicSelector.getHeuristics();
        this.heuristicSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.alpha = alpha;
        this.delta = delta;
        this.cr = crossoverRate;
        this.heuristicSelectionHistory = new OperatorSelectionHistory(heuristics);
        this.qualityHistory = new OperatorQualityHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.iteration = 0;

        //Initialize the stored pareto front
        super.initialize();
        this.paretoFront = new NondominatedPopulation(getResult());

        //initialize the previous population contribution rewards to all zero for each heuristic
        prevPopContRewards = new HashMap<>();
        for (Variation heur : heuristics) {
            prevPopContRewards.put(heur, new Reward(0, 0.0));
        }
        
        popIndices = new ArrayList<Integer>();
        for(int i=0;i<population.size();i++)
            popIndices.add(i);

    }

    @Override
    public void iterate() {

        List<Integer> indices = getSubproblemsToSearch();

        for (Integer index : indices) {
            iteration++;
            //select next heuristic
            Variation heuristic = heuristicSelector.nextHeuristic();

            List<Integer> matingIndices = getMatingIndices(index);

            Solution[] parents = new Solution[heuristic.getArity()];
            parents[0] = population.get(index).getSolution();

            if (heuristic.getArity() > 2) {
                // mimic MOEA/D parent selection for differential evolution
                pprng.shuffle(matingIndices);

                for (int i = 1; i < heuristic.getArity() - 1; i++) {
                    parents[i] = population.get(
                            matingIndices.get(i - 1)).getSolution();
                }

                parents[heuristic.getArity() - 1]
                        = population.get(index).getSolution();
            } else {
                for (int i = 1; i < heuristic.getArity(); i++) {
                    parents[i] = population.get(
                            pprng.nextItem(matingIndices)).getSolution();
                }
            }
            
            Solution[] offspring;
            boolean inNeighborhood = pprng.nextDouble()<delta;
            if(inNeighborhood)
                offspring = heuristic.evolve(parents);
            else{
                offspring = new Solution[]{population.get(index).getSolution()};
                matingIndices = popIndices;
            }
            double reward = 0.0;
            for (Solution child : offspring) {
                evaluate(child);
                updateIdealPoint(child);
                List<Double> improvements = updateSolution(child, matingIndices);
                for(Double improv:improvements){
                    reward+= improv;
                }
            }
            heuristicSelector.update(new Reward(iteration, reward), heuristic);
            heuristicSelectionHistory.add(heuristic);
            updateQualityHistory();
        }

        generation++;

        if ((updateUtility >= 0) && (generation % updateUtility == 0)) {
            updateUtility();
        }

    }

    /**
     * Updates the quality history every iteration for each heuristic according
     * to the INextHeuristic class used
     */
    private void updateQualityHistory() {
        HashMap<Variation, Double> currentQualities = heuristicSelector.getQualities();
        for (Variation heuristic : heuristics) {
            qualityHistory.add(heuristic, currentQualities.get(heuristic));
        }
    }

    /**
     * Reset the hyperheuristic. Clear all selection history and the credit
     * repository
     */
    @Override
    public void reset() {
        iteration = 0;
        heuristicSelectionHistory.clear();
        heuristicSelector.reset();
        numberOfEvaluations = 0;
        qualityHistory.clear();
    }

    /**
     * Returns the ordered history of heuristics that were selected
     *
     * @return The ordered history of heuristics that were selected
     */
    @Override
    public OperatorSelectionHistory getSelectionHistory() {
        return heuristicSelectionHistory;
    }


    /**
     * gets the quality history stored for each heuristic in the hyper-heuristic
     *
     * @return
     */
    @Override
    public OperatorQualityHistory getQualityHistory() {
        return qualityHistory;
    }

    @Override
    public IRewardDefinition getCreditDefinition() {
        return creditDef;
    }

    @Override
    public INextHeuristic getNextHeuristicSupplier() {
        return heuristicSelector;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public CreditHistory getCreditHistory() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
