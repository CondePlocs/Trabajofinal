package com.example.proyectofinalisismo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.Dialog;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MenuActivity extends AppCompatActivity {

    private CardView cardIdenti, cardresiduo, normativaCard, cardAyuda, cardReporte, cardTop;
    private TextView txtNombreDinamico, txtCodigoDinamico;
    private Button btnVolver;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");
        CardView cardReporte = findViewById(R.id.cardReporte);
        cardresiduo = findViewById(R.id.cardresiduo);
        txtNombreDinamico = findViewById(R.id.txtNombreDinamico);
        txtCodigoDinamico = findViewById(R.id.txtCodigoDinamico);
        btnVolver = findViewById(R.id.btn_volver);
        cardAyuda = findViewById(R.id.card_ayuda);
        cardIdenti = findViewById(R.id.card_identi);
        normativaCard = findViewById(R.id.normativaCard);
        cardTop = findViewById(R.id.cardTop);


        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            txtCodigoDinamico.setText(uid);

            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        String nombres = snapshot.child("nombres").getValue(String.class);
                        String apellidos = snapshot.child("apellidos").getValue(String.class);
                        txtNombreDinamico.setText(nombres + " " + apellidos);
                    } else {
                        txtNombreDinamico.setText("Nombre no encontrado");
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    txtNombreDinamico.setText("Error al cargar");
                }
            });
        } else {
            Intent intent = new Intent(MenuActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        btnVolver.setOnClickListener(v -> cerrarSesion());
        cardresiduo.setOnClickListener(v -> startActivity(new Intent(MenuActivity.this, RegistrarResiduoActivity.class)));
        cardAyuda.setOnClickListener(v -> realizarLlamada());
        cardIdenti.setOnClickListener(v -> mostrarDialogoFotochequeo());
        normativaCard.setOnClickListener(v -> mostrarDialogNormativa());
        cardReporte.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, ReporteActivity.class);
            startActivity(intent);
        });
        cardTop.setOnClickListener(v -> {
            showTopDialog();
        });

    }


    private void cerrarSesion() {
        mAuth.signOut();
        Toast.makeText(MenuActivity.this, "Sesión cerrada con éxito", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MenuActivity.this, LoginActivity.class));
        finish();
    }

    private void realizarLlamada() {
        String numeroTelefono = "987456321";
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numeroTelefono));
        startActivity(intent);
    }

    private void mostrarDialogoFotochequeo() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_user_info);

        // Referencias a los campos del XML
        TextView tvNombre = dialog.findViewById(R.id.tvNombre);
        TextView tvApellidos = dialog.findViewById(R.id.tvApellidos);
        TextView tvCorreo = dialog.findViewById(R.id.tvCorreo);
        TextView tvTelefono = dialog.findViewById(R.id.tvTelefono);
        TextView tvDistrito = dialog.findViewById(R.id.tvDistrito);
        TextView tvFechaCreacion = dialog.findViewById(R.id.tvFechaCreacion);
        TextView tvUltimaConexion = dialog.findViewById(R.id.tvUltimaConexion);
        TextView tvDni = dialog.findViewById(R.id.tvDni); // Nuevo campo
        TextView tvCodigoTrabajador = dialog.findViewById(R.id.tvCodigoTrabajador); // Nuevo campo
        MaterialButton btnCerrar = dialog.findViewById(R.id.img_cerrar);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Obtener datos del Realtime Database
            databaseReference.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        tvNombre.setText(snapshot.child("nombres").getValue(String.class));
                        tvApellidos.setText(snapshot.child("apellidos").getValue(String.class));
                        tvCorreo.setText(snapshot.child("correo").getValue(String.class));
                        tvTelefono.setText(snapshot.child("telefono").getValue(String.class));
                        tvDistrito.setText(snapshot.child("zonal").getValue(String.class)); // Cambio de "distrito" a "zonal"
                        tvDni.setText(snapshot.child("dni").getValue(String.class));
                        tvCodigoTrabajador.setText(snapshot.child("codigoTrabajador").getValue(String.class));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Toast.makeText(MenuActivity.this, "Error al cargar datos", Toast.LENGTH_SHORT).show();
                }
            });


            // Obtener datos del Authentication (fecha de creación y última sesión)
            long creationTimestamp = user.getMetadata().getCreationTimestamp();
            long lastSignInTimestamp = user.getMetadata().getLastSignInTimestamp();

            tvFechaCreacion.setText(convertirTimestampAFecha(creationTimestamp));
            tvUltimaConexion.setText(convertirTimestampAFecha(lastSignInTimestamp));
        }

        btnCerrar.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String convertirTimestampAFecha(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }


    private void mostrarDialogNormativa() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_normas);
        dialog.setCancelable(true);

        Button btnCerrar = dialog.findViewById(R.id.btnCerrar);
        btnCerrar.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }
    private void showTopDialog() {
        // Crear el diálogo
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_top);
        dialog.setCancelable(false);

        // Configurar elementos del diálogo
        ListView listView = dialog.findViewById(R.id.topListView);
        Button closeButton = dialog.findViewById(R.id.closeButton);

        // Firebase referencia
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("residuos");

        // Mapa para contar registros por tipoResiduo
        Map<String, Integer> residuoCount = new HashMap<>();

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    String tipoResiduo = data.child("tipoResiduo").getValue(String.class);
                    if (tipoResiduo != null) {
                        residuoCount.put(tipoResiduo, residuoCount.getOrDefault(tipoResiduo, 0) + 1);
                    }
                }

                // Ordenar por cantidad (descendente)
                List<Map.Entry<String, Integer>> sortedResiduos = new ArrayList<>(residuoCount.entrySet());
                sortedResiduos.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                // Obtener el top 10
                List<String> topResiduos = new ArrayList<>();
                int count = 0;
                for (Map.Entry<String, Integer> entry : sortedResiduos) {
                    if (count++ >= 10) break;
                    topResiduos.add(entry.getKey() + " - " + entry.getValue() + " registros");
                }

                // Adaptador para la lista
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, topResiduos);
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), "Error al obtener datos", Toast.LENGTH_SHORT).show();
            }
        });

        // Acción del botón cerrar
        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Mostrar el diálogo
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }

}






