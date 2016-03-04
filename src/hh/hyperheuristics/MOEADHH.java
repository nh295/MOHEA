/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.history.CreditHistory;
import hh.history.OperatorQualityHistory;
import hh.history.OperatorSelectionHistory;
import hh.nextheuristic.INextHeuristic;
import hh.creditassigment.IRewardDefinition;
import hh.creditassigment.Credit;
import hh.creditassignment.offspringparent.ParentDecomposition;
import hh.creditassignment.populationcontribution.DecompositionContribution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.moeaframework.algorithm.MOEAD;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * Implements MOEA/D-DRA.
 *
 * @author SEAK2
 */
public class MOEADHH extends MOEAD implements IHyperHeuristic {

    /**
     * The type of heuristic selection method
     */
    private final INextHeuristic operatorSelector;

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
     * The population contribution rewards from the previous iteration
     */
    private HashMap<Variation, Credit> prevPopContRewards;

    /**
     * Probability that an offspring will mate with neighbors
     */
    private final double delta;

    /**
     * Indices for the population
     */
    private final List<Integer> popIndices;
    
    /**
     * History of credits received by each operator
     */
    private final CreditHistory creditHistory;
    

    public MOEADHH(Problem problem, int neighborhoodSize,
            Initialization initialization, double delta, double eta, int updateUtility,
            INextHeuristic operatorSelector, IRewardDefinition creditDef) {
        super(problem, neighborhoodSize, initialization, operatorSelector.getOperators().iterator().next(), delta, eta, updateUtility);
        this.heuristics = operatorSelector.getOperators();
        this.operatorSelector = operatorSelector;
        this.creditDef = creditDef;
        this.delta = delta;
        this.heuristicSelectionHistory = new OperatorSelectionHistory(heuristics);
        this.creditHistory = new CreditHistory(heuristics);
        this.qualityHistory = new OperatorQualityHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.iteration = 0;

        super.initialize();

        //initialize the previous population contribution rewards to all zero for each heuristic
        prevPopContRewards = new HashMap<>();
        for (Variation heur : heuristics) {
            prevPopContRewards.put(heur, new Credit(0, 0.0));
        }

        popIndices = new ArrayList<>();
        for (int i = 0; i < population.size(); i++) {
            popIndices.add(i);
        }

    }
    
    public List<Solution> getPopulation(){
        ArrayList<Solution> out = new ArrayList<>();
        for(Individual ind:population)
            out.add(ind.getSolution());
        return out;
    }

    @Override
    public void iterate() {

        List<Integer> indices = getSubproblemsToSearch();

        for (Integer index : indices) {
            iteration++;
            //select next heuristic
            Variation operator = operatorSelector.nextHeuristic();

            List<Integer> matingIndices = getMatingIndices(index);

            Solution[] parents = new Solution[operator.getArity()];
            Individual parent = population.get(index);
            parents[0] = parent.getSolution();
            
            //decide mating pool
            boolean useNeighborhood = pprng.nextDouble() < delta;
            if(!useNeighborhood)
                matingIndices = new ArrayList(popIndices);

            if (operator.getArity() > 2) {
                // mimic MOEA/D parent selection for differential evolution
                pprng.shuffle(matingIndices);

                for (int i = 1; i < operator.getArity() - 1; i++) {
                    parents[i] = population.get(
                            matingIndices.get(i - 1)).getSolution();
                }

                parents[operator.getArity() - 1]
                        = population.get(index).getSolution();
            } else {
                for (int i = 1; i < operator.getArity(); i++) {
                    parents[i] = population.get(
                            pprng.nextItem(matingIndices)).getSolution();
                }
            }
            //create new offspring
            Solution[] offspring= operator.evolve(parents);

            //compute the credit assignment specific rewards
            switch (creditDef.getInputType()) {
                case OP:
                    double reward = 0;
                    for (Solution child : offspring) {
                        evaluate(child);
                        updateIdealPoint(child);
                        updateSolution(child, matingIndices);

                        ParentDecomposition OPDe = ((ParentDecomposition) creditDef);
                        OPDe.setWeights(parent.getWeights());
                        OPDe.setIdealPoint(getIdealPoint());
                        reward += OPDe.compute(child, parents[0], null, null);
                    }
                    if(reward <0)
                        reward = 0;
                    Credit operatorReward = new Credit(this.numberOfEvaluations, reward);
                    operatorSelector.update(operatorReward, operator);
                    creditHistory.add(operator, operatorReward);
                    break;
                case SI:
                    double rewardSi = 0;
                    for (Solution child : offspring) {
                        evaluate(child);
                        updateIdealPoint(child);
                        updateSolution(child, matingIndices);
                        rewardSi += updateSolution(child, matingIndices);
                    }
                    if(rewardSi <0)
                        reward = 0;
                    Credit operatorRewardSi = new Credit(this.numberOfEvaluations, rewardSi);
                    operatorSelector.update(operatorRewardSi, operator);
                    creditHistory.add(operator, operatorRewardSi);
                    break;
                case CS:
                    for (Solution child : offspring) {
                        evaluate(child);
                        updateIdealPoint(child);
                        updateSolution(child, matingIndices);
                        child.setAttribute("iteration", new SerializableVal(iteration));
                        child.setAttribute("heuristic", new SerializableVal(operator.toString()));
                    }
                    DecompositionContribution CDe = ((DecompositionContribution) creditDef);
                    HashMap<Variation, Credit> contRewards = CDe.compute(getNeighborhoodSolutions(index), null, null, heuristics, iteration);
                    Iterator<Variation> iter = contRewards.keySet().iterator();
                    while (iter.hasNext()) {
                        Variation operator_i = iter.next();
                        operatorSelector.update(contRewards.get(operator_i), operator_i);
                        creditHistory.add(operator_i, new Credit(this.numberOfEvaluations,contRewards.get(operator_i).getValue()));
                    }
                    break;
                default:
                    throw new NullPointerException("Credit definition not "
                            + "recognized. Used " + creditDef.getInputType() + ".");
            }

            heuristicSelectionHistory.add(operator,this.numberOfEvaluations);
//            updateQualityHistory();
            }

            generation++;

            if ((updateUtility >= 0) && (generation % updateUtility == 0)) {
                updateUtility();
            }

        }
        /**
         * Updates the quality history every iteration for each heuristic
         * according to the INextHeuristic class used
         */
    private void updateQualityHistory() {
        HashMap<Variation, Double> currentQualities = operatorSelector.getQualities();
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
        heuristicSelectionHistory.reset();
        operatorSelector.reset();
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
        return operatorSelector;
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
        return creditHistory;
    }

}
