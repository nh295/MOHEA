/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.hyperheuristics;

import java.io.Serializable;

/**
 *
 * @author nozomihitomi
 */
public class SerializableVal implements Serializable{
    private static final long serialVersionUID = 4724654853355168854L;
    
    double dval;
    int ival;
    String sval;
    
   public SerializableVal(double val){
       this.dval = val;
   }
   
   public SerializableVal(int val){
       this.ival = val;
   }
   
   public SerializableVal(String val){
       this.sval = val;
   }

    public double getDval() {
        return dval;
    }

    public int getIval() {
        return ival;
    }

    public String getSval() {
        return sval;
    }
   
   
   
}
