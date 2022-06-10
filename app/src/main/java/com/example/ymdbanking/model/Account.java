package com.example.ymdbanking.model;

import android.icu.text.Edits;

import com.example.ymdbanking.AccountsOverViewActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

/**
 * Class used to create an account for the user
 */

public class Account
{
	private String accountName;
	private String accountNo;
	private double accountBalance;
	private ArrayList<Transaction> transactions;

	private static final double DEPOSIT_MIN_LIMIT = AccountsOverViewActivity.getDepositMinLimit();
	private static final double LOAN_MIN_LIMIT = AccountsOverViewActivity.getLoanMinLimit();
//    private long dbID;

	public Account()
	{
		//Empty constructor
	}

	public Account(String accountName,String accountNo,double accountBalance)
	{
		this.accountName = accountName;
		this.accountNo = accountNo;
		this.accountBalance = accountBalance;
		transactions = new ArrayList<>(0);
	}

	public Account(String accountName,String accountNo,double accountBalance,ArrayList<Transaction> transactions)
	{
		this(accountName,accountNo,accountBalance);
		this.transactions = transactions;
	}

	/**
	 * Getters for the account name, number and balance
	 */
	public String getAccountName()
	{
		return accountName;
	}

	public String getAccountNo()
	{
		return accountNo;
	}

	public double getAccountBalance()
	{
		return accountBalance;
	}

	public void setAccountName(String accountName) {this.accountName = accountName;}

	public void setAccountNo(String accountNo) {this.accountNo = accountNo;}
	//    public void setDbID(long dbID) { this.dbID = dbID; }

	public void setAccountBalance(double accountBalance) { this.accountBalance = accountBalance; }

	public ArrayList<Transaction> getTransactions()
	{
		return transactions;
	}

	/**
	 * Method to implement payment to payee
	 * Adds a payment to transactions
	 *
	 * @param payee  - payee object
	 * @param amount - the amount to transfer to payee
	 */
	public void addPaymentTransaction(Payee payee,double amount)
	{
		accountBalance -= amount;

		int paymentCount = 0;

		for(int i = 0; i < transactions.size(); i++)
		{
			if(transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.PAYMENT)
			{
				paymentCount++;
			}
		}

		Transaction payment = new Transaction(amount,"T" + (transactions.size() + 1) + "-P" +
		                                             (paymentCount +
		                                              1),payee.getPayeeID(),payee.getPayeeName());
		transactions.add(payment);
//        transactions.put(payment.getTransactionID(),payment);
	}

	/**
	 * Method to implement a deposit to profile's account
	 *
	 * @param customerId - account's owner id
	 * @param amount     - amount to deposit in account
	 */
	public void addDepositTransaction(String customerId,double amount,String method)
	{
		if(amount >= DEPOSIT_MIN_LIMIT)
		{
			if(method.equals("Credit"))
				accountBalance += amount;

			int depositsCount = 0;

			try
			{
				for(int i = 0; i < transactions.size(); i++)
				{
					if(transactions.get(i).getTransactionType() ==
					   Transaction.TRANSACTION_TYPE.DEPOSIT)
					{
						depositsCount++;
					}
				}
			}
			catch(NullPointerException e)
			{
				transactions = new ArrayList<>(0);
				depositsCount = 0;
			}
			Transaction deposit;
			if(method.equals("Credit"))
				deposit = new Transaction("T" + (transactions.size() + 1) + "-D" +
				                          (depositsCount + 1),amount,this,customerId);
			else
				deposit = new Transaction(amount,this,customerId,
						"T" + (transactions.size() + 1) + "-D" + (depositsCount + 1));
			transactions.add(deposit);
		}
	}

	public void addLoanTransaction(String customerId,double amount)
	{
		accountBalance += amount;

		int loansCount = 0;

		for(int i = 0; i < transactions.size(); i++)
		{
			if(transactions.get(i).getTransactionType() == Transaction.TRANSACTION_TYPE.LOAN)
			{
				loansCount++;
			}
		}

		Transaction loan = new Transaction(
				"T" + (transactions.size() + 1) + "-L" + (loansCount + 1),this,amount,customerId);
		transactions.add(loan);
	}

	/**
	 * toString will be used by the account adapter
	 *
	 * @return
	 */
	public String toString()
	{
		return (accountName + " ($" + String.format(Locale.getDefault(),"%.2f",accountBalance) +
		        ")");
	}

	/**
	 * toString will be used by the account adapter
	 *
	 * @return
	 */
	public String toTransactionString() { return (accountName + " (" + accountNo + ")"); }

	public void setTransactions(ArrayList<Transaction> transactions) {this.transactions = transactions;}
}
