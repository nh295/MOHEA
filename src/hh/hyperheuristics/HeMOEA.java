/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.history.CreditHistory;
import hh.nextheuristic.INextHeuristic;
import hh.history.OperatorQualityHistory;
import hh.rewarddefinition.CreditFunctionType;
import hh.rewarddefinition.IRewardDefinition;
import hh.rewarddefinition.Reward;
import hh.rewarddefinition.offspringparent.AbstractOffspringParent;
import hh.rewarddefinition.offspringpopulation.AbstractOffspringPopulation;
import hh.rewarddefinition.populationcontribution.AbstractPopulationContribution;
import hh.history.OperatorSelectionHistory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.commons.lang3.ArrayUtils;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.ParallelPRNG;
import org.moeaframework.core.Population;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Selection;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.operator.real.PM;

/**
 *
 * @author Nozomi
 */
public class HeMOEA extends EpsilonMOEA implements IHyperHeuristic {

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
     * This counter keeps track of the archive's epsilon progress
     */
    private int epsilonProgressCounter;

    /**
     * This stores the value of the epsilon progress from the last iteration
     */
    private int prevEpsilonProgressCount;

    /**
     * The allowed number of iterations without epsilon progress
     */
    private final int lagWindow;

    /**
     * The injection rate. The fraction of the population to populate with
     * archival solutions
     */
    private final double injectionRate;

