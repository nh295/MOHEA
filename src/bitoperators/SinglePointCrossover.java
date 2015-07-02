/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitoperators;

import org.moeaframework.core.Solution;

/**
 * Finds a random crossover point with uniform probability and performs a single
 * point crossover about that point
 * @author nozomihitomi
 */
public class SinglePointCrossover extends AbstractBitOperator{
    private static final long serialVersionUID = -8572874883627209931L;

    @Override
    public int getArity() {
        return 2;
    }

    @Override
    public Solution[] evolve(Solution[] parents) {
        Solution mother = parents[0];
        Solution father = parents[1];
        
        int crsspoint = rand.nextInt(mother.getNumberOfVariables()-2)+1;
        Solution child1 = mother.copy();
        Solution child2 = father.copy();
        
        for(int i=crsspoint;i< mother.getNumberOfVariables();i++){
        child1.setVariable(i, father.getVariable(i));
        child2.setVariable(i, mother.getVariable(i));
        }
        return new Solution[]{child1,child2};
    }
    
}
