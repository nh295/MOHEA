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
public class QualityEstimator implements Serializable{
    private static final long serialVersionUID = 1914546705953536470L;
    
    private SumRewards summer;
    
    private MeanRewards averager;
    
    public QualityEstimator(){
        summer = new SumRewards();
        averager = new MeanRewards();
    }

    public double sum(int iteration, IRewardHistory history){
        return summer.estimate(iteration, history);
    }
    
    public double mean(int iteration, IRewardHistory history){
        return averager.estimate(iteration, history);
    }
}
