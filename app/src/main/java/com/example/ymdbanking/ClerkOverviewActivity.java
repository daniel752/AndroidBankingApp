package com.example.ymdbanking;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.ClerkAdapter;
import com.example.ymdbanking.db.ApplicationDB;
import com.example.ymdbanking.model.Admin;
import com.example.ymdbanking.model.Clerk;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;

public class ClerkOverviewActivity extends AppCompatActivity
{
	private FloatingActionButton fab;
	private ListView lstClerks;
	private TextView txtTitle;
	private EditText edtClerkFullName;
	private EditText edtClerkEmail;
	private EditText edtClerkId;
	private EditText edtClerkUsername;
	private EditText edtClerkPassword;
	private EditText edtClerkPasswordConfirm;
	private EditText edtPhone;
	private Button btnCancel;
	private Button btnAddClerk;
	private Dialog clerkDialog;
	private int selectedClerkIndex;
	private Gson gson;
	private Admin admin;
	private SharedPreferences userPreferences;
	private ArrayList<Clerk> clerks;
	private SessionManager sessionManager;

	private boolean displayClerkDialogOnLaunch;


	private View.OnClickListener addClerkClickListener = new View.OnClickListener()
	{
		@Override
		public void onClick(View view)
		{
			startActivity(new Intent(ClerkOverviewActivity.this,AddClerkActivity.class));

//			if (view.getId() == btnCancel.getId())
//			{
//				clerkDialog.dismiss();
//				Toast.makeText(getApplicationContext(), "Clerk Creation Cancelled", Toast.LENGTH_SHORT).show();
//			}
//			else if (view.getId() == btnAddClerk.getId())
//			{
//				startActivity(new Intent(ClerkOverviewActivity.this,AddClerkActivity.class));
//			}
		}
	};

//	private boolean validateClerkInfo()
//	{
//		return !edtClerkFullName.getText().toString().equals(null) &&
//		       !edtClerkEmail.getText().toString().equals(null) &&
//		       !edtClerkId.getText().toString().equals(null) &&
//		       !edtClerkUsername.getText().toString().equals(null) &&
//		       !edtClerkPassword.getText().toString().equals(null) &&
//		       !edtClerkPasswordConfirm.getText().toString().equals(null);
//	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_clerk_overview);
		clerks = new ArrayList<>();
		lstClerks = findViewById(R.id.lst_clerks_clerk_overview);
		txtTitle = findViewById(R.id.txt_title_clerk_overview);
		fab = findViewById(R.id.btn_add_clerk_overview);
		fab.setOnClickListener(addClerkClickListener);
		sessionManager = new SessionManager(getApplicationContext(),SessionManager.USER_SESSION);

		setValues();
//		displayClerkDialogOnLaunch = false;

	}

	private void setValues()
	{
		getAllClerks();
		selectedClerkIndex = 0;
		admin = sessionManager.getAdminObjFromSession();
	}

	private void viewClerk()
	{

	}

	public void getAllClerks()
	{
		FirebaseDatabase.getInstance().getReference("Users").get()
			.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(ds.child("typeID").getValue(int.class) == 2)
						clerks.add(ds.getValue(Clerk.class));

				ClerkAdapter clerkAdapter = new ClerkAdapter(getApplicationContext(),R.layout.lst_profile_row,clerks);
				lstClerks.setAdapter(clerkAdapter);
				lstClerks.setOnItemClickListener(new AdapterView.OnItemClickListener()
				{
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int i, long id)
					{
						selectedClerkIndex = i;
						viewClerk();
					}
				});
			}
		})
			.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(getApplicationContext(), "ERROR - Can't get all clerks from DB", Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
	}
}
