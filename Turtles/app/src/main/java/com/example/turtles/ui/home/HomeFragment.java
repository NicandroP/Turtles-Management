package com.example.turtles.ui.home;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static androidx.core.content.PermissionChecker.checkSelfPermission;
import static com.example.turtles.Utils.createTemporaryFile;
import static com.example.turtles.Utils.getImageUri;
import static com.example.turtles.Utils.getIndexFromArray;
import static com.example.turtles.Utils.saveImageToGallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.turtles.CustomAdapter;
import com.example.turtles.Utils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.turtles.R;
import com.example.turtles.Turtle;
import com.example.turtles.TurtleAdapter;
import com.example.turtles.databinding.FragmentHomeBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {
    final Activity activity = getActivity();
    String pictureImagePath = "";
    String pictureImagePath2 = "";
    String imageFileName;

    String currentTurtle;
    ArrayList<Turtle> list;
    private static final int ACTION_REQUEST_CAMERA = 100;
    private static final int ACTION_REQUEST_CAMERA_EDIT = 101;
    private ImageView editImage;
    private static final int ACTION_REQUEST_GALLERY = 1001;
    Uri imageUri;
    Uri newImageUri;
    String selectedCampo;
    String selectedGender="";
    String selectedAge="";
    private EditText editTextCode;
    private EditText editTextName;
    private Spinner genderSpinner;
    private Spinner campoSpinner;
    private Spinner ageSpinner;
    StorageReference storageReference;
    ImageView firebaseImage;
    ProgressDialog progressDialog;
    DatabaseReference reference= FirebaseDatabase.getInstance().getReference().child("Turtles");
    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button add =binding.add;
        final ImageButton filter=binding.filter;

        final RecyclerView recyclerView=binding.recyclerView;
        final Activity activity = requireActivity();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInsertDialog(view,activity);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity); // Esempio con LinearLayoutManager

        // Imposta il LayoutManager sulla RecyclerView
        recyclerView.setLayoutManager(layoutManager);


        list=new ArrayList<>();
        TurtleAdapter adapter = new TurtleAdapter(activity, list);
        recyclerView.setAdapter(adapter);
        //TurtleAdapter adapter=new TurtleAdapter<Turtle>(this,R.layout.list_item,list);

        adapter.setOnItemClickListener(new TurtleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                Turtle selectedItem = list.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);

                View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_layout, null);
                builder.setView(dialogView);
                ImageView dialogImageView = dialogView.findViewById(R.id.dialogImageView);
                TextView dialogTextViewName = dialogView.findViewById(R.id.dialogTextViewName);
                TextView dialogTextViewCode = dialogView.findViewById(R.id.dialogTextViewCode);
                TextView dialogTextViewCampo = dialogView.findViewById(R.id.dialogTextViewCampo);
                TextView dialogTextViewSesso = dialogView.findViewById(R.id.dialogTextViewSesso);
                TextView dialogTextViewEta = dialogView.findViewById(R.id.dialogTextViewEta);
                TextView dialogTextViewDate = dialogView.findViewById(R.id.dialogTextViewDateRegistration);
                ProgressBar loadingProgressBar = dialogView.findViewById(R.id.loadingProgressBar);
                loadingProgressBar.setVisibility(View.VISIBLE);
                dialogImageView.setVisibility(View.GONE);

                // Creare e mostrare il DialogView
                builder.setTitle("Dettagli");
                if(!selectedItem.getName().isEmpty()){
                    dialogTextViewName.setText("Nome: "+selectedItem.getName());
                }else{
                    dialogTextViewName.setText("Nome: Non assegnato");
                }

                if(selectedItem.getCodiceMicrochip()!=0){
                    dialogTextViewCode.setText("Codice microchip: "+selectedItem.getCodiceMicrochip());
                }else{
                    dialogTextViewCode.setText("Non assegnato");
                }

                dialogTextViewCampo.setText("Area: "+selectedItem.getCampo());

                if(!selectedItem.getSesso().isEmpty()){
                    dialogTextViewSesso.setText("Sesso: "+selectedItem.getSesso());
                }else{
                    dialogTextViewSesso.setText("Sesso: Non assegnato");
                }
                if(!selectedItem.getAge().isEmpty()){
                    dialogTextViewEta.setText("Età: "+selectedItem.getAge());
                }else{
                    dialogTextViewEta.setText("Età: Non assegnata");
                }

                dialogTextViewDate.setText("Registrazione: "+selectedItem.getRegistrationDate().replace("_", "/"));

                //StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(selectedItem.getUri());
                if(!selectedItem.getUri().isEmpty()){
                    StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
                    StorageReference ref = mImageStorage.child(selectedItem.getUri());

                    ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downUri = task.getResult();
                                String imageUrl = downUri.toString();
                                loadingProgressBar.setVisibility(View.GONE);
                                dialogImageView.setVisibility(View.VISIBLE);
                                Glide.with(requireActivity())
                                        .load(imageUrl)
                                        .override(600,1000)
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(dialogImageView
                                        );
                            }else{
                                Toast.makeText(activity, "Qualcosa è andato storto nel caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                builder.setNegativeButton("delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        list.remove(selectedItem);
                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                        Query applesQuery = ref.child("Turtles").orderByChild("id").equalTo(selectedItem.getId());


                        if(!selectedItem.getUri().isEmpty()){
                            StorageReference storageRef = FirebaseStorage.getInstance().getReference();
                            StorageReference imageRef = storageRef.child(selectedItem.getUri());


                            imageRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(activity, "Image Deletion successful", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(activity, "Image Deletion failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }


                        applesQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot appleSnapshot: dataSnapshot.getChildren()) {
                                    appleSnapshot.getRef().removeValue();
                                    Toast.makeText(activity, "Deletion succesfull", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(activity, "Deletion failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AlertDialog.Builder editBuilder = new AlertDialog.Builder(activity);
                        View editDialogView = getLayoutInflater().inflate(R.layout.dialog_edit_layout, null);
                        editBuilder.setView(editDialogView);

                        Button photoButton=editDialogView.findViewById(R.id.photoButton);
                        photoButton.setVisibility(View.GONE);
                        ImageButton deleteImg=editDialogView.findViewById(R.id.deleteImg);
                        ProgressBar editProgressBar=editDialogView.findViewById(R.id.editProgressBar);
                        editImage=editDialogView.findViewById(R.id.editImage);
                        EditText editName = editDialogView.findViewById(R.id.editTextName);
                        Spinner editAge = editDialogView.findViewById(R.id.ageSpinner);
                        Spinner editCampo = editDialogView.findViewById(R.id.campoSpinner);
                        Spinner editGender = editDialogView.findViewById(R.id.genderSpinner);
                        EditText editCode = editDialogView.findViewById(R.id.editCode);

                        editProgressBar.setVisibility(View.VISIBLE);
                        deleteImg.setVisibility(View.GONE);
                        editImage.setVisibility(View.GONE);

                        List<String> list = new ArrayList<String>();
                        list.add("Maschio");
                        list.add("Femmina");
                        list.add("Select one");
                        final int listsize = list.size() - 1;
                        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list) {
                            @Override
                            public int getCount() {
                                return(listsize); // Truncate the list
                            }
                        };

                        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        editGender.setAdapter(dataAdapter);
                        if(selectedItem.getSesso().isEmpty()){
                            editGender.setSelection(list.size() - 1);

                        }else{
                            int selectedIndex = list.indexOf(selectedItem.getSesso());
                            editGender.setSelection(selectedIndex);

                        }

                        editGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedGender = parent.getItemAtPosition(position).toString();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle the case when nothing is selected
                            }
                        });


                        List<String> list2 = new ArrayList<String>();
                        list2.add("1");
                        list2.add("2");
                        list2.add("3");
                        list2.add("Select one");
                        final int listsize2 = list2.size() - 1;

                        ArrayAdapter<String> dataAdapterCampo = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list2) {
                            @Override
                            public int getCount() {
                                return(listsize2); // Truncate the list
                            }
                        };

                        dataAdapterCampo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        editCampo.setAdapter(dataAdapterCampo);
                        editCampo.setSelection(listsize2);


                        editCampo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedCampo = parent.getItemAtPosition(position).toString();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle the case when nothing is selected
                            }
                        });

                        List<String> list3 = new ArrayList<String>();
                        list3.add("Adulto");
                        list3.add("Giovane");
                        list3.add("Select one");

                        final int listsize3 = list3.size() - 1;

                        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list3) {
                            @Override
                            public int getCount() {
                                return(listsize3); // Truncate the list
                            }
                        };

                        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        editAge.setAdapter(dataAdapter3);
                        if(selectedItem.getAge().isEmpty()){
                            editAge.setSelection(2);
                        }else{
                            editAge.setSelection(listsize3);
                        }


                        editAge.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedAge = parent.getItemAtPosition(position).toString();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Handle the case when nothing is selected
                            }
                        });

                        if(!selectedItem.getUri().isEmpty()){
                            StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
                            StorageReference ref = mImageStorage.child(selectedItem.getUri());


                            ref.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        Uri downUri = task.getResult();
                                        String imageUrl = downUri.toString();
                                        editProgressBar.setVisibility(View.GONE);
                                        editImage.setVisibility(View.VISIBLE);
                                        deleteImg.setVisibility(View.VISIBLE);
                                        Glide.with(requireActivity())
                                                .load(imageUrl)
                                                .override(600,1000)
                                                .diskCacheStrategy(DiskCacheStrategy.ALL)
                                                .into(editImage
                                                );
                                    }else{
                                        Toast.makeText(activity, "Qualcosa è andato storto nel caricamento dell'immagine", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            photoButton.setVisibility(View.VISIBLE);
                        }



                        deleteImg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
                                StorageReference ref = mImageStorage.child(selectedItem.getUri());


                                ref.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(activity, "Image Deletion successful", Toast.LENGTH_SHORT).show();
                                        selectedItem.setUri("");
                                        photoButton.setVisibility(View.VISIBLE);
                                        editImage.setVisibility(View.GONE);
                                        deleteImg.setVisibility(View.GONE);

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(activity, "Image Deletion failed", Toast.LENGTH_SHORT).show();
                                    }
                                });



                            }
                        });

                        photoButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                currentTurtle=selectedItem.getId();


                                Intent cameraIntent = new Intent(
                                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                imageFileName = timeStamp + ".jpg";
                                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                pictureImagePath2 = storageDir.getAbsolutePath() + "/" + imageFileName;
                                File file = new File(pictureImagePath2);
                                //Uri outputFileUri = Uri.fromFile(file);
                                Uri outputFileUri = FileProvider.getUriForFile(requireActivity(), "com.example.turtles.fileprovider", file);

                                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                                startActivityForResult(
                                        cameraIntent,
                                        ACTION_REQUEST_CAMERA_EDIT);


                                /*Intent cameraIntent = new Intent(
                                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(
                                        cameraIntent,
                                        ACTION_REQUEST_CAMERA_EDIT);*/

                            }
                        });

                        /*
                        int ageIndex;
                        String ageString = selectedItem.getAge();
                        if(ageString==""){
                            ageIndex = 2;
                        }else{
                            ageIndex = getIndexFromArray(list3, ageString);
                        }
                        editAge.setSelection(ageIndex);

                        int genderIndex;
                        String genderString = selectedItem.getSesso();
                        if(genderString==""){
                            genderIndex = 2;
                        }else{
                            genderIndex = getIndexFromArray(list3, ageString);
                        }
                        editGender.setSelection(genderIndex-1);
                        */

                        int campoString = selectedItem.getCampo();
                        int campoIndex = getIndexFromArray(list2, String.valueOf(campoString));
                        editName.setText(selectedItem.getName());

                        editCampo.setSelection(campoIndex);
                        editCode.setText(String.valueOf(selectedItem.getCodiceMicrochip()));

                        editBuilder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterfaceSave, int i) {
                                String newName = editName.getText().toString().trim();
                                String newAge = selectedAge;
                                String newCampo = selectedCampo;
                                String newGender = selectedGender;
                                String newCode = editCode.getText().toString().trim();

                                if (newCampo.isEmpty()) {
                                    Toast.makeText(activity, "Please enter area", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                if (!TextUtils.isDigitsOnly(newCode)) {
                                    // Gestisci il caso in cui il code non sia numerico
                                    Toast.makeText(activity, "Chip code must be numeric", Toast.LENGTH_SHORT).show();
                                    return;
                                }


                                // Modifica l'elemento selezionato con i nuovi valori
                                selectedItem.setName(newName);

                                if(newAge=="Select one"){
                                    newAge="";
                                }
                                if(newGender=="Select one"){
                                    newGender="";
                                }
                                selectedItem.setAge(newAge);
                                selectedItem.setSesso(newGender);

                                int newCampoInt = Integer.parseInt(newCampo);
                                selectedItem.setCampo(newCampoInt);

                                int newCodeInt = Integer.parseInt(newCode);
                                selectedItem.setCodiceMicrochip(newCodeInt);

                                reference.child(selectedItem.getId()).setValue(selectedItem)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(activity, "Edit successful", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(activity, "Edit failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });

                                dialogInterfaceSave.dismiss();
                            }
                        });



                        editBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterfaceCancel, int i) {
                                dialogInterfaceCancel.dismiss();
                            }
                        });

                        AlertDialog dialog = editBuilder.create();
                        dialog.show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                list.clear();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Turtle turtle=snapshot.getValue(Turtle.class);
                    if (turtle != null) {
                        //String txt = turtle.getName() + " : " + turtle.getAge();
                        list.add(turtle);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return root;
    }

    public void openInsertDialog(View view, Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add, null);
        builder.setView(dialogView);

        editTextCode = dialogView.findViewById(R.id.editTextCode);
        editTextName = dialogView.findViewById(R.id.editTextName);
        genderSpinner = dialogView.findViewById(R.id.genderSpinner);
        campoSpinner = dialogView.findViewById(R.id.campoSpinner);
        ageSpinner = dialogView.findViewById(R.id.ageSpinner);


        List<String> list1 = new ArrayList<String>();
        list1.add("Maschio");
        list1.add("Femmina");
        list1.add("Select one");
        final int listsize = list1.size() - 1;

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list1) {
            @Override
            public int getCount() {
                return(listsize); // Truncate the list
            }
        };

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderSpinner.setAdapter(dataAdapter);
        genderSpinner.setSelection(listsize); // Hidden item to appear in the spinner


        genderSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });


        List<String> list2 = new ArrayList<String>();
        list2.add("1");
        list2.add("2");
        list2.add("3");
        list2.add("Select one");
        final int listsize2 = list2.size() - 1;

        ArrayAdapter<String> dataAdapterCampo = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list2) {
            @Override
            public int getCount() {
                return(listsize2); // Truncate the list
            }
        };

        dataAdapterCampo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        campoSpinner.setAdapter(dataAdapterCampo);
        campoSpinner.setSelection(listsize2); // Hidden item to appear in the spinner
        campoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCampo = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });



        List<String> list3 = new ArrayList<String>();
        list3.add("Adulto");
        list3.add("Giovane");
        list3.add("Select one");
        final int listsize3 = list3.size() - 1;

        ArrayAdapter<String> dataAdapter3 = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list3) {
            @Override
            public int getCount() {
                return(listsize3); // Truncate the list
            }
        };

        dataAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ageSpinner.setAdapter(dataAdapter3);
        ageSpinner.setSelection(listsize3); // Hidden item to appear in the spinner
        ageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedAge = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle the case when nothing is selected
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                // Resettare la variabile imageUri quando l'utente apre la finestra di dialogo per inserire un nuovo elemento
                imageUri = null;
                firebaseImage.setImageURI(null);
            }
        });
        dialog.show();

        Button btnInsert = dialogView.findViewById(R.id.btnInsert);
        Button btnSelectImg = dialogView.findViewById(R.id.btnSelectImg);
        firebaseImage = dialogView.findViewById(R.id.firebaseImage);

        btnInsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = editTextCode.getText().toString().trim();
                boolean isCodePresent = false;
                for (Turtle turtle : list) {
                    if(!code.isEmpty()){
                        if (turtle.getCodiceMicrochip() == Integer.parseInt(code)) {
                            isCodePresent = true;
                            break;
                        }
                    }

                }

                if (isCodePresent) {
                    Toast.makeText(activity, "Tartaruga già presente con questo codice", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (selectedCampo.isEmpty() || selectedCampo=="Select one") {
                    // Gestisci il caso in cui i campi siano vuoti
                    Toast.makeText(activity, "Please enter area!", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (!TextUtils.isDigitsOnly(editTextCode.getText().toString())) {
                    // Gestisci il caso in cui il code non sia numerico
                    Toast.makeText(activity, "Code must be numeric", Toast.LENGTH_SHORT).show();
                    return;
                }

                //int age = Integer.parseInt(ageStr);
                progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle("Uploading....");
                progressDialog.show();

                SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy HH:mm", Locale.ITALIAN);
                Date now = new Date();
                String currentDate = formatter.format(new Date());
                String fileName = formatter.format(now);
                storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName);

                if (imageUri != null) {
                    storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            firebaseImage.setImageURI(null);
                            Toast.makeText(activity, "Successfully uploaded", Toast.LENGTH_SHORT).show();
                            String imageUrl = imageUri.toString();
                            //int codeInt = Integer.parseInt(code);
                            int campoInt = Integer.parseInt(selectedCampo);

                            Turtle newTurtle = new Turtle(campoInt, storageReference.getPath());
                            if (!selectedAge.isEmpty() && selectedAge!="Select one") {
                                newTurtle.setAge(selectedAge);
                            }else{
                                newTurtle.setAge("");
                            }
                            if (!selectedGender.isEmpty() && selectedGender!="Select one") {
                                newTurtle.setSesso(selectedGender);
                            }else{
                                newTurtle.setSesso("");
                            }
                            if (!editTextName.getText().toString().isEmpty()) {
                                newTurtle.setName(editTextName.getText().toString());
                            }
                            if (!editTextCode.getText().toString().isEmpty()) {
                                newTurtle.setCodiceMicrochip(Integer.parseInt(editTextCode.getText().toString()));
                            }
                            DatabaseReference newTurtleRef = reference.push();
                            String turtleId = newTurtleRef.getKey();
                            newTurtle.setId(turtleId);
                            newTurtle.setRegistrationDate(currentDate);

                            // Utilizza direttamente newTurtleRef per impostare il nuovo elemento
                            newTurtleRef.setValue(newTurtle).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        Toast.makeText(activity, "Insert successful", Toast.LENGTH_SHORT).show();
                                    } else {
                                        if (progressDialog.isShowing()) {
                                            progressDialog.dismiss();
                                        }
                                        Toast.makeText(activity, "Insert failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(activity, "Failed to upload", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.dismiss();
                } else {
                    // Caso in cui l'utente non ha selezionato un'immagine
                    Toast.makeText(activity, "Please select an image", Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                }

                // Chiudi la finestra di dialogo dopo l'inserimento
                //dialog.dismiss();
            }
        });

        btnSelectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = timeStamp + ".jpg";
                File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                File file = new File(pictureImagePath);
                //Uri outputFileUri = Uri.fromFile(file);
                Uri outputFileUri = FileProvider.getUriForFile(requireActivity(), "com.example.turtles.fileprovider", file);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(
                        cameraIntent,
                        ACTION_REQUEST_CAMERA);

                /*Intent cameraIntent = new Intent(
                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(
                        cameraIntent,
                        ACTION_REQUEST_CAMERA);

                Dialog dialog = new Dialog(getActivity());
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Choose Image Source");
                builder.setItems(new CharSequence[] { "Gallery", "Camera" },
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                switch (which) {
                                    case 0:
                                        Intent intent = new Intent(
                                                Intent.ACTION_GET_CONTENT);
                                        intent.setType("image/*");

                                        Intent chooser = Intent
                                                .createChooser(
                                                        intent,
                                                        "Choose a Picture");
                                        startActivityForResult(
                                                chooser,
                                                ACTION_REQUEST_GALLERY);

                                        break;

                                    case 1:
                                        Intent cameraIntent = new Intent(
                                                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

                                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                                        String imageFileName = timeStamp + ".jpg";
                                        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                        pictureImagePath = storageDir.getAbsolutePath() + "/" + imageFileName;
                                        File file = new File(pictureImagePath);
                                        //Uri outputFileUri = Uri.fromFile(file);
                                        Uri outputFileUri = FileProvider.getUriForFile(requireActivity(), "com.example.turtles.fileprovider", file);

                                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                                        startActivityForResult(
                                                cameraIntent,
                                                ACTION_REQUEST_CAMERA);

                                        break;

                                    default:
                                        break;
                                }
                            }
                        });

                builder.show();
                dialog.dismiss();*/

            }
        });

    }

    private Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // In alternativa, puoi utilizzare il metodo createBitmap per mantenere le dimensioni originali
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ACTION_REQUEST_GALLERY) {
                if (data != null && data.getData() != null) {
                    imageUri = data.getData();
                    Glide.with(requireContext())
                            .load(imageUri)
                            .override(600, 600)
                            .into(firebaseImage);
                }
            }else if (requestCode == ACTION_REQUEST_CAMERA) {

                File imgFile = new  File(pictureImagePath);
                if(imgFile.exists()){
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    Bitmap myBitmap2 = rotateBitmap(myBitmap, 90);
                    imageUri = getImageUri(requireActivity(), myBitmap2);
                    Glide.with(requireContext())
                            .load(myBitmap2)
                            .override(600, 1000)
                            .into(firebaseImage);

                }
            }else if(requestCode == ACTION_REQUEST_CAMERA_EDIT){
                File imgFile2 = new  File(pictureImagePath2);
                if(imgFile2.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile2.getAbsolutePath());
                    Bitmap myBitmap2 = rotateBitmap(myBitmap, 90);
                    newImageUri = getImageUri(requireActivity(), myBitmap2);
                    Glide.with(requireContext())
                            .load(myBitmap2)
                            .override(600, 1000)
                            .into(editImage);
                    SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy HH:mm", Locale.ITALIAN);
                    Date now = new Date();
                    String fileName = formatter.format(now);
                    storageReference = FirebaseStorage.getInstance().getReference("images/" + fileName);
                    if (newImageUri != null) {

                        storageReference.putFile(newImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                try {
                                    DatabaseReference turtlesRef = FirebaseDatabase.getInstance().getReference("Turtles");

                                    DatabaseReference turtleToUpdateRef = turtlesRef.child(currentTurtle);

                                    turtleToUpdateRef.child("uri").setValue(storageReference.getPath());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}