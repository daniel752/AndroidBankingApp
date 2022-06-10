package com.example.ymdbanking;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SendMessageActivity extends AppCompatActivity
{
	private TextView txtTitle;
	private EditText edtMessage;
	private Spinner spnSelectUser;
	private Button btnSendMessage;
	private Button btnCancelMessage;
	private SessionManager sessionManager;
	private Customer customer;
	private ArrayList<Customer> usersForChat;
	private ArrayAdapter<Customer> customerAdapter;
	private int selectedUserIndex;
	private static ArrayList<Message> messages;

	private View.OnClickListener MessageClickListener = new View.OnClickListener()
	{
		@RequiresApi(api = Build.VERSION_CODES.N)
		@Override
		public void onClick(View v)
		{
			if(v.getId() == R.id.btn_send_message_chat)
			{
				sendMessage();
				setValues();
			}
			else if(v.getId() == R.id.btn_cancel_message_chat)
			{
				Toast.makeText(SendMessageActivity.this,"Chat action cancelled",Toast.LENGTH_SHORT).show();
				startActivity(new Intent(SendMessageActivity.this,ShowMessagesActivity.class));
			}
		}
	};

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		txtTitle = findViewById(R.id.txt_title_chat);
		edtMessage = findViewById(R.id.edt_message_chat);
		spnSelectUser = findViewById(R.id.spn_select_user_chat);
		btnSendMessage = findViewById(R.id.btn_send_message_chat);
		btnCancelMessage = findViewById(R.id.btn_cancel_message_chat);

		sessionManager = new SessionManager(SendMessageActivity.this,SessionManager.USER_SESSION);
		customer = sessionManager.getCustomerObjFromSession();

		btnSendMessage.setOnClickListener(MessageClickListener);
		btnCancelMessage.setOnClickListener(MessageClickListener);

		usersForChat = new ArrayList<>();

		setValues();
	}

	private void setValues()
	{
		//Getting all customers besides the current one (receiving customers)
		selectedUserIndex = 0;
		FirebaseDatabase.getInstance().getReference("Users").get()
			.addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				for(DataSnapshot ds : task.getResult().getChildren())
					if(!ds.getKey().equals(customer.getId()) && ds.child("typeID").getValue(int.class) == 3)
						usersForChat.add(ds.getValue(Customer.class));

				//Setting adapter for customers list after pulling data from DB
				customerAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_item,usersForChat);
				customerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				spnSelectUser.setAdapter(customerAdapter);
				spnSelectUser.setSelection(0);
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(getApplicationContext(),"ERROR - Can't get receiving customers from DB",Toast.LENGTH_SHORT).show();
				Log.d("DB_ERROR",e.toString());
			}
		});
	}

//	@RequiresApi(api = Build.VERSION_CODES.N)
	private void sendMessage()
	{
		selectedUserIndex = spnSelectUser.getSelectedItemPosition();
//		setCustomerMessages();
		customer = sessionManager.getCustomerObjFromSession();
		customer.setMessages(messages);
//		customer.setMessages(sessionManager.getMessagesFromSession());
		customer.addMessage(customer.getUsername(),edtMessage.getText().toString());
		int msgSize = customer.getMessages().size();

//		sessionManager.editor.putInt("NumMessages",msgSize);
		Message message = new Message(customer.getUsername(),usersForChat.get(selectedUserIndex - 1).getUsername(),
				edtMessage.getText().toString(),customer.getMessages().get(selectedUserIndex - 1).getMessageId());

		//Writing message at sending user's 'Chats' under 'To'
		FirebaseDatabase.getInstance().getReference("Chats").child(customer.getId()).child("To").child(usersForChat.get(selectedUserIndex).getId())
			.child(customer.getMessages().get(msgSize - 1).getMessageId()).setValue(message).addOnCompleteListener(new OnCompleteListener<Void>()
		{
			@Override
			public void onComplete(@NonNull Task<Void> task)
			{
				//Writing message at receiving user's 'Chats' under 'From'
				FirebaseDatabase.getInstance().getReference("Chats").child(usersForChat.get(selectedUserIndex).getId())
						.child("From").child(customer.getId()).child(customer.getMessages().get(msgSize - 1).getMessageId()).setValue(message)
					.addOnCompleteListener(new OnCompleteListener<Void>()
				{
					@Override
					public void onComplete(@NonNull Task<Void> task)
					{
						Toast.makeText(SendMessageActivity.this,
								"Message has been sent to " + usersForChat.get(selectedUserIndex).getUsername(),Toast.LENGTH_SHORT).show();
						startActivity(new Intent(SendMessageActivity.this,ShowMessagesActivity.class));
					}
				});
			}
		});
	}

	@Override
	public void onBackPressed()
	{
		startActivity(new Intent(SendMessageActivity.this,ShowMessagesActivity.class));
	}

	//	@RequiresApi(api = Build.VERSION_CODES.N)
//	private void setCustomerMessages()
//	{
//		sessionManager.getMessagesFromSession().forEach(msg ->
//		    customer.getMessages().add(
//		            new Message(msg.getFromUser(),
//		                        msg.getToUser(),
//		                        msg.getMessage(),
//		                        msg.getMessageId())));
//	}

	public static ArrayList<Message> getMessages() {return messages;}
	public static void setMessages(ArrayList<Message> messages) {SendMessageActivity.messages = messages;}
}
