package com.example.ymdbanking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.ymdbanking.R;
import com.example.ymdbanking.model.Customer;
import com.example.ymdbanking.model.Message;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message>
{
	private Context context;
	private int resource;

	public MessageAdapter(Context context,int resource,ArrayList<Message> messages)
	{
		super(context,resource,messages);
		this.context = context;
		this.resource = resource;
	}

	@Override
	@NonNull
	public View getView(int position,View convertView,@NonNull ViewGroup parent)
	{
		if (convertView == null)
		{
			convertView = LayoutInflater.from(context).inflate(resource,parent,false);
		}
		Message message = getItem(position);
		TextView txtFromUser = convertView.findViewById(R.id.txt_sending_user);
		txtFromUser.setText(String.format("From: %s",message.getFromUser()));
		TextView txtToUser = convertView.findViewById(R.id.txt_receiving_user);
		txtToUser.setText(String.format("To: %s",message.getToUser()));
//		TextView txtMessage = convertView.findViewById(R.id.edt_message_chat);
//		txtMessage.setText(message.getMessage());
		return convertView;
	}
}
