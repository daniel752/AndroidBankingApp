package com.example.ymdbanking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class ForgotPassActivity extends AppCompatActivity
{

	TextInputLayout inputId;
	Button nextBtn;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_forgot_pass);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//Hooks

		inputId = findViewById(R.id.forgot_passId);
		nextBtn = findViewById(R.id.next_btn);


		nextBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{

				String check_id = inputId.getEditText().getText().toString().trim();

				Query checkUser = FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(check_id);
				checkUser.addListenerForSingleValueEvent(new ValueEventListener()
				{
					@Override
					public void onDataChange(@NonNull DataSnapshot snapshot)
					{

						if(snapshot.exists())
						{
							Intent intent = new Intent(getApplicationContext(),SetNewPasswordActivity.class);
							intent.putExtra("id",check_id);
							startActivity(intent);
						}
						else
							Toast.makeText(ForgotPassActivity.this,"No such users exist!",Toast.LENGTH_SHORT).show();
					}

					@Override
					public void onCancelled(@NonNull DatabaseError error)
					{
						Toast.makeText(ForgotPassActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
					}
				});

			}
		});

	}
}