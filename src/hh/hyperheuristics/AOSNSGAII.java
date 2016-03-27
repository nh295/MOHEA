/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.creditassigment.Credit;
import hh.creditassigment.CreditFunctionInputType;
import hh.creditassigment.ICreditAssignment;
import hh.creditassignment.offspringparent.AbstractOffspringParent;
import hh.creditassignment.offspringpopulation.AbstractOffspringPopulation;
import hh.creditassignment.populationcontribution.AbstractPopulationContribution;
import hh.history.CreditHistory;
import hh.history.OperatorQualityHistory;
import hh.history.OperatorSelectionHistory;
import hh.moea.SteadyStateNSGAII;
import hh.nextheuristic.INextHeuristic;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedSortingPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

/**
 * This hyperheuristic is uses steady state NSGAII. Every iteration a new
 * operator is selected
 *
 * @author SEAK2
 */
public class AOSNSGAII extends SteadyStateNSGAII implements IHyperHeuristic {

    /**
     * The type of heuristic selection method
     */
    private final INextHeuristic operatorSelector;

    /**
     * The Credit definition to be used that defines how much credit to receive
     * for certain types of solutions
     */
    private final ICreditAssignment creditDef;

    /**
     * The history that stores all the heuristics selected by the hyper
     * heuristics. History can be extracted by getSelectionHistory(). Used for
     * analyzing the results to see the dynamics of heuristics selected
     */
    private OperatorSelectionHistory operatorSelectionHistory;

    /**
     * The history that stores all the rewards received by the operators. Used
     * for analyzing the results to see the dynamics in rewards
     */
    private CreditHistory creditHistory;

    /**
     * The set of heuristics that the hyper heuristic is able to work with
     */
    private final Collection<Variation> heuristics;

    /**
     * The selection operator.
     */
    private final Selection selection;
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
     * Name to id the hyper-heuristic
     */
    private String name;

    /**
     * A temporary list to store solutions that are removed for the population
     * in order to correctly update the Pareto front indices
     */
    private ArrayList<Integer> removedSolutions;

    public AOSNSGAII(Problem problem, NondominatedSortingPopulation population,
            EpsilonBoxDominanceArchive archive, Selection selection,
            Initialization initialization, INextHeuristic heuristicSelector,
            ICreditAssignment creditDef) {
        super(problem, population, archive, selection, null, initialization);

        this.heuristics = heuristicSelector.getOperators();
        this.selection = selection;
        this.operatorSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.operatorSelectionHistory = new OperatorSelectionHistory(heuristics);
        this.qualityHistory = new OperatorQualityHistory(heuristics);
        this.creditHistory = new CreditHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.removedSolutions = new ArrayList<>();

        //Initialize the stored pareto front
        super.initialize();
    }

