package com.example.ymdbanking.model;

import java.util.ArrayList;

/**
 * Class for Admin user
 */
public class Admin extends User
{
	private ArrayList<Customer> users;
	private final static int typeID = 1;

	public Admin()
	{
		super();
		//Empty constructor
	}

	public Admin(String email,String fullName,String id,String password,String phone,String username)
	{
		super(email,fullName,id,password,phone,username,typeID);
	}

	public ArrayList<Customer> getUsers() {return users;}
	public void setUsers(ArrayList<Customer> users) {this.users = users;}
}
