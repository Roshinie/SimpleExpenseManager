/*
 * Copyright 2015 Department of Computer Science and Engineering, University of Moratuwa.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *                  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.impl;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.AccountDAO;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database.DatabaseHelper;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception.InvalidAccountException;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;

/**
 *This is the implementation of AccountDAO interface which handle the actions related to the account
 */
public class PersistentAccountDAO implements AccountDAO {
    private DatabaseHelper databaseHelper;

    public  PersistentAccountDAO(Context context){
        databaseHelper = new DatabaseHelper(context);
    }

    @Override
    public List<String> getAccountNumbersList() {
        ArrayList<String> acNumberList = new ArrayList<>();
        ArrayList<Account> accountDetails = databaseHelper.getAllAccounts();
        if(accountDetails.size()==0){
            return acNumberList;
        }else {
            for(Account acc:accountDetails){
                acNumberList.add(acc.getAccountNo());
            }
        }
        return acNumberList;
    }

    @Override
    public List<Account> getAccountsList() {
        return databaseHelper.getAllAccounts();
    }

    @Override
    public Account getAccount(String accountNo) throws InvalidAccountException {
        Account account = databaseHelper.getAccount(accountNo);
        return account;
    }

    @Override
    public void addAccount(Account account) {
        databaseHelper.insertAccount(account);
    }

    @Override
    public void removeAccount(String accountNo) throws InvalidAccountException {
        databaseHelper.deleteAccount(accountNo);
    }

    @Override
    public void updateBalance(String accountNo, ExpenseType expenseType, double amount) throws InvalidAccountException {
        if(accountNo ==null){
            throw new InvalidAccountException("Invalid Account Number");
        }

        Account account = databaseHelper.getAccount(accountNo);
        double balance = account.getBalance();
        if(expenseType == ExpenseType.INCOME){
            account.setBalance(balance+amount);
        }else if (expenseType == ExpenseType.EXPENSE){
            account.setBalance(balance-amount);
        }
        if(account.getBalance()<0 ){
            throw new InvalidAccountException("Insufficient credit");
        }
        else{
            databaseHelper.updateAccount(account);
        }
    }
}
