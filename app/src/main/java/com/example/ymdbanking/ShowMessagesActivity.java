package com.example.ymdbanking;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ymdbanking.adapters.MessageAdapter;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Message;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class ShowMessagesActivity extends AppCompatActivity
{
	private TextView txtTitle;
	private ListView lstMessages;
	private ArrayList<Message> messages;
	private ArrayAdapter<Message> messageAdapter;
	private Customer customer;
	private SessionManager sessionManager;
	private int selectedMessageIndex;
	private Button btnSendMessage;

	//Show message dialog
	private Dialog dlgShowMessage;
	private TextView txtTitleShowMessage;
	private TextView txtMessageContent;
	private Button btnDismissMessage;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_show_messages);

		txtTitle = findViewById(R.id.txt_title_show_messages);
		lstMessages = findViewById(R.id.lst_show_messages);
		btnSendMessage = findViewById(R.id.btn_send_message);

		sessionManager = new SessionManager(ShowMessagesActivity.this,SessionManager.USER_SESSION);
		customer = sessionManager.getCustomerObjFromSession();

		setValues();
	}

	private void setValues()
	{
		messages = new ArrayList<>();
		getMessages();
		lstMessages.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent,View view,int position,long id)
			{
				selectedMessageIndex = position;
				showMessageDialog();
			}
		});

		btnSendMessage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(ShowMessagesActivity.this,SendMessageActivity.class));
			}
		});
	}

	private void showMessageDialog()
	{
		dlgShowMessage = new Dialog(ShowMessagesActivity.this);
		dlgShowMessage.setContentView(R.layout.show_message_dialog);
		dlgShowMessage.setCanceledOnTouchOutside(true);
		dlgShowMessage.setOnCancelListener(new DialogInterface.OnCancelListener()
		{
			@Override
			public void onCancel(DialogInterface dialog)
			{
				Toast.makeText(ShowMessagesActivity.this,"Show message cancelled",Toast.LENGTH_SHORT).show();
			}
		});

		txtTitleShowMessage = dlgShowMessage.findViewById(R.id.txt_title_show_message);
		txtMessageContent = dlgShowMessage.findViewById(R.id.txt_content_show_message);
		btnDismissMessage = dlgShowMessage.findViewById(R.id.btn_dismiss_show_message);

		btnDismissMessage.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dlgShowMessage.dismiss();
			}
		});

		txtTitleShowMessage.setText(String.format("Message From %s",messages.get(selectedMessageIndex).getFromUser()));
		txtMessageContent.setText(messages.get(selectedMessageIndex).getMessage());

		dlgShowMessage.show();
	}

	private void getMessages()
	{
		FirebaseDatabase.getInstance().getReference("Chats").child(customer.getId()).child("To")
			.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
		{
			@Override
			public void onComplete(@NonNull Task<DataSnapshot> task)
			{
				HashMap<String,Message> messageHM = new HashMap<>();
				for(DataSnapshot ds : task.getResult().getChildren())
					for(DataSnapshot dsa : ds.getChildren())
						messageHM.put(dsa.getKey(),dsa.getValue(Message.class));

				FirebaseDatabase.getInstance().getReference("Chats").child(customer.getId()).child("From")
					.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>()
				{
					@Override
					public void onComplete(@NonNull Task<DataSnapshot> task)
					{
						for(DataSnapshot ds : task.getResult().getChildren())
							for(DataSnapshot dsa : ds.getChildren())
								messageHM.put(dsa.getKey(),dsa.getValue(Message.class));

						messages.addAll(messageHM.values());
						messageAdapter = new MessageAdapter(ShowMessagesActivity.this,R.layout.lst_message_row,messages);
						lstMessages.setAdapter(messageAdapter);
						SendMessageActivity.setMessages(messages);
//						sessionManager.saveMessagesForSession(messages);
//						sessionManager.saveCustomerObjForSession(customer);
					}
				})
				.addOnFailureListener(new OnFailureListener()
				{
					@Override
					public void onFailure(@NonNull Exception e)
					{
						Toast.makeText(ShowMessagesActivity.this,"Can't get from messages",Toast.LENGTH_SHORT).show();
						Log.d("CHAT FROM MSG ERROR",e.toString());
					}
				});
			}
		})
		.addOnFailureListener(new OnFailureListener()
		{
			@Override
			public void onFailure(@NonNull Exception e)
			{
				Toast.makeText(ShowMessagesActivity.this,"Can't get to messages",Toast.LENGTH_SHORT).show();
				Log.d("CHAT TO MSG ERROR",e.toString());
			}
		});
	}

	@Override
	public void onBackPressed()
	{
		startActivity(new Intent(ShowMessagesActivity.this,DashboardActivity.class));
	}
}
