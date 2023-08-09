package com.example.turtles;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TurtleAdapter extends RecyclerView.Adapter<TurtleAdapter.ViewHolder> {

    private Context context;
    private ArrayList<Turtle> turtleList;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }


    public TurtleAdapter(Context context, ArrayList<Turtle> turtleList) {
        this.context = context;
        this.turtleList = turtleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int clickedPosition = holder.getAdapterPosition();
                if (clickedPosition != RecyclerView.NO_POSITION && onItemClickListener != null) {
                    onItemClickListener.onItemClick(clickedPosition);
                }
            }
        });

        Turtle turtle = turtleList.get(position);
        int code = turtle.getCodiceMicrochip();
        String textCode;
        if (code == 0) {
            textCode = "Non assegnato";
        } else {
            textCode = String.valueOf(code);
        }
        holder.textViewCode.setText("Codice microchip: " + textCode);
        holder.textViewCampo.setText("Area n. " + String.valueOf(turtle.getCampo()));
    }

    @Override
    public int getItemCount() {
        return turtleList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewCode;
        TextView textViewCampo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewCode = itemView.findViewById(R.id.textViewCode);
            textViewCampo = itemView.findViewById(R.id.textViewCampo);

            // Aggiungi l'OnClickListener all'elemento
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Turtle selectedItem = turtleList.get(position);

                    }
                }
            });
        }
    }

}

