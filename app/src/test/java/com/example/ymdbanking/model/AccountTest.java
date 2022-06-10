package com.example.ymdbanking.model;

import junit.framework.TestCase;

import java.util.ArrayList;

public class AccountTest extends TestCase
{
	String accountName = "Daniel-1", accountNo = "A-1";
	double accountBalance = 1000;
	Account account = new Account(accountName,accountNo,accountBalance);

	public void testGetAccountName()
	{
		assertEquals(accountName,account.getAccountName());
	}

	public void testGetAccountNo()
	{
		assertEquals(accountNo,account.getAccountNo());
	}

	public void testGetAccountBalance()
	{
		assertEquals(accountBalance,account.getAccountBalance());
	}

	public void testSetAccountName()
	{
		String newAccountName = "Daniel-2";
		account.setAccountName(newAccountName);
		assertEquals(newAccountName,account.getAccountName());
	}

	public void testSetAccountNo()
	{
		String newAccountNo = "A-2";
		account.setAccountNo(newAccountNo);
		assertEquals(newAccountNo,account.getAccountNo());
	}

	public void testSetAccountBalance()
	{
		double newAccountBalance = 9999;
		account.setAccountBalance(newAccountBalance);
		assertEquals(newAccountBalance,account.getAccountBalance());
	}

	public void testGetTransactions()
	{
		int initialTransactions = 0;
		assertEquals(initialTransactions,account.getTransactions().size());
	}

	public void testAddPaymentTransaction()
	{
		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));

		String payeeID = "1",payeeName = "Arbiv-Corp.";
		Payee payee = new Payee(payeeID,payeeName);
		double amount = 1200;
		account.addPaymentTransaction(payee,1200);

		//Check if transactions size increased by 1
		assertEquals(1,account.getTransactions().size());

		//Validate payment transaction
		Transaction transaction = new Transaction(amount,"T1-P1",payeeID,payeeName);
		int index = account.getTransactions().size() - 1;
		//Validate amount
		assertEquals(amount,account.getTransactions().get(index).getAmount());
		//Validate transactionID
		assertEquals(transaction.getTransactionID(),account.getTransactions().get(index).getTransactionID());
		//Validate payeeID
		assertEquals(payeeID,account.getTransactions().get(index).getPayeeId());
		//Validate payeeName
		assertEquals(payeeName,account.getTransactions().get(index).getPayeeName());
		//Validate transaction type
		assertEquals(Transaction.TRANSACTION_TYPE.PAYMENT,transaction.getTransactionType());

		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));
	}

	public void testAddDepositTransaction()
	{
		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));

		String transactionID = "T1-D1",destinationCustomerId = "123456789";
		double amount = 2000;
		account.addDepositTransaction(destinationCustomerId,amount,"Credit");

		Transaction transaction = account.getTransactions().get(account.getTransactions().size() - 1);
		//Validate arrayList is increased by 1
		assertEquals(1,account.getTransactions().size());
		//Validate transactionID
		assertEquals(transactionID,transaction.getTransactionID());
		//Validate amount
		assertEquals(amount,transaction.getAmount());
		//Validate destinationAccount
		assertEquals(account.getAccountNo(),transaction.getDestinationAccount());
		//Validate destinationCustomerId
		assertEquals(destinationCustomerId,transaction.getDestinationCustomerId());

		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));
	}

	public void testAddLoanTransaction()
	{
		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));

		String transactionID = "T1-L1",destinationCustomerId = "123456789";
		double amount = 1000;
		//Create new deposit transaction
		account.addLoanTransaction(destinationCustomerId,amount);
		//Get new deposit transaction
		Transaction transaction = account.getTransactions().get(account.getTransactions().size() - 1);
		//Validate new deposit transaction
		//Validate transactionID
		assertEquals(transactionID,transaction.getTransactionID());
		//Validate amount
		assertEquals(amount,transaction.getAmount());
		//Validate destinationAccount
		assertEquals(account.getAccountNo(),transaction.getDestinationAccount());
		//Validate destinationCustomerId
		assertEquals(destinationCustomerId,transaction.getDestinationCustomerId());

		//Reset transactions arraylist to 0
		account.setTransactions(new ArrayList<>(0));
	}
}