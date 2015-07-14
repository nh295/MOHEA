/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.qualityestimation.QualityEstimator;
import hh.rewarddefinition.Reward;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This class stores the history of rewards earned by a particular heuristic or operator.
 * @author nozomihitomi
 */
public abstract class AbstractRewardHistory implements IRewardHistory, Serializable{
    private static final long serialVersionUID = -41148639682799251L;

    protected LinkedList<Reward> rewardHistory;
    protected QualityEstimator qualEst;
    
    public AbstractRewardHistory(){
        rewardHistory = new LinkedList<>();
        qualEst = new QualityEstimator();
    }
    
    /**
     * Gets the most recent reward in the credit history
     * @return the most recent credit in the credit history
     */
    public Reward getMostRecentReward(){
        return rewardHistory.getFirst();
    }
    
    /**
     * Gets the ith most recent reward.
     * @param i the index of the reward desired. i=0 is the most recent reward
     * @return the ith most recent reward.
     */
    public Reward get(int i){
        return rewardHistory.get(i);
    }
    
    /**
     * Adds the reward to the head of the list
     * @param reward to add
     */
    @Override
    public void add(Reward reward) {
        rewardHistory.addFirst(reward);
    }

    /**
     * Returns the entire stored history as a linkedList. The first items in the
     * list are the most recent
     * @return entire stored history as a linkedList
     */
    @Override
    public LinkedList<Reward> getHistory() {
        return rewardHistory;
    }
    
    /**
     * Returns the iterator that iterates over the rewards in the history. 
     * Iterator should start from the most recent rewards and iterate back in 
     * time
     * @return iterator that iterates over the rewards in the history
     */
    @Override
    public Iterator<Reward> iterator() {
        return rewardHistory.iterator();
    }
    
    /**
     * Clears the stored history.
     */
    @Override
    public void clear(){
        rewardHistory.clear();
    }
    
    /**
     * Returns the number of rewards stored in the history
     * @return the number of rewards stored in the history
     */
    @Override
    public int size(){
        return rewardHistory.size();
    }
    
    /**
     * Returns the latest reward in the history. If the history is empty, returns a Reward with 0.0 value
     * @return 
     */
    @Override
    public Reward getLatest(){
        if(rewardHistory.isEmpty())
            return new Reward(-1,0.0);
        else
            return rewardHistory.getFirst();
    }
}