    /**
     * Polynomial mutation operator used when after injecting archival solutions
     * into population does not fill up population
     */
    private PM pm;

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
     * Creates an instance of a hyper heuristic e-MOEA
     *
     * @param problem the problem to solve
     * @param population the type of population
     * @param archive the type of archive to implement
     * @param selection type of selection used to select higher fitness
     * individuals
     * @param initialization the method to initialize the population
     * @param heuristicSelector the heuristic selector to use in the
     * hyper-heuristic
     * @param creditDef the credit definition to score the performance of the
     * low-level heuristics
     * @param injectionRate The fraction of the population to populate with
     * archival solutions
     * @param lagWindow The allowed number of iterations without epsilon
     * progress
     */
    public HeMOEA(Problem problem, Population population,
            EpsilonBoxDominanceArchive archive, Selection selection,
            Initialization initialization, INextHeuristic heuristicSelector,
            IRewardDefinition creditDef, double injectionRate, int lagWindow) {
        super(problem, population, archive, selection, null, initialization);

        this.heuristics = heuristicSelector.getOperators();
        this.selection = selection;
        this.operatorSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.operatorSelectionHistory = new OperatorSelectionHistory(heuristics);
        this.qualityHistory = new OperatorQualityHistory(heuristics);
        this.creditHistory = new CreditHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.iteration = 0;
        this.epsilonProgressCounter = 0;
        this.injectionRate = injectionRate;
        this.pm = new PM(1 / problem.getNumberOfVariables(), 20);
        this.lagWindow = lagWindow;

        //Initialize the stored pareto front
        super.initialize();
        this.paretoFront = new NondominatedPopulation(getPopulation());

        //initialize the previous population contribution rewards to all zero for each heuristic
        prevPopContRewards = new HashMap<>();
        for (Variation heur : heuristics) {
            prevPopContRewards.put(heur, new Reward(0, 0.0));
        }
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * An iterations of this algorithm will select the next heuristic to be
     * applied, select the parents for that heuristic and select one child to
     * get evaluated. For heuristics that create more than one offspring, a
     * random offspring will be selected with uniform probability
     */
    @Override
    public void iterate() {
        iteration++;
//        System.out.println(paretoFront.size() + " iteration " + iteration);
        //Check epsilon progress (if the epsilon domination count increases in archive)
        if (prevEpsilonProgressCount == getArchive().getNumberOfDominatingImprovements()) {
            epsilonProgressCounter++;
            if (epsilonProgressCounter > lagWindow) {
                injectFromArchive();
            }
        }

        //select next heuristic
        Variation operator = operatorSelector.nextHeuristic();
//        operatorSelectionHistory.add(operator);

        Solution[] parents;

        Solution refParent; //used in OffspringParent Rewads
        if (archive.size() <= 1) {
            parents = selection.select(operator.getArity(), population);
            refParent = parents[pprng.nextInt(parents.length)];
        } else {
            refParent = archive.get(pprng.nextInt(archive.size()));
            parents = ArrayUtils.add(
                    selection.select(operator.getArity() - 1, population), refParent);
        }

        Solution[] children = operator.evolve(parents);
        if (creditDef.getType() == CreditFunctionType.OP) {
            double creditValue = 0;
            for (Solution child : children) {
                evaluate(child);
                Solution removedSoln = addToPopulation(child);
                if (removedSoln != null) {
                    //credit definitions operating on population and archive does 
                    //NOT modify the population by adding the child to the population/archive
                    switch (creditDef.getOperatesOn()) {
                        case PARENT:
                            creditValue += ((AbstractOffspringParent) creditDef).compute(child, refParent, population, removedSoln);
                            break;
                        default:
                            throw new NullPointerException("Credit definition not "
                                    + "recognized. Used " + creditDef.getType() + ".");
                    }
                }
                archive.add(child);
            }
            Reward reward = new Reward(this.numberOfEvaluations, creditValue);
            operatorSelector.update(reward, operator);
            creditHistory.add(operator, reward);
        } else if (creditDef.getType() == CreditFunctionType.SI) {
            double creditValue = 0.0;
            for (Solution child : children) {
                evaluate(child);
                Solution removedSoln = addToPopulation(child);
                if (removedSoln != null) { //solution made it in population
                    //credit definitions operating on PF and archive will 
                    //modify the nondominated population by adding the child to the nondominated population.
                    switch (creditDef.getOperatesOn()) {
                        case PARETOFRONT:
                            creditValue += ((AbstractOffspringPopulation) creditDef).compute(child, paretoFront);
                            archive.add(child);
                            break;
                        case ARCHIVE:
                            creditValue += ((AbstractOffspringPopulation) creditDef).compute(child, archive);
                            break;
                        default:
                            throw new NullPointerException("Credit definition not "
                                    + "recognized. Used " + creditDef.getType() + ".");
                    }
                }
            }
            Reward reward = new Reward(this.numberOfEvaluations, creditValue);
            operatorSelector.update(reward, operator);
            creditHistory.add(operator, reward);
        } else if (creditDef.getType() == CreditFunctionType.CS) {
            ArrayList<Solution> removedFromArchive = new ArrayList();
            ArrayList<Solution> childrenInArchive = new ArrayList();
            for (Solution child : children) {
                evaluate(child);
                child.setAttribute("iteration", new SerializableVal(iteration));
                child.setAttribute("heuristic", new SerializableVal(operator.toString()));
                Collection<Solution> removedA = archive.addAndReturnRemovedSolutions(child);
                if (archive.isChanged()) {
                    childrenInArchive.add(child);
                    if (removedA != null) {
                        removedFromArchive.addAll(removedA);
                    }
                }
            }
            HashMap<Variation, Reward> popContRewards;
            switch (creditDef.getOperatesOn()) {
                case PARETOFRONT:
                    ArrayList<Solution> removedFromPF = new ArrayList();
                    ArrayList<Solution> childrenInPF = new ArrayList();
                    for (Solution child : children) {
                        Collection<Solution> removedPF = paretoFront.addAndReturnRemovedSolutions(child);
                        if (paretoFront.isChanged()) {
                            childrenInPF.add(child);
                            if (removedPF != null) {
                                removedFromPF.addAll(removedPF);
                            }
                        }
                    }
                    //updated only if reward def uses the PF
                    if (!paretoFront.isChanged()) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).
                                compute(paretoFront, childrenInPF, removedFromPF, heuristics, this.numberOfEvaluations);
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                case ARCHIVE:
                    if (!archive.isChanged()) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).
                                compute(archive, childrenInArchive, removedFromArchive, heuristics, this.numberOfEvaluations);
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                default:
                    throw new NullPointerException("Credit definition not "
                            + "recognized. Used " + creditDef.getType() + ".");
            }
            Iterator<Variation> iter = popContRewards.keySet().iterator();
            while (iter.hasNext()) {
                Variation operator_i = iter.next();
                operatorSelector.update(popContRewards.get(operator_i), operator_i);
                creditHistory.add(operator_i, popContRewards.get(operator_i));
            }
        } else {
            throw new UnsupportedOperationException("RewardDefinitionType not recognized ");
        }
//        updateQualityHistory();
    }

    /**
     * This method is used when there is a stagnation in the epsilon progress.
     * It clears out the population and injects a portion of the archive into
     * the population. If there are more spaces remaining in the population
     * after that, the injected solutions are mutated until the desired
     * population size is reached
     */
    private void injectFromArchive() {
        int size = population.size();
        population.clear();
        for (int i = 0; i < Math.floor(((double) size) * injectionRate); i++) {
            ((NondominatedPopulation) population).forceAddWithoutCheck(archive.get(pprng.nextInt(archive.size())));
        }
        while (population.size() < size) {
            Solution[] solution2Mutate = new Solution[]{population.get(pprng.nextInt(population.size()))};
            Solution[] mutated = pm.evolve(solution2Mutate);
            ((NondominatedPopulation) population).forceAddWithoutCheck(mutated[0]);
        }
    }

    /**
     * reuses the previous population contribution rewards. This method updates
     * the iteration coutner in the rewards from the previous iteration
     *
     * @return the rewards for each heuristic with the up to date iteration
     * count
     */
    private HashMap<Variation, Reward> reusePrevPopContRewards() {
        for (Variation heur : heuristics) {
            Reward r = new Reward(iteration, prevPopContRewards.get(heur).getValue());
            prevPopContRewards.put(heur, r);
        }
        return prevPopContRewards;
    }

    /**
     * Updates the quality history every iteration for each heuristic according
     * to the INextHeuristic class used
     */
    private void updateQualityHistory() {
        HashMap<Variation, Double> currentQualities = operatorSelector.getQualities();
        for (Variation heuristic : heuristics) {
            qualityHistory.add(heuristic, currentQualities.get(heuristic));
//            System.out.print(currentQualities.get(heuristic) + "\t");
        }
//        System.out.println();
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
        iteration = 0;
        operatorSelectionHistory.reset();
        operatorSelector.reset();
        numberOfEvaluations = 0;
        qualityHistory.clear();
        population.clear();
        archive.clear();
        creditDef.clear();
    }

    @Override
    public IRewardDefinition getCreditDefinition() {
        return creditDef;
    }

    @Override
    public INextHeuristic getNextHeuristicSupplier() {
        return operatorSelector;
    }

}
