/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityestimation;

import hh.credithistory.IRewardHistory;
import hh.creditrepository.CreditHistoryRepository;
import java.io.Serializable;
import java.util.HashMap;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public interface IQualityEstimation extends Serializable{
    
    /**
     * estimates the quality of a heuristic based on its rewards history
     * @param iteration the iteration of the search
     * @param rewardHistory the history to aggregate
     * @return The estimated quality
     */
    public double estimate(int iteration, IRewardHistory rewardHistory);
    
    /**
     * estimates the quality of all heuristics based on its rewards history
     * @param iteration the iteration of the search
     * @param credHistRepo the credit repository that stores all the credit histories
     * @return The estimated quality
     */
    public HashMap<Variation,Double> estimate(int iteration, CreditHistoryRepository credHistRepo);
}
