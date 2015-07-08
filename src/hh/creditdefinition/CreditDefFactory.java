/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditdefinition;

import hh.creditdefinition.aggregate.AggregateEArchiveCredit;
import hh.creditdefinition.aggregate.AggregateParetoFrontCredit;
import hh.creditdefinition.aggregate.AggregateParetoRankCredit;
import hh.creditdefinition.immediate.ImmediateEArchiveCredit;
import hh.creditdefinition.immediate.ImmediateParetoFrontCredit;
import hh.creditdefinition.immediate.ImmediateParetoRankCredit;
import hh.creditdefinition.immediate.ParentDominationCredit;
import org.moeaframework.util.TypedProperties;

/**
 * Factory methods for creating instances of ICreditDefinition
 * @author SEAK2
 */
public class CreditDefFactory {

    /**
     * The default problem factory.
     */
    private static CreditDefFactory instance;

    /**
     * private constructor to enforce singleton
     */
    private CreditDefFactory(){
        super();
    }
    
    /**
     * Returns an instance of the hyper-heuristic factory
     * @return 
     */
    public static CreditDefFactory getInstance(){
        if(instance==null)
            return new CreditDefFactory();
        else 
            return instance;
    }
    
    public ICreditDefinition getCreditDef(String name, TypedProperties prop){
        ICreditDefinition credDef = null;
        double satisfy = prop.getDouble("satisfy", 1.0);
        double disatisfy = prop.getDouble("disatisfy", 0.0);
        double neither = prop.getDouble("neither", 0.0);
        double[] epsilon = prop.getDoubleArray("epsilon", null);
        switch(name){
            case "ODP": //offspring dominates parent
                credDef = new ParentDominationCredit(1.0, 0.0, 0.0);
                break;
            case "IPF": //immediate pareto front
                credDef = new ImmediateParetoFrontCredit(1.0, 0.0);
                break;
            case "IPR": //immediate pareto rank
                credDef = new ImmediateParetoRankCredit(1.0, 0.0,5);
                break;
            case "IEA": //immediate epsilon archive
                if(epsilon==null)
                    throw new NullPointerException("epsilon values must be defined");
                credDef = new ImmediateEArchiveCredit(1.0, 0,epsilon);
                break;
            case "APF": //aggregate pareto front
                credDef = new AggregateParetoFrontCredit(1.0,0.0);
                break;
            case "APR": //aggregate pareto rank
                credDef = new AggregateParetoRankCredit(1.0, 0.0,5);
                break;
            case "AEA": //aggregate epsilon archive
                if(epsilon==null)
                    throw new NullPointerException("epsilon values must be defined");
                credDef = new AggregateEArchiveCredit(1.0, 0.0, epsilon);
                break;
            default: throw new IllegalArgumentException("No such credit defintion: " + name);
        }
            
        return credDef;
    }
}
