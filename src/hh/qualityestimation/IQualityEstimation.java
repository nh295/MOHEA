/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.qualityestimation;

import hh.rewarddefinition.Reward;
import hh.credithistory.ICreditHistory;
import java.io.Serializable;

/**
 *
 * @author nozomihitomi
 */
public interface IQualityEstimation extends Serializable{
    
    /**
     * Aggregates the history using an aggregation function to produce one 
     * Credit value
     * @param iteration the iteration of the search
     * @param creditHistory the history to aggregate
     * @return The aggregated credit
     */
    public Reward aggregateCredit(int iteration, ICreditHistory creditHistory);
}
