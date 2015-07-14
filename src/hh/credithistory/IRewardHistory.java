/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.rewarddefinition.Reward;
import java.util.LinkedList;

/**
 *
 * @author Nozomi
 */
public interface IRewardHistory extends Iterable<Reward>{
    
    /**
     * Adds reward to the history
     * @param reward to add to the history
     */
    public void add(Reward reward);
    
    /**
     * Returns the reward history
     * @return the reward history
     */
    public LinkedList<Reward> getHistory();
    
    /**
     * Returns the latest reward in the history
     * @return 
     */
    public Reward getLatest();
    
    /**
     * Returns an instance of reward history.
     * @return an instance of reward history
     */
    public IRewardHistory getInstance();
    
    /**
     * Clears the stored reward history.
     */
    public void clear();
    
    /**
     * Returns the number of rewards stored in the history
     * @return the number of rewards stored in the history
     */
    public int size();
}
