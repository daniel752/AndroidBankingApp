package com.example.ymdbanking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.ymdbanking.model.Customer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.URIBuilder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

public class UserProfileActivity extends AppCompatActivity
{
	TextView disp_username, disp_email, disp_phone, disp_numAccounts;
	HashMap<String, String> userDetails;
	ImageView profilePic, backBtn;
	Button addAccountBtn;
	Customer customer;
	private static final int PICK_IMAGE = 100;
	Uri imageUri;
	private SessionManager sessionManager;


	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_user_profile);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

		//Hooks
		disp_email = findViewById(R.id.profile_email);
		disp_phone = findViewById(R.id.profile_phone);
		disp_username = findViewById(R.id.profile_user_name);
		disp_numAccounts = findViewById(R.id.disp_accounts_counter);
		addAccountBtn = findViewById(R.id.add_account_btn);
		profilePic = findViewById(R.id.setting_profile_image);
		backBtn = findViewById(R.id.profile_backBtn);

		//User Session
		sessionManager = new SessionManager(UserProfileActivity.this,SessionManager.USER_SESSION);
		userDetails = sessionManager.getUserDetailFromSession();
		customer = sessionManager.getCustomerObjFromSession();

		disp_username.setText(customer.getUsername());
		disp_email.setText(customer.getEmail());
		disp_phone.setText(customer.getPhone());
		disp_numAccounts.setText(sessionManager.userSession.getString("NumAccounts",null));
		getPictureFromDB();

		profilePic.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				openGallery();
			}
		});

		backBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				finish();
			}
		});
	}

	private void getPictureFromDB()
	{
//		StorageReference storageReference = FirebaseStorage.getInstance().getReference("ProfilePic/" + customer.getId());
//		try
//		{
//			Glide.with(UserProfileActivity.this).load(storageReference).into(profilePic);
//		}
//		catch(Error error)
//		{
//			Toast.makeText(UserProfileActivity.this,"Can't load image from DB",Toast.LENGTH_SHORT).show();
//
//		}
		FirebaseStorage.getInstance().getReference("ProfilePic/" + customer.getId()).getDownloadUrl()
			.addOnCompleteListener(new OnCompleteListener<Uri>()
		{
			@Override
			public void onComplete(@NonNull Task<Uri> task)
			{
				Bitmap bitmap = null;
				InputStream inputStream;
				try
				{
					inputStream = new java.net.URL(task.getResult().toString()).openStream();
					bitmap = BitmapFactory.decodeStream(inputStream);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
//				Uri uri = task.getResult();
//				profilePic.setImageURI(uri);
				profilePic.setImageBitmap(bitmap);
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(UserProfileActivity.this,"Can't get profile picture from DB",Toast.LENGTH_SHORT).show();
				Log.d("GET_PROFILE_PIC_ERROR",e.toString());
			}
		});
//		FirebaseDatabase.getInstance().getReference("Users").child(customer.getId()).child("imageUri")
//			.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
//		{
//			@Override
//			public void onComplete(@NonNull Task<DataSnapshot> task)
//			{
//				DataSnapshot ds = task.getResult();
//				imageUri = Uri.parse(ds.getValue(String.class));
//				profilePic.setImageURI(imageUri);
//			}
//		})
	}

	private void openGallery()
	{
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent,PICK_IMAGE);
	}

	@Override
	protected void onActivityResult(int requestCode,int resultCode,Intent data)
	{
		super.onActivityResult(requestCode,resultCode,data);
		if(resultCode == RESULT_OK && requestCode == PICK_IMAGE)
			imageUri = data.getData();

		if(imageUri != null)
		{
			profilePic.setImageURI(imageUri);

			FirebaseStorage.getInstance().getReference().child("ProfilePic/" + customer.getId()).putFile(imageUri)
				.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
			{
				@Override
				public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
				{
					customer.setImageUri(imageUri.toString());
					sessionManager.saveCustomerObjForSession(customer);
					Toast.makeText(UserProfileActivity.this,"New profile picture saved on phone",Toast.LENGTH_SHORT).show();
					FirebaseDatabase.getInstance().getReference("Users").child(customer.getId()).child("imageUri")
						.setValue(imageUri.getPath()).addOnCompleteListener(new OnCompleteListener<Void>()
					{
						@Override
						public void onComplete(@NonNull Task<Void> task)
						{
							Toast.makeText(UserProfileActivity.this,"New profile picture saved on DB",Toast.LENGTH_SHORT).show();
							Toast.makeText(UserProfileActivity.this,"Looks good ;)",Toast.LENGTH_SHORT).show();
						}
					})
					.addOnFailureListener(new OnFailureListener()
					{
						@Override
						public void onFailure(@NonNull Exception e)
						{
							Toast.makeText(UserProfileActivity.this,"Can't save new profile picture",Toast.LENGTH_SHORT).show();
							Log.d("SET PROFILE PIC ERROR",e.toString());
						}
					});
				}
			})
			.addOnFailureListener(new OnFailureListener()
			{
				@Override
				public void onFailure(@NonNull Exception e)
				{
					Toast.makeText(UserProfileActivity.this,"Can't save new profile picture in DB",Toast.LENGTH_SHORT).show();
					Log.d("SET PROFILE PIC ERROR",e.toString());
				}
			});
		}
	}
}