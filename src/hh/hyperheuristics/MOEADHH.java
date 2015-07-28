/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.hyperheuristics;

import hh.credithistory.RewardHistory;
import hh.creditrepository.CreditHistoryRepository;
import hh.creditrepository.ICreditRepository;
import hh.nextheuristic.INextHeuristic;
import hh.qualityestimation.IQualityEstimation;
import hh.qualityhistory.HeuristicQualityHistory;
import hh.rewarddefinition.IRewardDefinition;
import hh.rewarddefinition.Reward;
import hh.selectionhistory.HeuristicSelectionHistory;
import hh.selectionhistory.IHeuristicSelectionHistory;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
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
     * The repository that will store all the credits earned by the heuristics
     */
    private ICreditRepository creditRepo;

    /**
     * the credit aggregation scheme used to process the credits from previous
     * iterations
     */
    private final IQualityEstimation creditAgg;

    /**
     * The history that stores all the heuristics selected by the hyper
     * heuristics. History can be extracted by getSelectionHistory(). Used for
     * analyzing the results to see the dynamics of heuristics selected
     */
    private IHeuristicSelectionHistory heuristicSelectionHistory;

    /**
     * The credit history of all heuristics at every iteration. Can be extracted
     * by getCreditHistory(). Used for analyzing the results to see the dynamics
     * of the instantaneous credits received
     */
    private CreditHistoryRepository creditHistory;

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
     * crossover rate
     */
    private final double cr;

    public MOEADHH(Problem problem, int neighborhoodSize,
            Initialization initialization, double delta, double eta, int updateUtility,
            INextHeuristic heuristicSelector, IRewardDefinition creditDef,
            ICreditRepository creditRepo, IQualityEstimation creditAgg,
            double alpha, double crossoverRate) {
        super(problem, neighborhoodSize, initialization, heuristicSelector.getHeuristics().iterator().next(), delta, eta, updateUtility);
        checkHeuristics(heuristicSelector, creditRepo);
        this.heuristics = heuristicSelector.getHeuristics();
        this.heuristicSelector = heuristicSelector;
        this.creditRepo = creditRepo;
        this.creditDef = creditDef;
        this.creditAgg = creditAgg;
        this.alpha = alpha;
        this.cr = crossoverRate;
        this.heuristicSelectionHistory = new HeuristicSelectionHistory(heuristics);
        this.creditHistory = new CreditHistoryRepository(heuristics, new RewardHistory());
        this.qualityHistory = new HeuristicQualityHistory(heuristics);
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
            if(pprng.nextDouble()<cr)
                offspring = heuristic.evolve(parents);
            else
                offspring = new Solution[]{population.get(index).getSolution()};

            double reward = 0.0;
            for (Solution child : offspring) {
                evaluate(child);
                updateIdealPoint(child);
                List<Double> improvements = updateSolution(child, matingIndices);
                for(Double improv:improvements){
                    reward+= improv;
                }
            }
            creditRepo.update(heuristic, new Reward(iteration, reward));
            heuristicSelector.update(creditRepo, creditAgg);
            heuristicSelectionHistory.add(heuristic);
            updateCreditHistory();
            updateQualityHistory();
        }

        generation++;

        if ((updateUtility >= 0) && (generation % updateUtility == 0)) {
            updateUtility();
        }

    }

    /**
     * Updates the credit history every iteration for each heuristic according
     * to the INextHeuristic class used
     */
    private void updateCreditHistory() {
        for (Variation heuristic : heuristics) {
            creditHistory.update(heuristic, creditRepo.getLatestReward(heuristic));
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
     * Checks to see if the heuristics in the INextHeuristic and the credit
     * repository match
     *
     * @param heuristicSelector
     * @param creditRepo
     */
    private void checkHeuristics(INextHeuristic heuristicSelector, ICreditRepository creditRepo) {
        Iterator<Variation> iter = heuristicSelector.getHeuristics().iterator();
        Collection<Variation> repoHeuristics = creditRepo.getHeuristics();
        while (iter.hasNext()) {
            Variation heur = iter.next();
            if (!repoHeuristics.contains(heur)) {
                throw new RuntimeException("Mismatch in heuristics in INextHeuristic and ICrediRepository:" + heur);
            }
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
        creditHistory.clear();
        qualityHistory.clear();
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
     * Returns the entire history of credits for each heuristic.
     *
     * @return the entire history of credits for each heuristic.
     */
    @Override
    public CreditHistoryRepository getCreditHistory() {
        return creditHistory;
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

    @Override
    public IRewardDefinition getCreditDefinition() {
        return creditDef;
    }

    @Override
    public INextHeuristic getNextHeuristicSupplier() {
        return heuristicSelector;
    }

    /**
     * Returns the latest credit received by each heuristic
     *
     * @return the latest credit received by each heuristic
     */
    @Override
    public HashMap<Variation, Reward> getLatestCredits() {
        HashMap<Variation, Reward> out = new HashMap<>();
        Iterator<Variation> iter = creditRepo.getHeuristics().iterator();
        while (iter.hasNext()) {
            Variation heuristic = iter.next();
            out.put(heuristic, creditRepo.getLatestReward(heuristic));
        }
        return out;
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
