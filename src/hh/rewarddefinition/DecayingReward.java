/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition;

/**
 * The value of this credit decays exponentially at a rate determined by the 
 * parameter alpha.
 * 
 * Bai, R., Burke, E. K., Gendreau, M., Kendall, G., & McCollum, B. (2007). 
 * Memory Length in Hyper-heuristics: An Empirical Study. In 2007 IEEE Symposium
 * on Computational Intelligence in Scheduling (pp. 173–178). IEEE. 
 * doi:10.1109/SCIS.2007.367686
 * @author nozomihitomi
 */
public class DecayingReward extends Reward{
    private static final long serialVersionUID = 6624973624007991952L;

    /**
     * Learning rate: should be between [0,1];
     */
    private final double alpha;
    
    public DecayingReward(int t,double value,double alpha) {
        super(t,value);
        this.alpha = alpha;
        if(alpha>1 || alpha<0)
            System.err.println("WARNING:: decay parameter in DecayingCredit is "
                    + "" + alpha + ", outside of range [0,1]");
    }
    
    /**
     * This constructor converts a Credit into a decaying credit by copying over
     * the information stored in credit
     * @param credit 
     * @param alpha 
     */
    public DecayingReward(Reward credit,double alpha){
        this(credit.getIteration(),credit.getValue(),alpha);
    }
    
    /**
     * Fraction of original value since this credit value decays over time
     * @param iteration The current iteration
     * @return 1 since this credit class does not decay in value over time;
     */
    @Override
    public double fractionOriginalVal(int iteration){
        return Math.pow(alpha, iteration-this.iteration);
    }
}
