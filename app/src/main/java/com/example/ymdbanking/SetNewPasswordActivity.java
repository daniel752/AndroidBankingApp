package com.example.ymdbanking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetNewPasswordActivity extends AppCompatActivity
{

	TextInputLayout pass_new, confirm_new_pass;
	Button saveBtn;
	String ID;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_new_password);

		pass_new = findViewById(R.id.input_new_pass);
		confirm_new_pass = findViewById(R.id.input_confirm_new_pass);
		saveBtn = findViewById(R.id.save_btn);

		ID = getIntent().getStringExtra("id");
		saveBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				String pass = pass_new.getEditText().getText().toString().trim();
				String confirm_pass = confirm_new_pass.getEditText().getText().toString().trim();

				if(pass.equals(confirm_pass))
				{
					DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
					reference.child(ID).child("password").setValue(pass);
					Toast.makeText(SetNewPasswordActivity.this,"Password changed successfully",Toast.LENGTH_SHORT).show();
					startActivity(new Intent(getApplicationContext(),LoginActivity.class));
				}
				else
				{
					pass_new.setError("");
				}

			}
		});


	}
}