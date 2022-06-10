package com.example.ymdbanking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ymdbanking.adapters.ProfileAdapter;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ShowUsersActivity extends AppCompatActivity
{

	private ListView usersList;
	private int selectedCustomerIndex;
	private Admin admin;
	private Clerk clerk;
	private ArrayList<Customer> customers;
	private SessionManager sessionManager;

	//View user dialog
	private Dialog dlgViewUser;
	private TextView txtFullName;
	private TextView txtEmail;
	private TextView txtId;
	private TextView txtUsername;

	private Dialog dlgClerkToUser;
	private Spinner spnSelectClerk;
	private Button btnSuccess;
	private Button btnCancel;
	private ArrayAdapter<Clerk> clerkAdapter;
	private ArrayList<Clerk> clerks;
	private ImageView imgCancelButton;
	private String sessionID;


	private View.OnClickListener clerkToUserClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == imgCancelButton.getId())
			{
				dlgClerkToUser.dismiss();
				Toast.makeText(ShowUsersActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnSuccess.getId())
			{
				addClerkToUser();
			}
		}
	};

	private View.OnClickListener viewUserClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == btnCancel.getId())
			{
				dlgViewUser.dismiss();
				Toast.makeText(ShowUsersActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
			}
			else if(view.getId() == btnSuccess.getId())
			{
				deleteUser();
				startActivity(new Intent(ShowUsersActivity.this,ShowUsersActivity.class));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_users);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		usersList = findViewById(R.id.lst_users);
		selectedCustomerIndex = 0;
		sessionManager = new SessionManager(ShowUsersActivity.this,SessionManager.USER_SESSION);
//		sessionID = sessionManager.userSession.getString(SessionManager.KEY_TYPE_ID,null);
		if(LoginActivity.getUserTypeID() == 1)
			admin = sessionManager.getAdminObjFromSession();
		else if(LoginActivity.getUserTypeID() == 2)
			clerk = sessionManager.getClerkObjFromSession();

		setValues();
	}

	public void setValues()
	{
		ArrayList<Customer> tempCustomers = new ArrayList<>();
		if(LoginActivity.getUserTypeID() == 2)
			tempCustomers = getClerkCustomers();
		customers = new ArrayList<>();
		ArrayList<Customer> finalTempCustomers = tempCustomers;
		FirebaseDatabase.getInstance().getReference("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 3)
					{
						boolean exists = false;
						//Iterating through clerk's customers
						for(Customer customer : finalTempCustomers)
							//Checking if customer is already in clerk's customer list
							if(customer.getId().equals(ds.getValue(Customer.class).getId()))
								exists = true;

						if(!exists)
							//If customer isn't in the list we can add him to clerk's customer list
							customers.add(ds.getValue(Customer.class));
					}

				ProfileAdapter adapter = new ProfileAdapter(ShowUsersActivity.this,R.layout.lst_profile_row,customers);
				usersList.setAdapter(adapter);

				usersList.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> adapterView,View view,int i,long l)
					{

						selectedCustomerIndex = i;
						//If current user is admin
						if(LoginActivity.getUserTypeID() == 1)
							setDialogViewUser();
							//If the user is clerk
						else if(LoginActivity.getUserTypeID() == 2)
							displayClerkToUserDialog();

					}
				});
			}
		});
	}

	public void setDialogViewUser()
	{
		dlgViewUser = new Dialog(ShowUsersActivity.this);
		dlgViewUser.setContentView(R.layout.view_user_dialog);
		dlgViewUser.setCanceledOnTouchOutside(true);
		dlgViewUser.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(getApplicationContext(),"Cancelled view user dialog",Toast.LENGTH_SHORT).show();
			}
		});

		txtFullName = dlgViewUser.findViewById(R.id.txt_fullname_view_user_dialog);
		txtEmail = dlgViewUser.findViewById(R.id.txt_email_view_user_dialog);
		txtId = dlgViewUser.findViewById(R.id.txt_id_view_user_dialog);
		txtUsername = dlgViewUser.findViewById(R.id.txt_username_view_user_dialog);
		btnSuccess = dlgViewUser.findViewById(R.id.btn_delete_view_user_dialog);
		btnCancel = dlgViewUser.findViewById(R.id.btn_cancel_view_user_dialog);

		txtFullName.setText(customers.get(selectedCustomerIndex).getFullName());
		txtEmail.setText(customers.get(selectedCustomerIndex).getEmail());
		txtId.setText(customers.get(selectedCustomerIndex).getId());
		txtUsername.setText(customers.get(selectedCustomerIndex).getUsername());

		btnSuccess.setOnClickListener(viewUserClickListener);
		btnCancel.setOnClickListener(viewUserClickListener);

		dlgViewUser.show();
	}

	public ArrayList<Customer> getClerkCustomers()
	{
		ArrayList<Customer> customers = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("ClerkCustomers").child(clerk.getId())
				.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					customers.add(ds.getValue(Customer.class));
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{

					}
				});
		return customers;
	}

	public void displayClerkToUserDialog()
	{
		dlgClerkToUser = new Dialog(ShowUsersActivity.this);
		dlgClerkToUser.setContentView(R.layout.add_clerk_to_user_dialog);
		dlgClerkToUser.setCanceledOnTouchOutside(true);

		dlgClerkToUser.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(ShowUsersActivity.this,"Adding Clerk has been cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		imgCancelButton = dlgClerkToUser.findViewById(R.id.clerkToUser_cancelBtn);
		btnSuccess = dlgClerkToUser.findViewById(R.id.add_clerkUser_btn);
		spnSelectClerk = dlgClerkToUser.findViewById(R.id.spn_select_clerk);

		clerks = new ArrayList<>();
		FirebaseDatabase.getInstance().getReference("Users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 2)
						clerks.add(ds.getValue(Clerk.class));
			}
		});
		clerkAdapter = new ArrayAdapter<>(this,android.R.layout.simple_spinner_item,clerks);
		clerkAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spnSelectClerk.setAdapter(clerkAdapter);

		imgCancelButton.setOnClickListener(clerkToUserClickListener);
		btnSuccess.setOnClickListener(clerkToUserClickListener);

		dlgClerkToUser.show();
	}

	public void addClerkToUser()
	{
		clerk.assignCustomerToClerk(((Customer) usersList.getAdapter().getItem(selectedCustomerIndex)),getApplicationContext());
		Toast.makeText(getApplicationContext(),"User has been assigned to you",Toast.LENGTH_SHORT).show();
		dlgClerkToUser.dismiss();
		setValues();
	}

	public void deleteUser()
	{
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        //Deletes user's account from firebase authentication
//        if(user != null)
//        {
//            user.delete().addOnCompleteListener(new OnCompleteListener<Void>()
//            {
//                @Override
//                public void onComplete(@NonNull Task<Void> task)
//                {
		//Deletes user's accounts from Accounts collection
		FirebaseDatabase.getInstance().getReference("Accounts")
				.child(customers.get(selectedCustomerIndex).getId()).removeValue();

		//Deletes user from Users collection
		FirebaseDatabase.getInstance().getReference("Users").child(customers.get(selectedCustomerIndex).getId())
				.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
		{
			@Override
			public void onComplete(@NonNull Task<Void> task)
			{
				//Deletes user's data from rest of collections
				FirebaseDatabase.getInstance().getReference().get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						//Iterating through collection to find user's data
						for(DataSnapshot ds : task.getResult().getChildren())
						{
							//If we're on Users or Accounts collection than skip a loop
							if(ds.getKey().equals("Users") ||
							   ds.getKey().equals("Accounts"))
								continue;

							//Iterating through collection's children to see if we have a child with user's id
							for(DataSnapshot dsa : ds.getChildren())
							{
								if(dsa.child(customers.get(selectedCustomerIndex).getId()).exists())
								{
									//If user's id exists than we'll remove it from the collection
									dsa.child(customers.get(selectedCustomerIndex).getId()).getRef().removeValue();
								}
							}
						}
					}
				})
						.addOnFailureListener(new OnFailureListener()
						{
							@Override
							public void onFailure(@NonNull Exception e)
							{
								Toast.makeText(getApplicationContext(),"Can't delete user from DB",Toast.LENGTH_SHORT).show();
								Log.d("DB_REMOVE_USER_ERROR",e.toString());
							}
						});

				Toast.makeText(getApplicationContext(),
						"User - " + customers.get(selectedCustomerIndex).getUsername()
						+ " ID - " + customers.get(selectedCustomerIndex).getId() +
						" has been deleted from DB",Toast.LENGTH_SHORT).show();
			}
		})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(getApplicationContext(),"Can't delete user from DB",Toast.LENGTH_SHORT).show();
						Log.d("DB_REMOVE_USER_ERROR",e.toString());
					}
				});
	}
//            })
//            .addOnFailureListener(new OnFailureListener()
//            {
//                @Override
//                public void onFailure(@NonNull Exception e)
//                {
//                    Toast.makeText(getApplicationContext(),"Can't delete user from DB",Toast.LENGTH_SHORT).show();
//                    Log.d("DB_REMOVE_USER_ERROR",e.toString());
//                }
//            });
//        }
//        else
//        {
//            Toast.makeText(getApplicationContext(),"Can't delete user",Toast.LENGTH_SHORT).show();
//        }
//    }

}