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

    public BinaryR3Indicator(int numObj,int numVecs) {
        super(numObj,numVecs);
    }

    /**
     * 
     * @param popA
     * @param popB can be the reference population
     * @param refPt reference point
     * @return 
     */
    @Override
    public double compute(NondominatedPopulation popA, NondominatedPopulation popB,Solution refPt) {
        double val = 0.0;
        for(WtVector vec: wtVecs){
            double utilB = popUtility(vec,popB,refPt);
           val+= (utilB-popUtility(vec,popA,refPt))/utilB;
        }
        return val/wtVecs.size();
    }

    @Override
    public double computeWRef(NondominatedPopulation popA, NondominatedPopulation refPop,Solution refPt) {
        return compute(refPop,popA,refPt);
    }
    
    @Override
    public double compute(Solution solnA, Solution solnB, Solution refPt) {
        throw new UnsupportedOperationException("No papers use a IBEA R3 indicator. Need precedence first...");
    }
    
     @Override
    public String toString() {
        return "BIR3";
    }

    
}
