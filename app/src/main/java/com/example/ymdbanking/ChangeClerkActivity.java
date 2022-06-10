package com.example.ymdbanking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.ClerkAdapter;
import com.example.ymdbanking.adapters.CustomerAdapter;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ChangeClerkActivity extends AppCompatActivity
{
	private ListView lstUsers;
	private Dialog dlgChangeClerk;
	private TextView txtClerkName;
	private Customer customer;
	private Clerk customerClerk;
	private Clerk selectedClerk;
	private ArrayList<Customer> customers;
	private ArrayAdapter<Customer> customerAdapter;
	private ArrayList<Clerk> clerks;
	private int selectedUserIndex;
	private int selectedClerkIndex;
	boolean flag;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_clerk);
		lstUsers = findViewById(R.id.lst_select_user_change_clerk);
		customers = new ArrayList<>();
		clerks = new ArrayList<>();

		getCustomers();
	}

	private void getCustomers()
	{
		FirebaseDatabase.getInstance().getReference("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 3)
						customers.add(ds.getValue(Customer.class));

				customerAdapter = new CustomerAdapter(ChangeClerkActivity.this,R.layout.lst_profile_row,customers);
				lstUsers.setAdapter(customerAdapter);

				lstUsers.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent,View view,int position,long id)
					{
						selectedUserIndex = position;
						customer = customers.get(selectedUserIndex);
						getClerks();
						checkIfHasClerk(true);
					}
				});
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(ChangeClerkActivity.this,"Can't get customers from DB",Toast.LENGTH_SHORT).show();
				Log.d("GET_CUSTOMERS_ERROR",e.toString());
			}
		});
	}

	private void getClerks()
	{
		clerks = new ArrayList<>(0);
		FirebaseDatabase.getInstance().getReference("Users")
			.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 2)
						clerks.add(ds.getValue(Clerk.class));

//				showChangeClerkDialog();
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(getApplicationContext(),"ERROR - Can't get all clerks from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
	}

	private void showChangeClerkDialog()
	{
//		dlgChangeClerk = new Dialog(ChangeClerkActivity.this);
//		dlgChangeClerk.setContentView(R.layout.change_clerk_dialog);
		dlgChangeClerk.setCanceledOnTouchOutside(true);
		dlgChangeClerk.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(ChangeClerkActivity.this,"Change clerk view cancelled",Toast.LENGTH_SHORT).show();
			}
		});

//		txtClerkName = dlgChangeClerk.findViewById(R.id.txt_clerk_name);
//		dlgChangeClerk = new Dialog(ChangeClerkActivity.this);
//		dlgChangeClerk.setContentView(R.layout.change_clerk_dialog);

		ArrayAdapter<Clerk> clerkAdapter = new ClerkAdapter(ChangeClerkActivity.this,R.layout.lst_profile_row,clerks);
		ListView lstClerks = dlgChangeClerk.findViewById(R.id.lst_change_clerk);
		lstClerks.setAdapter(clerkAdapter);

		lstClerks.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				selectedClerkIndex = position;
				selectedClerk = clerks.get(selectedClerkIndex);
				checkIfHasClerk(false);
				changeClerk(customerClerk,selectedClerk);
			}
		});

		dlgChangeClerk.show();
	}

	private void checkIfHasClerk(boolean fl)
	{
		this.flag = fl;
		FirebaseDatabase.getInstance().getReference("ClerkCustomers")
			.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child(customer.getId()).exists())
					{
						for(Clerk clerk : clerks)
						{
							if(clerk.getId().equals(ds.getKey()))
							{
								customerClerk = clerk;
								dlgChangeClerk = new Dialog(ChangeClerkActivity.this);
								dlgChangeClerk.setContentView(R.layout.change_clerk_dialog);
								txtClerkName = dlgChangeClerk.findViewById(R.id.txt_clerk_name);
								txtClerkName.setText(customerClerk.getFullName());
//								changeClerk(customerClerk,selectedClerk);
							}
						}
						break;
					}
				if(flag)
					showChangeClerkDialog();
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(ChangeClerkActivity.this,"Can't check if user has clerk assigned to him",Toast.LENGTH_SHORT).show();
				Log.d("CHECK_HAS_CLERK_ERROR",e.toString());
			}
		});
	}

	private void changeClerk(Clerk oldClerk,Clerk newClerk)
	{
		FirebaseDatabase.getInstance().getReference("ClerkCustomers")
			.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
				{
					if(ds.getKey().equals(oldClerk.getId()) && ds.child(customer.getId()).exists())
					{
						HashMap<String,String> customerHashMap = new HashMap<>();
						customerHashMap.put("email",customer.getEmail());
						customerHashMap.put("fullName",customer.getFullName());
						customerHashMap.put("id",customer.getId());
						customerHashMap.put("password",customer.getPassword());
						customerHashMap.put("phone",customer.getPhone());
						customerHashMap.put("typeID",String.valueOf(customer.getTypeID()));
						customerHashMap.put("username",customer.getUsername());
						ds.child(customer.getId()).getRef().removeValue();
						ds.getRef().getRoot().child("ClerkCustomers").child(newClerk.getId()).child(customer.getId()).setValue(customerHashMap);

						dlgChangeClerk.dismiss();
						Toast.makeText(ChangeClerkActivity.this,"Your clerk has been changed",Toast.LENGTH_SHORT).show();
						startActivity(new Intent(ChangeClerkActivity.this,ChangeClerkActivity.class));
					}
				}
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(ChangeClerkActivity.this,"Can't change clerks",Toast.LENGTH_SHORT).show();
				Log.d("CHANGE CLERK ERROR",e.toString());
			}
		});
	}
}
