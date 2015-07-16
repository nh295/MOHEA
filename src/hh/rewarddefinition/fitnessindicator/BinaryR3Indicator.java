/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.rewarddefinition.fitnessindicator;


import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

/**
 *
 * @author nozomihitomi
 */
public class BinaryR3Indicator extends BinaryR2Indicator{

    public BinaryR3Indicator(Solution referencePt,int numVecs) {
        super(referencePt,numVecs);
    }

    /**
     * 
     * @param popA
     * @param popB can be the reference population
     * @return 
     */
    @Override
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB) {
        double val = 0.0;
        for(WtVector vec: wtVecs){
            double utilB = popUtility(vec,popB);
           val+= (utilB-popUtility(vec,popA))/utilB;
        }
        
        return val/wtVecs.size();
    }

    @Override
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation refPop) {
        return compute(popA,refPop);
    }
    
     @Override
    public String toString() {
        return "BIR3";
    }

    
}
