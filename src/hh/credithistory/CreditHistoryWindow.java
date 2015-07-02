/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.credithistory;

import hh.creditdefinition.Credit;

/**
 * Credit history with a sliding window that keeps the last W earned Credits. Window of credits is first-in-first-out
 * @author nozomihitomi
 */
public class CreditHistoryWindow extends AbstractCreditHistory{
    private static final long serialVersionUID = 165645971050230636L;

    private int windowSize;
    
    public CreditHistoryWindow(int windowSize){
        super();
        this.windowSize = windowSize;
    }
    
    @Override
    public ICreditHistory getInstance() {
        return new CreditHistoryWindow(windowSize);
    }
    
    /**
     * Adds the credit to the head of the list and removes credits outside of the sliding window.
     * @param credit to add
     */
    @Override
    public void addCredit(Credit credit) {
        creditHistory.addFirst(credit);
        if(creditHistory.size()>windowSize)
            creditHistory.removeLast();
    }
}
