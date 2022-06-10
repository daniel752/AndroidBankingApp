package com.example.ymdbanking.model;

import android.content.Context;

import junit.framework.TestCase;

import org.junit.Before;

import java.util.ArrayList;

public class ClerkTest extends TestCase
{
	String email,fullName,id,password,phone,username;
	Clerk clerk;

	@Before
	public void setUp()
	{
		email = "adir@gmail.com";
		fullName = "Adir Shaish";
		id = "12121212";
		password = "123456";
		phone = "123456789";
		username = "adir";
		clerk = new Clerk(email,fullName,id,password,phone,username);
	}

	public void testGetLoansToApprove()
	{
		//Validate loans arrayList size
		int expectedSize = 0;
		int actualSize = clerk.getLoansToApprove().size();
		assertEquals(expectedSize,actualSize);
	}

	public void testSetLoansToApprove()
	{
		String customerId = "123456789",accountName = "Daniel-1",accountNo = "A-1";
		String transactionID = "T1-L1";
		double initBalance = 1000,loanAmount = 2000;
		Account account = new Account(accountName,accountNo,initBalance);

		clerk.addLoanTransaction(customerId,account,loanAmount);

		//Validate arrayList size
		int expectedSize = 1;
		int actualSize = clerk.getLoansToApprove().size();
		assertEquals(expectedSize,actualSize);
		//Get transaction for validation
		Transaction transaction = clerk.getLoansToApprove().get(clerk.getLoansToApprove().size() - 1);
		//Validate loan transaction
		//Validate customerId
		assertEquals(customerId,transaction.getDestinationCustomerId());
		//Validate accountNo
		assertEquals(accountNo,transaction.getDestinationAccount());
		//Validate accountName
		assertEquals(transactionID,transaction.getTransactionID());
		//Validate amount
		assertEquals(loanAmount,transaction.getAmount());
		//Validate transaction type
		assertEquals(Transaction.TRANSACTION_TYPE.LOAN,transaction.getTransactionType());
	}

	public void testGetCustomers()
	{
		int initialCustomersSize = 0;
		assertEquals(initialCustomersSize,clerk.getCustomers().size());
	}

	public void testSetCustomers()
	{
		//Reset arrayList
		clerk.setCustomers(new ArrayList<>(0));

		clerk.getCustomers().add(new Customer("daniel@gmail.com","Daniel Arbiv","123456789",
				"123456","0501232130","daniel","USA"));
		int expectedSize = 1;
		int actualSize = clerk.getCustomers().size();
		assertEquals(expectedSize,actualSize);
		//Reset arrayList
		clerk.setCustomers(new ArrayList<>(0));
	}

	public void testAddLoanTransaction()
	{
		//Reset arrayList for test
		clerk.setLoansToApprove(new ArrayList<>(0));

		String customerId = "123456789";
		String accountName = "Daniel-1",accountNo = "A-1";
		double initialBalance = 1000;
		Account account = new Account(accountName,accountNo,initialBalance);
		double loanAmount = 2500;

		//Add loan transaction
		clerk.addLoanTransaction(customerId,account,loanAmount);
		//Get loan transaction
		Transaction transaction = clerk.getLoansToApprove().get(clerk.getLoansToApprove().size() - 1);
		//Validate transaction's info
		String transactionID = "T1-L1";
		//Validate transactionID
		assertEquals(transactionID,transaction.getTransactionID());
		//Validate destinationAccount
		assertEquals(accountNo,transaction.getDestinationAccount());
		//Validate amount
		assertEquals(loanAmount,transaction.getAmount());
		//Validate customerId
		assertEquals(customerId,transaction.getDestinationCustomerId());

		//Reset arrayList for test
		clerk.setLoansToApprove(new ArrayList<>(0));
	}

	public void testAddLoanForPending()
	{
		//Transaction details
		String transactionID = "T1-L1",customerId = "123456789";
		double loanAmount = 10000;
		//Account details
		String accountName = "Daniel-1",accountNo = "A-1";
		double accountBalance = 2000;
		//Init account
		Account account = new Account(accountName,accountNo,accountBalance);
		//Init transaction
		Transaction transaction = new Transaction(transactionID,account,loanAmount,customerId);
		//Add to clerk's pending loans arrayList
		clerk.addLoanForPending(transaction);
		//Get transaction to validate
		transaction = clerk.getLoansToApprove().get(clerk.getLoansToApprove().size() - 1);
		//Validate the element addition to arrayList
		int expectedSize = 1;
		int actualSize = clerk.getLoansToApprove().size();
		assertEquals(expectedSize,actualSize);
		//Validate transactionID
		assertEquals(transactionID,transaction.getTransactionID());
		//Validate customerId
		assertEquals(customerId,transaction.getDestinationCustomerId());
		//Validate loanAmount
		assertEquals(loanAmount,transaction.getAmount());
		//Validate transaction type is loan
		assertEquals(Transaction.TRANSACTION_TYPE.LOAN,transaction.getTransactionType());
		//Validate transaction status is pending
		assertEquals(Transaction.STATUS.PENDING,transaction.getStatus());

		//Reset arrayList for test
		clerk.setLoansToApprove(new ArrayList<>(0));
	}
}