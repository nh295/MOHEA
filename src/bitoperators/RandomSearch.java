/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitoperators;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

/**
 * Replaces solution with a random solution
 * @author nozomihitomi
 */
public class RandomSearch extends AbstractBitOperator{
    private static final long serialVersionUID = 5771371939069191618L;

    public RandomSearch(){
        super();
    }
    
    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution result = parents[0].copy();
        for(int i=0;i<result.getNumberOfVariables();i++){
            BinaryVariable var = new BinaryVariable(1);
            var.set(0, rand.nextBoolean());
            result.setVariable(i, var);
        }
        return new Solution[]{result};
    }
    
}
