package com.example.ymdbanking.model;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class TransactionTest
{
	private Customer customer;

	@Before
	public void setUp()
	{
		//Setting customer info
		String email = "daniel@gmail.com";
		String fullName = "Daniel Arbiv";
		String id = "12345689";
		String password = "123456";
		String phone = "12345678";
		String username = "daniel";
		String country = "USA";
		customer = new Customer(email,fullName,id,password,phone,username,country);
	}

	@Test
	public void testTransfer()
	{
		//Sending account details
		String accountName = "Daniel-1",accountNo = "A1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);
		//Sending Account
		Account sendingAccount = customer.getAccounts().get(customer.getAccounts().size() - 1);
		//Receiving account
		String accountName2 = "Mazal-1",accountNo2 = "A-1";
		double initBalance2 = 2500;
		Account receivingAccount = new Account(accountName2,accountNo2,initBalance2);
		double transferAmount = 2000;
		customer.addTransferTransaction(sendingAccount,receivingAccount,transferAmount);
		double expectedAmount = 4500;
		double actualAmount = receivingAccount.getAccountBalance();
		assertEquals(expectedAmount,actualAmount,0.0001);
	}

	@Test
	public void testTransferOverAccountAmount()
	{
		//Sending account details
		String accountName = "Daniel-1",accountNo = "A1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);
		//Sending Account
		Account sendingAccount = customer.getAccounts().get(customer.getAccounts().size() - 1);
		//Receiving account
		String accountName2 = "Mazal-1",accountNo2 = "A1";
		double initBalance2 = 2500;
		Account receivingAccount = new Account(accountName2,accountNo2,initBalance2);
		//Transfer amount is over account's balance by 1
		//Should not approve transfer so balance of receiving account should stay the same
		double transferAmount = 2001;
		customer.addTransferTransaction(sendingAccount,receivingAccount,transferAmount);
		double expectedAmount = 2500;
		double actualAmount = receivingAccount.getAccountBalance();
		assertEquals(expectedAmount,actualAmount,0.0001);
	}

	@Test
	public void testDeposit()
	{
		//Account details
		String accountName = "Daniel-1",accountNo = "A1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);
		//Account
		Account account = customer.getAccounts().get(customer.getAccounts().size() - 1);
		double depositAmount = 100;

		account.addDepositTransaction(customer.getId(),depositAmount,"Credit");
		double expected = 2100;
		double actual = account.getAccountBalance();
		assertEquals(expected,actual,0.0001);
	}

	@Test
	public void testDepositUnderLimit()
	{
		//Account details
		String accountName = "Daniel-1",accountNo = "A1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);
		//Account
		Account account = customer.getAccounts().get(customer.getAccounts().size() - 1);
		//Deposit min limit is 100
		double depositAmount = 99;

		account.addDepositTransaction(customer.getId(),depositAmount,"Credit");
		//account's balance should not change
		double expected = 2000;
		double actual = account.getAccountBalance();
		assertEquals(expected,actual,0.0001);
	}

//	@Test
//	public void testLoan()
//	{
//		//Account details
//		String accountName = "Daniel-1",accountNo = "A1";
//		double initBalance = 2000;
//		customer.addAccount(accountName,initBalance);
//		//Account
//		Account account = customer.getAccounts().get(customer.getAccounts().size() - 1);
//
//		//Clerk details
//		String email = "adir@gmail.com";
//		String fullName = "Adir Shaish";
//		String id = "12121212";
//		String password = "123456";
//		String phone = "123456789";
//		String username = "adir";
//		Clerk clerk = new Clerk(email,fullName,id,password,phone,username);
//
//		//Create loan transaction
//		double loanAmount = 10000;
//		account.addLoanTransaction(customer.getId(),loanAmount);
//
//	}
}
