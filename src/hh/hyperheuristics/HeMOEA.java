/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.nextheuristic.INextHeuristic;
import hh.qualityhistory.HeuristicQualityHistory;
import hh.rewarddefinition.IRewardDefinition;
import hh.rewarddefinition.Reward;
import hh.rewarddefinition.RewardDefinitionType;
import hh.rewarddefinition.offspringparent.AbstractOffspringParent;
import hh.rewarddefinition.offspringpopulation.AbstractOffspringPopulation;
import hh.rewarddefinition.populationcontribution.AbstractPopulationContribution;
import hh.selectionhistory.HeuristicSelectionHistory;
import hh.selectionhistory.IHeuristicSelectionHistory;
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

/**
 *
 * @author Nozomi
 */
public class HeMOEA extends EpsilonMOEA implements IHyperHeuristic {

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
    private IHeuristicSelectionHistory heuristicSelectionHistory;


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
    private HeuristicQualityHistory qualityHistory;

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
     */
    public HeMOEA(Problem problem, Population population,
            EpsilonBoxDominanceArchive archive, Selection selection,
            Initialization initialization, INextHeuristic heuristicSelector,
            IRewardDefinition creditDef) {
        super(problem, population, archive, selection, null, initialization);

        this.heuristics = heuristicSelector.getHeuristics();
        this.selection = selection;
        this.heuristicSelector = heuristicSelector;
        this.creditDef = creditDef;
        this.heuristicSelectionHistory = new HeuristicSelectionHistory(heuristics);
        this.qualityHistory = new HeuristicQualityHistory(heuristics);
        this.pprng = new ParallelPRNG();
        this.iteration = 0;

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

        //select next heuristic
        Variation heuristic = heuristicSelector.nextHeuristic();

        Solution[] parents;

        Solution refParent; //used in OffspringParent Rewads
        if (archive.size() <= 1) {
            parents = selection.select(heuristic.getArity(), population);
            refParent = parents[pprng.nextInt(parents.length)];
        } else {
            refParent = archive.get(pprng.nextInt(archive.size()));
            parents = ArrayUtils.add(
                    selection.select(heuristic.getArity() - 1, population), refParent);
        }

        Solution[] children = heuristic.evolve(parents);

        if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPARENT) {
            for (Solution child : children) {
                evaluate(child);
                
                int solnRemoved = addToPopulation(child);
                double creditValue;
                //credit definitions operating on population and archive does 
                //NOT modify the population by adding the child to the population/archive
                switch (creditDef.getOperatesOn()) {
                    case PARENT:
                        creditValue = ((AbstractOffspringParent) creditDef).compute(child, refParent, null,solnRemoved);
                        break;
                    default:
                        throw new NullPointerException("Credit definition not "
                                + "recognized. Used " + creditDef.getType() + ".");
                }
                archive.add(child);
                heuristicSelector.update(new Reward(iteration, creditValue),heuristic);
            }
        } else if (creditDef.getType() == RewardDefinitionType.OFFSPRINGPOPULATION) {
            for (Solution child : children) {
                evaluate(child);
                double creditValue;
                //credit definitions operating on population and archive will 
                //modify the population by adding the child to the population/
                //archive. For computational efficiency (e.g. don't have to 
                //compute dominance for reward computation and for population update)
                switch (creditDef.getOperatesOn()) {
                    case PARETOFRONT:
                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, paretoFront);
                        archive.add(child);
                        break;
                    case ARCHIVE:
                        creditValue = ((AbstractOffspringPopulation) creditDef).compute(child, archive);
                        break;
                    default:
                        throw new NullPointerException("Credit definition not "
                                + "recognized. Used " + creditDef.getType() + ".");
                }
                addToPopulation(child);
                heuristicSelector.update(new Reward(iteration, creditValue),heuristic);
            }
        } else if (creditDef.getType() == RewardDefinitionType.POPULATIONCONTRIBUTION){
            for (Solution child : children) {
                evaluate(child);
                child.setAttribute("iteration", new SerializableVal(this.getNumberOfEvaluations()));
                child.setAttribute("heuristic", new SerializableVal(heuristic.toString()));
                addToPopulation(child);
            }
            boolean archiveChanged = archive.addAll(children);
            HashMap<Variation, Reward> popContRewards;
            switch (creditDef.getOperatesOn()) {
                case PARETOFRONT:
                    boolean PFchanged = paretoFront.addAll(children); //updated only if reward def uses the PF
                    if (!PFchanged) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).compute(paretoFront, heuristics, this.getNumberOfEvaluations());
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                case ARCHIVE:
                    if (!archiveChanged) {
                        popContRewards = reusePrevPopContRewards();
                    } else {
                        popContRewards = ((AbstractPopulationContribution) creditDef).compute(archive, heuristics, this.getNumberOfEvaluations());
                        prevPopContRewards = popContRewards; //update prevPopContRewards for future iterations
                    }
                    break;
                default:
                    throw new NullPointerException("Credit definition not "
                            + "recognized. Used " + creditDef.getType() + ".");
            }
            Iterator<Variation> iter = popContRewards.keySet().iterator();
            while(iter.hasNext()){
                Variation operator = iter.next();
                heuristicSelector.update(popContRewards.get(operator),operator);
            }
        }else{
            throw new UnsupportedOperationException("RewardDefinitionType not recognized ");
        }
//        heuristicSelectionHistory.add(heuristic);
//        updateCreditHistory();
        updateQualityHistory();
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
        HashMap<Variation, Double> currentQualities = heuristicSelector.getQualities();
        for (Variation heuristic : heuristics) {
            qualityHistory.add(heuristic, currentQualities.get(heuristic));
//            System.out.print(currentQualities.get(heuristic) + "\t");
        }
//        System.out.println();
    }

    /**
     * Returns the ordered history of heuristics that were selected
     *
     * @return The ordered history of heuristics that were selected
     */
    @Override
    public IHeuristicSelectionHistory getSelectionHistory() {
        return heuristicSelectionHistory;
    }
    /**
     * gets the quality history stored for each heuristic in the hyper-heuristic
     *
     * @return
     */
    @Override
    public HeuristicQualityHistory getQualityHistory() {
        return qualityHistory;
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

    @Override
    public IRewardDefinition getCreditDefinition() {
        return creditDef;
    }

    @Override
    public INextHeuristic getNextHeuristicSupplier() {
        return heuristicSelector;
    }

}
