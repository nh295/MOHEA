/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityestimation;

import hh.credithistory.IRewardHistory;
import java.io.Serializable;

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
}
