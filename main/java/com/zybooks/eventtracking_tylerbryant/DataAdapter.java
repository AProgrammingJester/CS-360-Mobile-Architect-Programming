package com.zybooks.eventtracking_tylerbryant;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    private final List<DataItem> dataList;
    private final Context context;
    private final OnItemClickListener listener;

    public DataAdapter(Context context, List<DataItem> dataList, OnItemClickListener listener) {
        this.context = context;
        this.dataList = dataList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.data_row, parent, false);
        view.setTag(listener); // Store listener reference
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DataItem item = dataList.get(position);
        holder.textName.setText(item.getName());
        holder.textDate.setText(item.getDate());

        holder.buttonDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                dataList.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, dataList.size());

                if (context instanceof DataGridActivity) {
                    ((DataGridActivity) context).saveDataList();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textDate;
        Button buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textRowName);
            textDate = itemView.findViewById(R.id.textRowDate);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteRow);

            itemView.setOnClickListener(v -> {
                int pos = getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && itemView.getTag() instanceof OnItemClickListener) {
                    ((OnItemClickListener) itemView.getTag()).onItemClick(pos);
                }
            });
        }
    }
}