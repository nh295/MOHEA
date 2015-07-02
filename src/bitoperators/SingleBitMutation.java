/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitoperators;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryVariable;

/**
 *Chooses one bit to mutate from the decision variables, and flips the bit.
 * @author nozomihitomi
 */
public class SingleBitMutation extends AbstractBitOperator{
    private static final long serialVersionUID = 2399645483990107238L;

	/**
	 * Constructs a single bit mutation operator.
	 */
	public SingleBitMutation() {
		super();
	}

	@Override
	public Solution[] evolve(Solution[] parents) {
		Solution result = parents[0].copy();
                
                int bit2Flip = rand.nextInt(result.getNumberOfVariables());
                BinaryVariable orig = ((BinaryVariable)result.getVariable(bit2Flip));
                orig.set(0, !orig.get(0));

		return new Solution[] { result };
	}

	@Override
	public int getArity() {
		return 1;
	}
}