    @Override
    public void iterate() {

        //select next heuristic
        Variation operator = operatorSelector.nextHeuristic();
        operatorSelectionHistory.add(operator, this.numberOfEvaluations);

        //create new offspring
        Solution[] parents = selection.select(operator.getArity(), population);
        Solution[] children = operator.evolve(parents);

        if (creditDef.getInputType() == CreditFunctionInputType.OP) {
            double creditValue = 0.0;
            for (Solution child : children) {
                Solution refParent = parents[pprng.nextInt(parents.length)];
                evaluate(child);
                enlu.addSolution(child, population);

                //credit definitions operating on population and archive does 
                //NOT modify the population by adding the child to the population/archive
                switch (creditDef.getOperatesOn()) {
                    case PARENT:
                        creditValue += ((AbstractOffspringParent) creditDef).compute(child, refParent, population, null);
                        break;
                    default:
                        throw new NullPointerException("Credit definition not "
                                + "recognized. Used " + creditDef.getInputType() + ".");

                }

                int worstIndex = findWorstSolution();
                population.remove(worstIndex);
            }

            Credit reward = new Credit(this.numberOfEvaluations, creditValue);
            operatorSelector.update(reward, operator);
            creditHistory.add(operator, reward);
        } else if (creditDef.getInputType() == CreditFunctionInputType.SI) {
            try {
                double creditValue = 0.0;
                for (Solution child : children) {
                    removedSolutions.clear();
                    evaluate(child);
                    enlu.addSolution(child, population);
                    int worstIndex = findWorstSolution();
                    removedSolutions.add(worstIndex);
                    population.remove(worstIndex);

                    if (worstIndex != population.size()) { //solution made it in population
                        //credit definitions operating on PF and archive will 
                        //modify the nondominated population by adding the child to the nondominated population.
                        switch (creditDef.getOperatesOn()) {
                            case PARETOFRONT:

                                creditValue += ((AbstractOffspringPopulation) creditDef).compute(child, null);
                                break;
                            default:
                                throw new NullPointerException("Credit definition not "
                                        + "recognized. Used " + creditDef.getInputType() + ".");
                        }
                    }
                }
                Credit reward = new Credit(this.numberOfEvaluations, creditValue);
                operatorSelector.update(reward, operator);
                creditHistory.add(operator, reward);
            } catch (Exception e) {
                Logger.getLogger(AOSNSGAII.class.getName()).log(Level.SEVERE, null, e);
            }
        } else if (creditDef.getInputType() == CreditFunctionInputType.CS) {
            for (Solution child : children) {
                removedSolutions.clear();
                evaluate(child);
                child.setAttribute("heuristic", new SerializableVal(operator.toString()));
                enlu.addSolution(child, population);
                int worstIndex = findWorstSolution();
                removedSolutions.add(worstIndex); //only need to keep track of the latest removal
                population.remove(worstIndex);
            }
            HashMap<Variation, Credit> popContRewards;
            switch (creditDef.getOperatesOn()) {
                case PARETOFRONT:

                    popContRewards = ((AbstractPopulationContribution) creditDef).
                            compute(getParetoFront(), heuristics, this.numberOfEvaluations);

                    break;
                default:
                    throw new NullPointerException("Credit definition not "
                            + "recognized. Used " + creditDef.getInputType() + ".");
            }
            Iterator<Variation> iter = popContRewards.keySet().iterator();
            while (iter.hasNext()) {
                Variation operator_i = iter.next();
                operatorSelector.update(popContRewards.get(operator_i), operator_i);
                creditHistory.add(operator_i, new Credit(this.numberOfEvaluations, popContRewards.get(operator_i).getValue()));
            }
        } else {
            throw new UnsupportedOperationException("RewardDefinitionType not recognized ");
        }
//        updateQualityHistory();
    }

    /**
     * Finds the Pareto front using the Pareto rank from the efficient
     * nondomination update level method
     *
     * @return
     */
    private Population getParetoFront() {
        Population paretoFront = new Population();
        ArrayList<Integer> pfIndices = new ArrayList(enlu.getParetoFront());
        HashMap<Integer, Integer> map = new HashMap(); //key is old index, value is new index
        for (Integer index : pfIndices) {
            map.put(index, index);
        }
        for(Integer removedIndex : removedSolutions){
            map.remove(removedIndex);
        }

        //for every index that has been removed, update any Pareto front indices that are larger than it
        //do not include any index belonging to both the Pareto front and the removed list (this can occur if the Pareto front is the only front)
        Iterator<Integer> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            int pfIndex = iter.next();
            for (Integer removedIndex : removedSolutions) {
                if (pfIndex > removedIndex) {
                    map.put(pfIndex, map.get(pfIndex) - 1);
                }
            }
        }

        for (Integer index : map.values()) {
            if(index<0){
                System.err.println("");
            }
            paretoFront.add(population.get(index));
        }
        return paretoFront;
    }

    /**
     * Returns the ordered history of operators that were selected
     *
     * @return The ordered history of operators that were selected
     */
    @Override
    public OperatorSelectionHistory getSelectionHistory() {
        return operatorSelectionHistory;
    }

    /**
     * gets the quality history stored for each operator in the hyper-heuristic
     *
     * @return
     */
    @Override
    public OperatorQualityHistory getQualityHistory() {
        return qualityHistory;
    }

    /**
     * gets the credit history stored for each operator in the hyper-heuristic
     *
     * @return
     */
    @Override
    public CreditHistory getCreditHistory() {
        return creditHistory;
    }

    /**
     * Reset the hyperheuristic. Clear all selection history and the credit
     * repository. Clears the population and the archive
     */
    @Override
    public void reset() {
        operatorSelectionHistory.reset();
        operatorSelector.reset();
        numberOfEvaluations = 0;
        qualityHistory.clear();
        population.clear();
        archive.clear();
        creditDef.clear();
    }

    @Override
    public ICreditAssignment getCreditDefinition() {
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

}
