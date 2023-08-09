package com.example.turtles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class TurtleAdapter extends ArrayAdapter<Turtle> {

    private Context context;
    private List<Turtle> turtleList;


    public TurtleAdapter(Context context, List<Turtle> turtleList) {
        super(context, 0, turtleList);
        this.context = context;
        this.turtleList = turtleList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        TextView textViewCode = convertView.findViewById(R.id.textViewCode);
        TextView textViewCampo = convertView.findViewById(R.id.textViewCampo);

        Turtle turtle = turtleList.get(position);
        int code=turtle.getCodiceMicrochip();
        String textCode;
        if(code==0){
            textCode="Non assegnato";
        }else{
            textCode=String.valueOf(turtle.getCodiceMicrochip());
        }
        textViewCode.setText("Codice microchip: "+textCode);
        textViewCampo.setText("Area n. "+String.valueOf(turtle.getCampo()));

        return convertView;
    }
}
