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
import hh.nextheuristic.INextHeuristic;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
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
public class AOSNSGAII extends NSGAII implements IHyperHeuristic {

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
     * Pareto front
     */
    private NondominatedPopulation paretofront;

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

        NondominatedSortingPopulation population = getPopulation();
        paretofront = new NondominatedPopulation(getPopulation());
        Population offspring = new Population();
        int populationSize = population.size();

        while (offspring.size() < populationSize) {

            //select next heuristic
            Variation operator = operatorSelector.nextHeuristic();
            operatorSelectionHistory.add(operator, this.numberOfEvaluations);

            Solution[] parents = selection.select(operator.getArity(),
                    population);
            Solution[] children = operator.evolve(parents);

            offspring.addAll(children);

            evaluateAll(children);

            if (creditDef.getInputType() == CreditFunctionInputType.OP) {
                double creditValue = 0.0;
                for (Solution child : children) {
                    Solution refParent = parents[pprng.nextInt(parents.length)];

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
                }

                Credit reward = new Credit(this.numberOfEvaluations, creditValue);
                operatorSelector.update(reward, operator);
                creditHistory.add(operator, reward);
            } else if (creditDef.getInputType() == CreditFunctionInputType.SI) {
                double creditValue = 0.0;
                for (Solution child : children) {
                    removedSolutions.clear();
                    //credit definitions operating on PF and archive will 
                    //modify the nondominated population by adding the child to the nondominated population.
                    switch (creditDef.getOperatesOn()) {
                        case PARETOFRONT:
                            creditValue += ((AbstractOffspringPopulation) creditDef).compute(child, paretofront);
                            break;
                        default:
                            throw new NullPointerException("Credit definition not "
                                    + "recognized. Used " + creditDef.getInputType() + ".");
                    }
                }
                Credit reward = new Credit(this.numberOfEvaluations, creditValue);
                operatorSelector.update(reward, operator);
                creditHistory.add(operator, reward);
            } else if (creditDef.getInputType() == CreditFunctionInputType.CS) {
                for (Solution child : children) {
                    child.setAttribute("heuristic", new SerializableVal(operator.toString()));
                    paretofront.addAll(children);
                }
                HashMap<Variation, Credit> popContRewards;
                switch (creditDef.getOperatesOn()) {
                    case PARETOFRONT:

                        popContRewards = ((AbstractPopulationContribution) creditDef).
                                compute(paretofront, heuristics, this.numberOfEvaluations);

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

        population.addAll(offspring);

        population.truncate(populationSize);

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
