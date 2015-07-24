/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import hh.credithistory.RewardHistory;
import hh.qualityestimation.IQualityEstimation;
import hh.rewarddefinition.Reward;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.moeaframework.core.Variation;

/**
 * This class of credit repository stores reward for each heuristic. One Reward 
 * object is assigned for each heuristic. This does not store the history of 
 * rewards received over time
 * @author nozomihitomi
 */
public class CreditRepository implements ICreditRepository,Serializable{
    private static final long serialVersionUID = 1004365209150732930L;
    
    protected HashMap<Variation,Reward> creditRepository;
    
    /**
     * The most recently rewarded heuristic. There maybe multiple if using aggregated type reward definition
     */
    private ArrayList<Variation> lastRewardedHeuristic = new ArrayList();
    
    /**
     * This constructor creates the credit repository that initialize 0 reward for each heuristic
     * @param heuristics An iterable set of the candidate heuristics to be used
     */
    public CreditRepository(Collection<Variation> heuristics) {
        creditRepository = new HashMap(heuristics.size());
        Iterator<Variation> iter = heuristics.iterator();
        while(iter.hasNext()){
            creditRepository.put(iter.next(), new Reward(-1,0.0));
        }
    }

    /**
     * Method returns the current quality estimate for the specified heuristic based on its current reward
     * @param qualEst The method to aggregate the history of rewards. In this implementation, a quality estimator is not needed
     * @param iteration the iteration to aggregate up to. In this implementation, an iteration is not needed
     * @param heuristic to estimate quality for
     * @return the current quality estimate for the specified heuristic
     */
    @Override
    public double estimateQuality(IQualityEstimation qualEst, int iteration,Variation heuristic){
        RewardHistory rh = new RewardHistory();
        rh.add(creditRepository.get(heuristic));
        return qualEst.estimate(iteration,rh);
    }
    
    /**
     * Method returns the current qualities of all heuristics based on credit history
     * @param qualEst The method to estimate the quality of a heuristic based on the history of credits
     * @param iteration the iteration to aggregate up to from beginning of stored history
     * @return 
     */
    @Override
    public HashMap<Variation,Double> estimateQuality(IQualityEstimation qualEst, int iteration){
        HashMap<Variation,Double> out = new HashMap<>();
        for(Variation heuristic : getHeuristics()){
            out.put(heuristic, estimateQuality(qualEst,iteration,heuristic));
        }
        return out;
    }

    /**
     * Replaces the reward assigned to the specified heuristic with the given credit
     * @param heuristic the heuristic to query
     * @param reward that will replace old reward
     */
    @Override
    public void update(Variation heuristic, Reward reward) {
        creditRepository.put(heuristic, reward);
        lastRewardedHeuristic.clear();
        lastRewardedHeuristic.add(heuristic);
    }
    
    @Override
    public void update(HashMap<Variation,Reward> rewards) {
        lastRewardedHeuristic.clear();
        Iterator<Variation> iter = rewards.keySet().iterator();
        while(iter.hasNext()){
            Variation heuristic = iter.next();
            creditRepository.put(heuristic, rewards.get(heuristic));
            lastRewardedHeuristic.add(heuristic);
        }
    }
    
    /**
     * Returns the heuristics that are stored in this repository
     * @return the heuristics that are stored in this repository
     */
    @Override
    public Collection<Variation> getHeuristics() {
        return creditRepository.keySet();
    }    
    
    /**
     * Clears the reward stored in the repository. Resets credits to 0
     */
    @Override
    public void clear() {
        Iterator<Variation> iter = creditRepository.keySet().iterator();
        while(iter.hasNext()){
            creditRepository.put(iter.next(), new Reward(-1,0.0));
        }
    }

    @Override
    public Reward getLatestReward(Variation heuristic) {
        return creditRepository.get(heuristic);
    }

    @Override
    public Collection<Variation> getLastRewardedHeuristic() {
        return lastRewardedHeuristic;
    }
    
}
