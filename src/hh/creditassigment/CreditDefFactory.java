/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hh.creditassigment;

import hh.creditassignment.fitnessindicator.AdditiveEpsilonIndicator;
import hh.creditassignment.fitnessindicator.HypervolumeIndicator;
import hh.creditassignment.fitnessindicator.R2Indicator;
import hh.creditassignment.offspringparent.OPBinaryIndicator;
import hh.creditassignment.offspringparent.ParentDecomposition;
import hh.creditassignment.offspringparent.ParentDomination;
import hh.creditassignment.offspringpopulation.OffspringEArchive;
import hh.creditassignment.offspringpopulation.OffspringNeighborhood;
import hh.creditassignment.offspringpopulation.OffspringParetoFront;
import hh.creditassignment.offspringpopulation.OffspringPopulationIndicator;
import hh.creditassignment.populationcontribution.DecompositionContribution;
import hh.creditassignment.populationcontribution.EArchiveContribution;
import hh.creditassignment.populationcontribution.IndicatorContribution;
import hh.creditassignment.populationcontribution.ParetoFrontContribution;
import java.io.IOException;
import java.util.Arrays;
import org.moeaframework.core.Problem;
import org.moeaframework.util.TypedProperties;

/**
 * Factory methods for creating instances of ICreditDefinition
 *
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
    private CreditDefFactory() {
        super();
    }

    /**
     * Returns an instance of the hyper-heuristic factory
     *
     * @return
     */
    public static CreditDefFactory getInstance() {
        if (instance == null) {
            return new CreditDefFactory();
        } else {
            return instance;
        }
    }

    public ICreditAssignment getCreditDef(String name, TypedProperties prop, Problem problem) throws IOException {
        ICreditAssignment credDef = null;
        //Get values from properties or use default values
        double satisfy = prop.getDouble("satisfy", 1.0);
        double disatisfy = prop.getDouble("disatisfy", 0.0);
        double neither = prop.getDouble("neither", 0.0);
        int rank = prop.getInt("rankThresh", 5);
        //reference point used to compute hypervolume
        double[] defRef = new double[problem.getNumberOfObjectives()];
        Arrays.fill(defRef, 2.0);
        double[] refPoint = prop.getDoubleArray("ref point", defRef);
        //ideal point used in R family indicators
        double[] defIdeal = new double[problem.getNumberOfObjectives()];
        Arrays.fill(defIdeal, 0.0);
        double[] idealPoint = prop.getDoubleArray("ref point", defIdeal);
        
        int numVec=0;
        if(problem.getNumberOfObjectives()==2){
            numVec = prop.getInt("numVec", 50);
        }else if(problem.getNumberOfObjectives()==3){
            numVec = prop.getInt("numVec", 91);
        }
        //kappa parameter used in IBEA
        double kappa = prop.getDouble("kappa", 0.05);
        int numObj = problem.getNumberOfObjectives();
        switch (name) {
            case "OPDe": //offspring improves parent in subproblem
                credDef = new ParentDecomposition();
                break;
            case "ODP": //offspring dominates parent
                credDef = new ParentDomination(satisfy, neither, disatisfy);
                break;
            case "OPIAE": //offspring parent additive epsilon indicator using pareto front
                credDef = new OPBinaryIndicator(new AdditiveEpsilonIndicator(), kappa,problem);
                break;
            case "OPIHV": //offspring parent hypervolume indicator using pareto front
                credDef = new OPBinaryIndicator(new HypervolumeIndicator(problem), kappa,problem);
                break;
            case "OPIR2": //offspring parent hypervolume indicator using pareto front
                credDef = new OPBinaryIndicator(new R2Indicator(numObj,numVec), kappa,problem);
                break;
//            case "OPIR3": //offspring parent hypervolume indicator using pareto front
//                credDef = new IBEABinaryIndicator(new BinaryR3Indicator(numObj,numVec), kappa, new Solution(idealPoint));
//                break;
            case "SIDe": //
                credDef = new OffspringNeighborhood();
                break;
            case "OPopPF": //in pareto front
                credDef = new OffspringParetoFront(satisfy, disatisfy);
                break;
            case "OPopEA": //in epsilon archive
                credDef = new OffspringEArchive(satisfy, disatisfy);
                break;
            case "OPopIPFAE": //offpsring improvement to additive epsilon indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new AdditiveEpsilonIndicator(),CreditDefinedOn.PARETOFRONT);
                break;
            case "OPopIPFHV":
                //offpsring improvement to hypervolume of pareto front
                credDef = new OffspringPopulationIndicator(new HypervolumeIndicator(problem),CreditDefinedOn.PARETOFRONT);
                break;
            case "OPopIPFR2": //offpsring improvement to R2 indicator value for pareto front
                credDef = new OffspringPopulationIndicator(new R2Indicator(numObj,numVec),CreditDefinedOn.PARETOFRONT);
                break;
//            case "OPopIPFR3": //offpsring improvement to R3 indicator value for pareto front
//                credDef = new OffspringPopulationIndicator(new BinaryR3Indicator(numObj,numVec),CreditDefinedOn.PARETOFRONT,new Solution(idealPoint));
//                break;
            case "OPopIEAAE": //offpsring improvement to additive epsilon indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new AdditiveEpsilonIndicator(),CreditDefinedOn.ARCHIVE);
                break;
            case "OPopIEAHV":
                //offpsring improvement to hypervolume of pareto front
                credDef = new OffspringPopulationIndicator(new HypervolumeIndicator(problem), CreditDefinedOn.ARCHIVE);
                break;
            case "OPopIEAR2": //offpsring improvement to R2 indicator value for epsilon archive
                credDef = new OffspringPopulationIndicator(new R2Indicator(numObj,numVec),CreditDefinedOn.ARCHIVE);
                break;
//            case "OPopIEAR3": //offpsring improvement to R3 indicator value for epsilon archive
//                credDef = new OffspringPopulationIndicator(new BinaryR3Indicator(numObj, numVec),CreditDefinedOn.ARCHIVE,new Solution(idealPoint));
//                break;
            case "CSDe": //contribution to the subproblem's neighborhood
                credDef = new DecompositionContribution(satisfy, disatisfy);
                break;
            case "CPF": //contribution to pareto front
                credDef = new ParetoFrontContribution(satisfy, disatisfy);
                break;
            case "CEA": //contribution to epsilon archive
                credDef = new EArchiveContribution(satisfy, disatisfy);
                break;
            case "CHVPF": //Contribution to HV of Pareto front
                credDef = new IndicatorContribution(new HypervolumeIndicator(problem), CreditDefinedOn.PARETOFRONT);
                break;
            case "CHVEA": //Contribution to HV of epsilon archive
                credDef = new IndicatorContribution(new HypervolumeIndicator(problem), CreditDefinedOn.ARCHIVE);
                break;
            case "CR2PF": //Contribution to R2 of Pareto front
                credDef = new IndicatorContribution(new R2Indicator(numObj,numVec), CreditDefinedOn.PARETOFRONT);
                break;
            case "CR2EA": //Contribution to R2 of epsilon archive
                credDef = new IndicatorContribution(new R2Indicator(numObj,numVec), CreditDefinedOn.ARCHIVE);
                break;
            default:
                throw new IllegalArgumentException("No such credit defintion: " + name);
        }

        return credDef;
    }
}
