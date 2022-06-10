package com.example.ymdbanking;

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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.CustomerAdapter;
import com.example.ymdbanking.db.ApplicationDB;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.example.ymdbanking.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class CustomerOverviewActivity extends AppCompatActivity
{
	private ListView lstProfiles;
	private int selectedCustomerIndex;
	private Clerk clerk;
	private Customer customer;
	private ArrayAdapter<Customer> customerAdapter;
	private Dialog customerDialog;
	private Dialog accountDialog;
	private SessionManager sessionManager;
	private EditText edtAccountName;
	private EditText edtAccountInitAmount;


	public View.OnClickListener customerDialogClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == R.id.btn_success_customer_dialog)
			{
				displayAccountDialog();
			}
			else if(view.getId() == R.id.btn_show_accounts_customer_dialog)
			{
				startActivity(new Intent(CustomerOverviewActivity.this,AccountsOverViewActivity.class));
			}
			else if(view.getId() == R.id.btn_cancel_customer_dialog)
			{
				customerDialog.dismiss();
			}
		}
	};

	public View.OnClickListener accountDialogClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			if(view.getId() == R.id.btn_success_account_dialog)
			{
				openAccount();
			}
			else if(view.getId() == R.id.btn_cancel_account_dialog)
			{
				accountDialog.dismiss();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customers_overview);
		ArrayList<Customer> customers = new ArrayList<>();
		TextView txtTitle = findViewById(R.id.txt_profile_fragment_title);
		lstProfiles = findViewById(R.id.lst_profiles_overview);
		setValues();
	}

	private void displayCustomerDialog(int index)
	{
		customerDialog = new Dialog(this);
		customerDialog.setContentView(R.layout.customer_dialog);
		customerDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		customerDialog.setCanceledOnTouchOutside(true);
		customerDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(getApplicationContext(), "Customer's info closed", Toast.LENGTH_SHORT).show();
			}
		});
		TextView txtCustomerEmail = customerDialog.findViewById(R.id.txt_email_customer_dialog);
		TextView txtCustomerFullName = customerDialog.findViewById(R.id.txt_fullname_customer_dialog);
		TextView txtCustomerId = customerDialog.findViewById(R.id.txt_id_customer_dialog);
		TextView txtCustomerUsername = customerDialog.findViewById(R.id.txt_username_customer_assign_dialog);
		//txtCustomerAccountNum = customerDialog.findViewById(R.id.txt_accounts_num_customer_dialog);
		txtCustomerEmail.setText(customerAdapter.getItem(index).getEmail());
		txtCustomerFullName.setText(customerAdapter.getItem(index).getFullName());
		txtCustomerId.setText(customerAdapter.getItem(index).getId());
		txtCustomerUsername.setText(customerAdapter.getItem(index).getUsername());
		//txtCustomerAccountNum.setText(customerAdapter.getItem(index).getNumberOfAccounts());
		Button btnAssignCustomer = customerDialog.findViewById(R.id.btn_success_customer_dialog);
		Button btnShowAccounts = customerDialog.findViewById(R.id.btn_show_accounts_customer_dialog);
		Button btnCancelAssign = customerDialog.findViewById(R.id.btn_cancel_customer_dialog);
		btnAssignCustomer.setOnClickListener(customerDialogClickListener);
		btnShowAccounts.setOnClickListener(customerDialogClickListener);
		btnCancelAssign.setOnClickListener(customerDialogClickListener);
		customerDialog.show();
	}

	private void setValues()
	{
		selectedCustomerIndex = 0;
		sessionManager = new SessionManager(getApplicationContext(),SessionManager.USER_SESSION);
		clerk = sessionManager.getClerkObjFromSession();
		getClerkCustomers();
	}

	private void displayAccountDialog()
	{
		accountDialog = new Dialog(this);
		accountDialog.setContentView(R.layout.account_dialog);
		accountDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		accountDialog.setCanceledOnTouchOutside(true);
		accountDialog.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialogInterface)
			{
				Toast.makeText(getApplicationContext(), "Account creation canceled", Toast.LENGTH_SHORT).show();
			}
		});
		TextView txtTitleAccount = accountDialog.findViewById(R.id.txt_title_account_dialog);
		edtAccountName = accountDialog.findViewById(R.id.edt_name_account_dialog);
		edtAccountInitAmount = accountDialog.findViewById(R.id.edt_init_bal_account_dialog);
		Button btnSuccess = accountDialog.findViewById(R.id.btn_success_account_dialog);
		Button btnCancel = accountDialog.findViewById(R.id.btn_cancel_account_dialog);
		btnSuccess.setOnClickListener(accountDialogClickListener);
		btnCancel.setOnClickListener(accountDialogClickListener);
		accountDialog.show();
	}

	private void openAccount()
	{
		String balance = edtAccountInitAmount.getText().toString();
		boolean isNum = false;
		double initDepositAmount = 0;

		if (!(edtAccountName.getText().toString().equals("")))
		{
			try
			{
				initDepositAmount = Double.parseDouble(edtAccountInitAmount.getText().toString());
				isNum = true;
			}
			catch (Exception e)
			{
				if (!edtAccountInitAmount.getText().toString().equals(""))
				{
					Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
					edtAccountInitAmount.getText().clear();
				}
			}

			if (edtAccountName.getText().toString().length() > 10)
			{
				Toast.makeText(this, R.string.account_name_exceeds_char, Toast.LENGTH_SHORT).show();
				edtAccountName.getText().clear();

			}
			else if ((isNum) || balance.equals(""))
			{
				boolean match = false;

				for (int i = 0; i < customer.getAccounts().size(); i++)
				{
					if (edtAccountName.getText().toString().equalsIgnoreCase(customer.getAccounts().get(i).getAccountName())) {
						match = true;
					}
				}

				if (!match)
				{
					ApplicationDB applicationDb = new ApplicationDB(getApplicationContext());
					customer.addAccount(edtAccountName.getText().toString(), initDepositAmount);

					if (!balance.equals(""))
					{
						if (isNum)
						{
							if (initDepositAmount >= 0.01)
							{
								applicationDb.saveNewAccount(customer, customer.getAccounts().get(customer.getAccounts().size() - 1));
							}
						}
					}

					Toast.makeText(this, R.string.acc_saved_successfully, Toast.LENGTH_SHORT).show();

					SessionManager sessionManager = new SessionManager(this,SessionManager.USER_SESSION);
					Gson gson = new Gson();
					String json = gson.toJson(customer);
					sessionManager.editor.putString(SessionManager.SESSION_OBJ,json);

					accountDialog.dismiss();

				}
				else
				{
					Toast.makeText(this, R.string.account_name_error, Toast.LENGTH_SHORT).show();
					edtAccountName.getText().clear();
				}
			}
		}
		else
		{
			Toast.makeText(this, "Please enter an account name", Toast.LENGTH_SHORT).show();
		}
	}

	private ArrayList<Customer> getClerkCustomers()
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

				customerAdapter = new CustomerAdapter(getApplicationContext(),R.layout.lst_profile_row,customers);
				lstProfiles.setAdapter(customerAdapter);
				lstProfiles.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int i, long id)
					{
						selectedCustomerIndex = i;
						customer = customerAdapter.getItem(selectedCustomerIndex);

						ApplicationDB applicationDB = new ApplicationDB(getApplicationContext());
						customer.setAccounts(applicationDB.getAccountsFromCurrentCustomer(customer.getId()));
						sessionManager.saveCustomerObjForSession(customer);
						sessionManager.saveAccountsObjForSession(customer.getAccounts());
						displayCustomerDialog(i);
					}
				});
			}
		})
			.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(getApplicationContext(),"Can't get " + clerk.getFullName() + "'s customers list from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
		return customers;
	}
}
