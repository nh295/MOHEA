/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.qualityestimation;

import hh.credithistory.IRewardHistory;
import hh.creditrepository.CreditHistoryRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.moeaframework.core.Variation;

/**
 * This quality estimator sums up all the credits belonging to a heuristic, for
 * each heuristic and then orders the summations. An ordinal rank is established
 * and quality is assigned based on the ordinal rank
 *
 * @author nozomihitomi
 */
public class RankRewards implements IQualityEstimation{

    private final QualityEstimator qualEst;

    private final double paramD;

    public RankRewards(double paramD) {
        if (paramD > 1.0 || paramD < 0.0) {
            throw new IllegalArgumentException("Invalid D value: D must be in [0,1]. D is " + paramD);
        }
        this.paramD = paramD;
        qualEst = new QualityEstimator();
    }

    /**
     * In this implementation, a quality measure can be NaN if the sum of the decayed ranks is zero. 
     * @param iteration
     * @param credHistRepo
     * @return 
     */
    @Override
    public HashMap<Variation, Double> estimate(int iteration, CreditHistoryRepository credHistRepo) {
        HashMap<Variation, Double> out = new HashMap<>();
        HashMap<Double, ArrayList<Variation>> sums = new HashMap<>();
        
        //put all sums into a hashmap with sum as key
        for (Variation heuristic : credHistRepo.getHeuristics()) {
            double sum = qualEst.sum(iteration, credHistRepo.getHistory(heuristic));
            if (sums.containsKey(sum)) {
                sums.get(sum).add(heuristic);
            } else {
                ArrayList<Variation> vars = new ArrayList<>();
                vars.add(heuristic);
                sums.put(sum, vars);
            }
        }

        //Find decay
        ArrayList<Double> sortedSums = new ArrayList(sums.keySet());
        Collections.sort(sortedSums);
        int rank = 0;
        double qualSum = 0.0;
        for (Double key : sortedSums) {
            ArrayList<Variation> heuristics = sums.get(key);
            Collections.shuffle(heuristics);
            for (Variation heuristic:heuristics) {
                double qual = Math.pow(paramD, rank)*key;
                out.put(heuristic, qual);
                qualSum += qual;
                rank++;
            }
        }
        //normalize quality measures
        for (Variation heuristic:out.keySet()) {
                out.put(heuristic, out.get(heuristic)/qualSum);
        }
        return out;
    }

    @Override
    public double estimate(int iteration, IRewardHistory rewardHistory) {
        throw new UnsupportedOperationException("This method is not supported for " + this.getClass());
    }

}
