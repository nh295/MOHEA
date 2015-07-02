/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hh.creditrepository;

import java.io.Serializable;

/**
 *
 * @author nozomihitomi
 */
public class CreditHistory extends AbstractCreditHistory implements Serializable{
    private static final long serialVersionUID = 6804265407688982872L;

    @Override
    public ICreditHistory getInstance() {
        return new CreditHistory();
    }
}
