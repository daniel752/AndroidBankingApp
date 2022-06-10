package com.example.ymdbanking;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
;import java.util.Locale;

public class SignUpActivity extends AppCompatActivity
{
	TextView titleText, alreadyHaveAccount;
	TextInputLayout inputName, inputEmail, inputUser, inputPass, inputConfirmPass, inputPhone, inputId, inputCountry;
	Button btnSignUp;
	ImageView backBtn;
	String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+]";
	ProgressDialog progressDialog;

	private FirebaseAuth mAuth;
	private String name, username, email, pass, pass_confirm, phone, id, country;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signup);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//Hooks
		inputName = findViewById(R.id.signup_fullname);
		inputEmail = findViewById(R.id.signup_email);
		inputUser = findViewById(R.id.signup_username);
		inputPass = findViewById(R.id.signup_enter_password);
		inputConfirmPass = findViewById(R.id.signup_confirm_pass);
		inputPhone = findViewById(R.id.signup_phone);
		inputId = findViewById(R.id.signup_id);
		inputCountry = findViewById(R.id.signup_country);
		titleText = findViewById(R.id.signup_title);
		btnSignUp = findViewById(R.id.signup_btn);

		//Firebase
		mAuth = FirebaseAuth.getInstance();

		progressDialog = new ProgressDialog(this);

		btnSignUp.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				setName(inputName.getEditText().getText().toString().trim());
				setUsername(inputUser.getEditText().getText().toString().trim());
				setEmail(inputEmail.getEditText().getText().toString().trim());
				setPass(inputPass.getEditText().getText().toString().trim());
				setPass_confirm(inputConfirmPass.getEditText().getText().toString().trim());
				setPhone(inputPhone.getEditText().getText().toString().trim());
				setId(inputId.getEditText().getText().toString().trim());
				setCountry(inputCountry.getEditText().toString().trim());
				checkCredentials();
			}
		});


	}

	private void checkCredentials()
	{
		String check_pass = "^" +
		                    "(?=.*[0-9])" +         //at least 1 digit
//                "(?=.*[a-z])" +         //at least 1 lower case letter
//                "(?=.*[A-Z])" +         //at least 1 upper case letter
                            "(?=.*[a-zA-Z])" +      //any letter
		                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
		                    "(?=S+$)" +           //no white spaces
		                    ".{6,}" +               //at least 4 characters
		                    "$";
		String chekspaces = "\\A\\w{1,20}\\z";
		String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+]";
		if(name.isEmpty())
			inputName.setError("Field can not be empty!");
		else if(username.isEmpty())
			inputUser.setError("Field can not be empty!");
		else if(username.length() > 20) inputUser.setError("Username is too long!");
		else if(!username.matches(chekspaces)) inputUser.setError("No White spaces are allowed");
		else if(email.isEmpty())
			inputEmail.setError("Field can not be empty!");
//        else if(!email.matches(emailPattern)) inputEmail.setError("Invalid Email!");

		else if(pass.isEmpty() || pass_confirm.isEmpty())
			inputPass.setError("Field can not be empty!");
//        else if(!pass.matches(check_pass))
//            inputPass.setError("Password should contain at least 6 characters");
		else if(!pass_confirm.matches(pass))
		{
			inputPass.setError("Password not matched both fields");
		}

		else
		{

//            progressDialog.setTitle("Singing In");
//            progressDialog.setMessage("Please waite, while checking your credentials");
//            progressDialog.setCanceledOnTouchOutside(false);
//            progressDialog.show();

			mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>()
			{
				@Override
				public void onComplete(@NonNull Task<AuthResult> task)
				{
					if(task.isSuccessful())
					{
						storeNewUserData();
						Toast.makeText(SignUpActivity.this,"Successfully Signed In",Toast.LENGTH_SHORT).show();

					}
					else
						Toast.makeText(SignUpActivity.this,task.getException().toString(),Toast.LENGTH_SHORT).show();

				}
			});
		}
	}


	private void storeNewUserData()
	{
		FirebaseDatabase rootNode = FirebaseDatabase.getInstance();
		DatabaseReference reference = rootNode.getReference("Users");

//        UserHelperClass addNewUser = new UserHelperClass(name,id,username,email,pass,phone);
		User newCustomer = new Customer(email,name,id,pass,phone,username,country);

		reference.child(id).setValue(newCustomer).addOnCompleteListener(new OnCompleteListener<Void>()
		{
			@Override
			public void onComplete(@NonNull Task<Void> task)
			{
				Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});


	}

	//getters and setters

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
	}

	public String getEmail()
	{
		return email;
	}

	public void setEmail(String email)
	{
		this.email = email;
	}

	public String getPass()
	{
		return pass;
	}

	public void setPass(String pass)
	{
		this.pass = pass;
	}

	public String getPass_confirm()
	{
		return pass_confirm;
	}

	public void setPass_confirm(String pass_confirm)
	{
		this.pass_confirm = pass_confirm;
	}

	public String getPhone()
	{
		return phone;
	}

	public void setPhone(String phone)
	{
		this.phone = phone;
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	private void setCountry(String s) {country = s;}

}