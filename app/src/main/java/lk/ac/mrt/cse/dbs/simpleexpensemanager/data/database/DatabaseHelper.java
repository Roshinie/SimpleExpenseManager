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

package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Account;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.ExpenseType;
import lk.ac.mrt.cse.dbs.simpleexpensemanager.data.model.Transaction;

/**
 * Contains all the functionalities related to the sqlite database
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public  static final String DATABASE_NAME = "180281J";
    public  static final int DATABASE_VERSION = 2;
    public  static final String TBL1_NAME = "account_table";
    public  static final String TBL2_NAME= "transaction_table";



    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String create_q1 = "create table "+TBL1_NAME+" (accountNo TEXT(50) PRIMARY KEY,bankName TEXT(50),accountHolderName TEXT(50),balance REAL) ";
        String create_q2 =" create table "+TBL2_NAME+" (accountNo TEXT(50) ,date date, expenseType TEXT(20),amount REAL,FOREIGN KEY (accountNo) REFERENCES "+TBL1_NAME+"(accountNo))";
        sqLiteDatabase.execSQL(create_q1);
        sqLiteDatabase.execSQL(create_q2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String delete_q1 = "DROP TABLE IF EXISTS "+TBL1_NAME;
        String delete_q2 ="DROP TABLE IF EXISTS "+TBL2_NAME;
        sqLiteDatabase.execSQL(delete_q1);
        sqLiteDatabase.execSQL(delete_q2);
        onCreate(sqLiteDatabase);
    }
    /**
     * Get the details of all the available accounts
     */
    public ArrayList<Account> getAllAccounts(){
        ArrayList<Account> accountList=new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TBL1_NAME,null);

        if(res.getCount()==0){
            return accountList;
        }else{
            while(res.moveToNext()){
                String accountNo = res.getString(0);
                String bankName = res.getString(1);
                String accountHolderName = res.getString(2);
                double balance = res.getDouble(3);
                accountList.add(new Account(accountNo,bankName,accountHolderName,balance));
            }
            return accountList;
        }
    }
    /**
     * Get the details of an account by its account number
     */
    public Account getAccount(String accNo){
        Account account = null;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TBL1_NAME+" WHERE accountNo = ?",new String[]{accNo});

        if(res.getCount() == 0){
            return account;
        }else{
            while(res.moveToNext()){
                String accountNo = res.getString(0);
                String bankName = res.getString(1);
                String accountHolderName = res.getString(2);
                double balance = res.getDouble(3);
                account = new Account(accountNo,bankName,accountHolderName,balance);
            }
            return account;
        }
    }
    /**
     * Insert an new account to the database
     */
    public boolean insertAccount(Account account){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",account.getAccountNo());
        contentValues.put("bankName",account.getBankName());
        contentValues.put("accountHolderName",account.getAccountHolderName());
        contentValues.put("balance",account.getBalance());
        long result = db.insert(TBL1_NAME,null,contentValues);
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }
    /**
     * Delete an account from the database
     */
    public boolean deleteAccount(String accountNo){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TBL1_NAME,"accountNo = "+accountNo,null) > 0;
    }
    /**
     * Update the balance of the account
     */
    public boolean updateAccount(Account account){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",account.getAccountNo());
        contentValues.put("bankName",account.getBankName());
        contentValues.put("accountHolderName",account.getAccountHolderName());
        contentValues.put("balance",account.getBalance());
        long result = db.update(TBL1_NAME,contentValues,"accountNo = ?",new String[]{account.getAccountNo()});
        if(result == -1){
            return false;
        }else{
            return true;
        }
    }

    /**
     * Transaction functions
     * Insert new transactions to the database
     */
    public boolean logTransaction(Transaction transaction){

        DateFormat format = new SimpleDateFormat("m-d-yyyy", Locale.ENGLISH);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put("accountNo",transaction.getAccountNo());
        contentValues.put("date",format.format(transaction.getDate()));
        contentValues.put("expenseType",transaction.getExpenseType().toString());
        contentValues.put("amount",transaction.getAmount());

        long res = db.insert(TBL2_NAME,null,contentValues);
        if(res == -1){
            return false;
        }else{
            return true;
        }
    }
    /**
     * Get all the transactions
     */
    public ArrayList<Transaction> getTransactions(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TBL2_NAME,null);
        return populateTransactions(res);
    }
    /**
     * Get transactions upto a limit
     */
    public ArrayList<Transaction> getTransactions(int limit){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM "+TBL2_NAME+" LIMIT "+limit,null);
        return populateTransactions(res);
    }

    private ArrayList<Transaction> populateTransactions(Cursor res){

        ArrayList<Transaction> transactionList=new ArrayList<>();
        DateFormat format = new SimpleDateFormat("m-d-yyyy", Locale.ENGLISH);
        if(res.getCount()==0){
            return transactionList;
        }else{

            while(res.moveToNext()){
                String accountNo = res.getString(0);
                Date date = new Date();
                ExpenseType expenseType = ExpenseType.valueOf(res.getString(2));
                try {
                    date =  format.parse(res.getString(1));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
//

                double amount = res.getDouble(3);
                transactionList.add(new Transaction(date,accountNo,expenseType,amount));
            }
            return transactionList;
        }
    }
}
