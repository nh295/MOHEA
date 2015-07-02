/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bitoperators;

import java.io.Serializable;
import java.util.Random;
import org.moeaframework.core.Variation;

/**
 *
 * @author nozomihitomi
 */
public abstract class AbstractBitOperator implements Variation,Serializable{
    private static final long serialVersionUID = 330533285019188343L;
    
    
    /**
     * Random generator
     */
    protected Random rand;
    
    public AbstractBitOperator (){
        this.rand = new Random();
    }
    
}
