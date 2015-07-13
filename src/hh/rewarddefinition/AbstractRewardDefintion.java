/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.rewarddefinition;

/**
 *
 * @author SEAK2
 */
public class AbstractRewardDefintion implements IRewardDefinition{
    
    protected RewardDefinitionType type;
    
    protected RewardDefinedOn operatesOn;

    @Override
    public RewardDefinitionType getType() {
        return type;
    }   

    @Override
    public RewardDefinedOn getOperatesOn() {
        return operatesOn;
    }
    
}
