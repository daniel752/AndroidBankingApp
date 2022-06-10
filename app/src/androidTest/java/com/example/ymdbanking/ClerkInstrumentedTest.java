package com.example.ymdbanking;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;

import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;

import org.junit.Before;
import org.junit.Test;

public class ClerkInstrumentedTest
{
	Clerk clerk;
	String email,fullName,id,password,phone,username;
	Customer customer;
	String custEmail,custFullName,custId,custPassword,custPhone,custUsername,custCountry;
	Context context;

	@Before
	public void setUp()
	{
		//SetUp for clerk assign test
		context = InstrumentationRegistry.getInstrumentation().getContext();
		email = "adir@gmail.com";
		fullName = "Adir Shaish";
		id = "12121212";
		password = "123456";
		phone = "123456789";
		username = "adir";
		clerk = new Clerk(email,fullName,id,password,phone,username);
	}

	@Test
	public void integrationTestAssignCustomerToClerk()
	{
		Customer customer = new Customer();
		customer.setEmail("daniel@gmail.com");
		customer.setFullName("Daniel Arbiv");
		customer.setId("123456789");
		customer.setPassword("123456");
		customer.setPhone("12345678");
		customer.setUsername("daniel");
		customer.setCountry("USA");

		clerk.assignCustomerToClerk(customer,context);
		customer = clerk.getCustomers().get(clerk.getCustomers().size() - 1);
		//Validate email
		assertEquals("daniel@gmail.com",customer.getEmail());
		//Validate fullName
		assertEquals("Daniel Arbiv",customer.getFullName());
		//Validate id
		assertEquals("123456789",customer.getId());
		//Validate password
		assertEquals("123456",customer.getPassword());
		//Validate phone
		assertEquals("12345678",customer.getPhone());
		//Validate username
		assertEquals("daniel",customer.getUsername());
		//Validate country
		assertEquals("USA",customer.getCountry());
	}
}
