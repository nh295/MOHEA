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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import hh.moea.SteadyStateIBEA;
import org.moeaframework.algorithm.IBEA;
import org.moeaframework.core.FitnessEvaluator;
import org.moeaframework.core.Indicator;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.fitness.IndicatorFitnessEvaluator;

/**
 * This hyperheuristic is uses R2 MOEA to manage the population. R2 MOEA is
 * steady state indicator based algorithm developed by Diaz et al in "A ranking
 * method based on the R2 indicator for many-objective optimization" 2013 IEEE
 * Congress on Evolutionary Computation
 *
 * @author SEAK2
 */
public class AOSIBEA extends IBEA implements IHyperHeuristic {

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

    private double[] upperbound;

    private double[] lowerbound;

    private NondominatedPopulation paretofront;

    /**
     * Name to id the hyper-heuristic
     */
    private String name;

    public AOSIBEA(Problem problem, Population population,
            NondominatedPopulation archive, Selection selection,
            Initialization initialization, IndicatorFitnessEvaluator fitnessEvaluator, INextHeuristic heuristicSelector,
            ICreditAssignment creditDef) {
        super(problem, archive, initialization, null, fitnessEvaluator);

        this.heuristics = heuristicSelector.getOperators();
        this.selection = selection;
        this.operatorSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.operatorSelectionHistory = new OperatorSelectionHistory(heuristics);
        this.qualityHistory = new OperatorQualityHistory(heuristics);
        this.creditHistory = new CreditHistory(heuristics);
        this.pprng = new ParallelPRNG();

        //Initialize 
        super.initialize();
        paretofront = new NondominatedPopulation(getPopulation());

    }

    @Override
    protected void iterate() {

        Population offspring = new Population();
        int populationSize = population.size();
        lowerbound = fitnessEvaluator.getLowerbound();
        upperbound = fitnessEvaluator.getUpperbound();

        Population prevGen = copyPrevGen(population);

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
//                    fitnessEvaluator.addAndUpdateFitnessOnly(prevGen,child);
//                    updatedBound(child);
                    switch (creditDef.getOperatesOn()) {
                        case PARENT:
                            creditValue += ((AbstractOffspringParent) creditDef).compute(fitnessEvaluator.normalize(child), fitnessEvaluator.normalize(refParent), prevGen, null);
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
//                        evaluate(child);
                        fitnessEvaluator.addAndUpdateFitnessOnly(prevGen, child);
                    switch (creditDef.getOperatesOn()) {
                        case POPULATION:
                            creditValue += ((AbstractOffspringPopulation) creditDef).compute(child, population);
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
//                        evaluate(child);
                    child.setAttribute("heuristic", new SerializableVal(operator.toString()));
                    fitnessEvaluator.addAndUpdateFitnessOnly(population, child);
                }
                HashMap<Variation, Credit> popContRewards;
                switch (creditDef.getOperatesOn()) {
                    case POPULATION:
                        popContRewards = ((AbstractPopulationContribution) creditDef).
                                compute(population, heuristics, this.numberOfEvaluations);
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
        fitnessEvaluator.evaluate(population);

        while (population.size() > populationSize) {
            int worstIndex = findWorstIndex();
            fitnessEvaluator.removeAndUpdate(population, worstIndex);
        }

    }

    private Population copyPrevGen(Population pop) {
        Population copyPop = new Population();
        for (Solution solution : pop) {
            Solution copySolution = solution.copy();
            copySolution.setAttribute("prevfitness", (double) solution.getAttribute(FitnessEvaluator.FITNESS_ATTRIBUTE));
            copyPop.add(copySolution);
        }
        return copyPop;
    }

    private void updatedBound(Solution solution) {
        for (int i = 0; i < lowerbound.length; i++) {
            lowerbound[i] = Math.min(lowerbound[i], solution.getObjective(i));
            upperbound[i] = Math.max(upperbound[i], solution.getObjective(i));
        }
    }

    public Solution normalize(Solution solution) {
        Solution out = solution.copy();
        for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
            out.setObjective(i, (solution.getObjective(i) - lowerbound[i]) / (upperbound[i] - lowerbound[i]));
        }
        return out;
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
