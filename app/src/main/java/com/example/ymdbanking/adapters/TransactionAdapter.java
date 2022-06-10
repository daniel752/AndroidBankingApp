package com.example.ymdbanking.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import androidx.annotation.NonNull;

import com.example.ymdbanking.R;
import com.example.ymdbanking.model.Transaction;

import java.util.ArrayList;

public class TransactionAdapter extends ArrayAdapter<Transaction>
{
	private Context context;
	private int resource;

	public TransactionAdapter(Context context,int resource,ArrayList<Transaction> transactions)
	{
		super(context,resource,transactions);

		this.context = context;
		this.resource = resource;
	}

	@Override
	@NonNull
	public View getView(int position,View convertView,@NonNull ViewGroup parent)
	{
		if(convertView == null)
		{

			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
			convertView = inflater.inflate(resource,parent,false);
		}

		Transaction transaction = getItem(position);

		ImageView imgTransactionIcon = convertView.findViewById(R.id.img_transaction);
		TextView txtTransactionTitle = convertView.findViewById(R.id.txt_transaction_type_id);
		TextView txtTransactionTimestamp = convertView.findViewById(R.id.txt_transaction_timestamp);
		TextView txtTransactionInfo = convertView.findViewById(R.id.txt_transaction_info);
		txtTransactionInfo.setVisibility(View.VISIBLE);
		TextView txtTransactionAmount = convertView.findViewById(R.id.txt_transaction_amount);
		TextView txtTransactionStatus = convertView.findViewById(R.id.txt_transaction_status);

		txtTransactionTitle.setText(transaction.getTransactionType().toString() + " - " +
		                            transaction.getTransactionID());
		txtTransactionTimestamp.setText(transaction.getTimestamp());
		txtTransactionAmount.setText("Amount: $" + String.format("%.2f",transaction.getAmount()));

		if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.PAYMENT)
		{
			imgTransactionIcon.setImageResource(R.drawable.lst_payment_icon);
			txtTransactionInfo.setText("To Payee: " + transaction.getPayeeId());
			txtTransactionAmount.setTextColor(Color.RED);
		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.TRANSFER)
		{
			imgTransactionIcon.setImageResource(R.drawable.lst_transfer_icon);
			txtTransactionInfo.setText("From: " + transaction.getSendingAccount() + " - " + "To: " +
			                           transaction.getDestinationAccount());
			txtTransactionAmount.setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_light));
		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.DEPOSIT)
		{
			imgTransactionIcon.setImageResource(R.drawable.lst_deposit_icon);
			txtTransactionInfo.setVisibility(View.GONE);
			txtTransactionAmount.setTextColor(getContext().getResources().getColor(android.R.color.holo_green_dark));
		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.CASH_DEPOSIT)
		{
			imgTransactionIcon.setImageResource(R.drawable.lst_deposit_icon);
			txtTransactionAmount.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
		}
		else if(transaction.getTransactionType() == Transaction.TRANSACTION_TYPE.LOAN)
		{
			if(transaction.getStatus() == Transaction.STATUS.PENDING)
			{
				imgTransactionIcon.setImageResource(R.drawable.pending_icon);
				txtTransactionStatus.setText(Transaction.STATUS.PENDING.toString());
			}
			else if(transaction.getStatus() == Transaction.STATUS.APPROVED)
			{
				imgTransactionIcon.setImageResource(R.drawable.approved_icon);
				txtTransactionStatus.setText(Transaction.STATUS.APPROVED.toString());
			}
			else if(transaction.getStatus() == Transaction.STATUS.DENIED)
			{
				imgTransactionIcon.setImageResource(R.drawable.denied_icon);
				txtTransactionStatus.setText(Transaction.STATUS.DENIED.toString());
			}
			txtTransactionInfo.setVisibility(View.GONE);
			txtTransactionAmount.setTextColor(getContext().getResources().getColor(android.R.color.holo_purple));
		}

		return convertView;
	}
}
