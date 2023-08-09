package com.example.turtles.ui.dashboard;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.turtles.Utils;
import com.example.turtles.databinding.FragmentDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

public class DashboardFragment extends Fragment {
    private FragmentDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        final TextView totTurtles = binding.totTurtles;
        final TextView adulti = binding.adulti;
        final TextView giovani = binding.giovani;
        final TextView sessoF = binding.sessoF;
        final TextView sessoM = binding.sessoM;
        final ImageView imageView=binding.campi;

        // Ottieni il riferimento al nodo "Turtles" nel database
        DatabaseReference turtlesRef = FirebaseDatabase.getInstance().getReference().child("Turtles");

        // Aggiungi un ValueEventListener per ascoltare i cambiamenti nel nodo "Turtles"
        turtlesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Quando ci sono cambiamenti nel nodo "Turtles", ottieni il numero totale delle tartarughe
                long totalTurtles = dataSnapshot.getChildrenCount();

                // Imposta il testo nella TextView "totTurtles"
                totTurtles.setText("Totale tartarughe: " + totalTurtles);

                int numMaschi = 0;
                int numFemmine = 0;

                for (DataSnapshot turtleSnapshot : dataSnapshot.getChildren()) {
                    // Ottieni il valore del campo "sesso" per l'elemento corrente
                    String sessoTartaruga = turtleSnapshot.child("sesso").getValue(String.class);

                    // Conta maschi e femmine
                    if ("Maschio".equals(sessoTartaruga)) {
                        numMaschi++;
                    } else if ("Femmina".equals(sessoTartaruga)) {
                        numFemmine++;
                    }
                }
                String sessoFText = "&#8226; Femmine: "+numFemmine;
                sessoF.setText(Html.fromHtml(sessoFText, Html.FROM_HTML_MODE_COMPACT));

                String sessoMText = "&#8226; Maschi: "+numMaschi;
                sessoM.setText(Html.fromHtml(sessoMText, Html.FROM_HTML_MODE_COMPACT));


                int numAdulti = 0;
                int numGiovani = 0;

                for (DataSnapshot turtleSnapshot : dataSnapshot.getChildren()) {
                    // Ottieni il valore del campo "sesso" per l'elemento corrente
                    String etaTartaruga = turtleSnapshot.child("age").getValue(String.class);

                    // Conta maschi e femmine
                    if ("Adulto".equals(etaTartaruga)) {
                        numAdulti++;
                    } else if ("Giovane".equals(etaTartaruga)) {
                        numGiovani++;
                    }
                }

                String adultiText = "&#8226; Adulti: "+numAdulti;
                adulti.setText(Html.fromHtml(adultiText, Html.FROM_HTML_MODE_COMPACT));

                String giovaniText = "&#8226; Giovani: "+numGiovani;
                giovani.setText(Html.fromHtml(giovaniText, Html.FROM_HTML_MODE_COMPACT));



                int numCampo1 = 0;
                int numCampo2 = 0;
                int numCampo3 = 0;

                for (DataSnapshot turtleSnapshot : dataSnapshot.getChildren()) {
                    // Ottieni il valore del campo "sesso" per l'elemento corrente
                    Long campo = turtleSnapshot.child("campo").getValue(Long.class);

                    // Conta maschi e femmine
                    if ("1".equals(campo.toString())) {
                        numCampo1++;
                    } else if ("2".equals(campo.toString())) {
                        numCampo2++;
                    } else if("3".equals(campo.toString())){
                        numCampo3++;
                    }
                }



                BitmapDrawable dynamicImage = Utils.createDynamicTextImage(getActivity(), numCampo3+"", numCampo2+"", numCampo1+"");
                imageView.setImageDrawable(dynamicImage);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Gestisci eventuali errori
            }
        });

        // ... il resto del codice ...

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}