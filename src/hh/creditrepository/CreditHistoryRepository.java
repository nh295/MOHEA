/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditrepository;

import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.Reward;
import hh.credithistory.IRewardHistory;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * This class of credit repository stores the reward history over time for each
 * heuristic.
 *
 * @author nozomihitomi
 */
public class CreditHistoryRepository extends CreditRepository implements Serializable {

    private static final long serialVersionUID = -151125984931862164L;

    protected HashMap<Variation, IRewardHistory> creditHistory;

    /**
     * This constructor creates the credit repository that initialize 0 reward
     * for each heuristic
     *
     * @param heuristics An iterable set of the candidate heuristics to be used
     * @param history the type of history desired
     */
    public CreditHistoryRepository(Collection<Variation> heuristics, IRewardHistory history) {
        super(heuristics);
        creditHistory = new HashMap<>(heuristics.size());
        Iterator<Variation> iter = heuristics.iterator();
        while (iter.hasNext()) {
            creditHistory.put(iter.next(), history.getInstance());
        }
    }

    /**
     * Gets the entire history of the specified heuristic
     *
     * @param heuristic
     * @return
     */
    public IRewardHistory getHistory(Variation heuristic) {
        return creditHistory.get(heuristic);
    }

    /**
     * Updates the superclass CreditRepository which stores the latest rewards
     * earned by each heuristic
     *
     * @param heuristic
     * @param reward
     */
    protected void updateSuper(Variation heuristic, Reward reward) {
        super.update(heuristic, reward);
    }

    /**
     * Adds the new reward to the history of the credits
     *
     * @param heuristic the heuristic to query
     * @param reward that will be added to the history
     */
    @Override
    public void update(Variation heuristic, Reward reward) {
        updateSuper(heuristic, reward);
        creditHistory.get(heuristic).add(reward);
    }

    /**
     * Clears the credit histories stored in the repository.
     */
    @Override
    public void clear() {
        Iterator<Variation> iter = creditHistory.keySet().iterator();
        while (iter.hasNext()) {
            creditHistory.get(iter.next()).clear();
        }
    }

    /**
     * Gets the sum of all credit assigned to the specified heuristic, summed
     * over the history
     *
     * @param iteration The iteration to take the sum to from the beginning of the stored history
     * @param heuristic the heuristic to query
     * @return the sum of all credit assigned to the specified heuristic, summed
     * over the history
     */
    @Override
    public double estimateQuality(IQualityEstimation qualEst, int iteration, Variation heuristic) {
        return qualEst.estimate(iteration, creditHistory.get(heuristic));
    }
    
    /**
     * Method returns the current qualities of all heuristics based on credit history
     * @param qualEst The method to estimate the quality of a heuristic based on the history of credits
     * @param iteration the iteration to aggregate up to from beginning of stored history
     * @return 
     */
    @Override
    public HashMap<Variation,Double> estimateQuality(IQualityEstimation qualEst, int iteration){
        return qualEst.estimate(iteration, this);
    }
}
