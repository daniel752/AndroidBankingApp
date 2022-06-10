package com.example.ymdbanking.model;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class CustomerTest
{
	private Customer customer;

	@Before
	public void setUp() throws Exception
	{
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
	public void addAccount()
	{
		//Account details
		String accountName = "Daniel-1",accountNo = "A1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);
		Account account = customer.getAccounts().get(customer.getAccounts().size() - 1);
		assertEquals(accountName,account.getAccountName());
		assertEquals(accountNo,account.getAccountNo());
		assertEquals(initBalance,account.getAccountBalance(),0.0001);

		customer.setAccounts(new ArrayList<>(0));
	}

	@Test
	public void addPayee()
	{
		//Payee details
		String payeeName = "Corp1",payeeID = "P1";
		customer.addPayee(payeeName);
		Payee payee = customer.getPayees().get(customer.getPayees().size() - 1);
		assertEquals(payeeID,payee.getPayeeID());
		assertEquals(payeeName,payee.getPayeeName());
	}

	@Test
	public void addMessage()
	{
		//Message details
		String receivingUser = "ma",msg = "hello";
		customer.addMessage(receivingUser,msg);
		Message message = customer.getMessages().get(customer.getMessages().size() - 1);
		//Validate message details
		String messageID = "M1";
		String fromUser = "daniel";
		int expectedSize = 1;
		int actualSize = customer.getMessages().size();
		assertEquals(expectedSize,actualSize);
		assertEquals(fromUser,message.getFromUser());
		assertEquals(receivingUser,message.getToUser());
		assertEquals(messageID,message.getMessageId());
		assertEquals(msg,customer.getMessages().get(actualSize - 1).getMessage());
	}

	@Test
	public void getAccounts()
	{
		int expectedSize = 0;
		int actualSize = customer.getAccounts().size();
		assertEquals(expectedSize,actualSize);

		//Account details
		String accountName = "Daniel-1";
		double initBalance = 2000;
		customer.addAccount(accountName,initBalance);

		expectedSize = 1;
		actualSize = customer.getAccounts().size();
		assertEquals(expectedSize,actualSize);
	}

	@Test
	public void setAccounts()
	{
		//Accounts details
		String accountName1 = "Daniel-1",accountNo1 = "A1";
		double initBalance1 = 2000;
		String accountName2 = "Daniel-2",accountNo2 = "A2";
		double initBalance2 = 1500;

		Account account1 = new Account(accountName1,accountNo1,initBalance1);
		Account account2 = new Account(accountName2,accountNo2,initBalance2);
		ArrayList<Account> accounts = new ArrayList<Account>();
		accounts.add(account1);
		accounts.add(account2);

		customer.setAccounts(accounts);

		//Validate
		int expectedSize = 2;
		int actualSize = customer.getAccounts().size();
		assertEquals(expectedSize,actualSize);

		account1 = customer.getAccounts().get(actualSize - 2);
		assertEquals(accountName1,account1.getAccountName());
		assertEquals(accountNo1,account1.getAccountNo());
		assertEquals(initBalance1,account1.getAccountBalance(),0.0001);

		account2 = customer.getAccounts().get(actualSize - 1);
		assertEquals(accountName2,account2.getAccountName());
		assertEquals(accountNo2,account2.getAccountNo());
		assertEquals(initBalance2,account2.getAccountBalance(),0.0001);
	}

//	@Test
//	public void getPayees()
//	{
//	}
//
//	@Test
//	public void setPayees()
//	{
//	}
//
//	@Test
//	public void getMessages()
//	{
//	}
//
//	@Test
//	public void setMessages()
//	{
//	}
}