package com.smsindia.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.yourpackage.R;
import com.yourpackage.model.WithdrawalModel;
import java.util.List;

public class WithdrawalAdapter extends RecyclerView.Adapter<WithdrawalAdapter.ViewHolder> {

    private List<WithdrawalModel> list;
    private final OnPayClickListener listener;

    public interface OnPayClickListener {
        void onPay(WithdrawalModel request);
    }

    public WithdrawalAdapter(List<WithdrawalModel> list, OnPayClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_withdrawal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WithdrawalModel item = list.get(position);
        holder.tvUpi.setText(item.upi_id);
        holder.tvAmount.setText("₹" + item.amount);
        
        holder.btnPay.setOnClickListener(v -> listener.onPay(item));
    }

    @Override
    public int getItemCount() { return list.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUpi, tvAmount;
        Button btnPay;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUpi = itemView.findViewById(R.id.tv_upi);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            btnPay = itemView.findViewById(R.id.btn_pay);
        }
    }
}
