/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.rewarddefinition.Reward;

/**
 * Credit history with a sliding window that keeps the last W earned Rewards. Window of rewards is first-in-first-out
 * @author nozomihitomi
 */
public class RewardHistoryWindow extends AbstractRewardHistory{
    private static final long serialVersionUID = 165645971050230636L;

    private int windowSize;
    
    public RewardHistoryWindow(int windowSize){
        super();
        this.windowSize = windowSize;
    }
    
    @Override
    public IRewardHistory getInstance() {
        return new RewardHistoryWindow(windowSize);
    }
    
    /**
     * Adds the credit to the head of the list and removes rewards outside of the sliding window.
     * @param reward to add
     */
    @Override
    public void add(Reward reward) {
        rewardHistory.addFirst(reward);
        if(rewardHistory.size()>windowSize)
            rewardHistory.removeLast();
    }
}
