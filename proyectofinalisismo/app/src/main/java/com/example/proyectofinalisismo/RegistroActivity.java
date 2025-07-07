package com.example.proyectofinalisismo;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegistroActivity extends AppCompatActivity {

    private EditText etNombres, etApellidos, etCorreo, etTelefono, etPassword, etConfirmPassword, etZonal, etDNI, etCodigoTrabajador;
    private Button btnRegistrar;
    private TextView txtVolver;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        // Inicializar Firebase
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("usuarios");

        // Vincular elementos del XML
        etNombres = findViewById(R.id.etNombres);
        etApellidos = findViewById(R.id.etApellidos);
        etCorreo = findViewById(R.id.etCorreo);
        etTelefono = findViewById(R.id.etTelefono);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etZonal = findViewById(R.id.etZonal);
        etDNI = findViewById(R.id.etDNI);
        etCodigoTrabajador = findViewById(R.id.etCodigoTrabajador);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtVolver = findViewById(R.id.txtVolver);

        // Configurar teclados específicos
        etDNI.setInputType(android.text.InputType.TYPE_CLASS_NUMBER); // Teclado numérico
        etCodigoTrabajador.setInputType(android.text.InputType.TYPE_CLASS_TEXT); // Teclado normal

        txtVolver.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        // Ajustar padding para diseño EdgeToEdge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Acción del botón registrar
        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombres = etNombres.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        String zonal = etZonal.getText().toString().trim();
        String dni = etDNI.getText().toString().trim();
        String codigoTrabajador = etCodigoTrabajador.getText().toString().trim();

        // Validaciones campo por campo
        if (TextUtils.isEmpty(nombres)) {
            Toast.makeText(this, "Por favor, ingresa tus nombres", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(apellidos)) {
            Toast.makeText(this, "Por favor, ingresa tus apellidos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(correo)) {
            Toast.makeText(this, "Por favor, ingresa tu correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(telefono)) {
            Toast.makeText(this, "Por favor, ingresa tu número de teléfono", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(zonal)) {
            Toast.makeText(this, "Por favor, ingresa tu zonal", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(dni)) {
            Toast.makeText(this, "Por favor, ingresa tu DNI", Toast.LENGTH_SHORT).show();
            return;
        } else if (dni.length() != 8) {
            Toast.makeText(this, "El DNI debe tener 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(codigoTrabajador)) {
            Toast.makeText(this, "Por favor, ingresa tu código de trabajador", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Por favor, ingresa una contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Por favor, confirma tu contraseña", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear usuario en Firebase Authentication
        mAuth.createUserWithEmailAndPassword(correo, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        guardarDatosEnRealtime(user.getUid(), nombres, apellidos, correo, telefono, zonal, dni, codigoTrabajador);
                    } else {
                        Toast.makeText(this, "Error al registrar: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }


    private void guardarDatosEnRealtime(String userId, String nombres, String apellidos, String correo, String telefono, String zonal, String dni, String codigoTrabajador) {
        HashMap<String, String> usuario = new HashMap<>();
        usuario.put("nombres", nombres);
        usuario.put("apellidos", apellidos);
        usuario.put("correo", correo);
        usuario.put("telefono", telefono);
        usuario.put("zonal", zonal);
        usuario.put("dni", dni);
        usuario.put("codigoTrabajador", codigoTrabajador);

        databaseReference.child(userId).setValue(usuario).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_LONG).show();
                limpiarCampos();
                // Redirige a ExitoActivity
                Intent intent = new Intent(RegistroActivity.this, ExitoActivity.class);
                startActivity(intent);
                finish(); // Finaliza esta actividad para que no vuelva atrás con el botón
            } else {
                Toast.makeText(this, "Error al guardar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void limpiarCampos() {
        etNombres.setText("");
        etApellidos.setText("");
        etCorreo.setText("");
        etTelefono.setText("");
        etZonal.setText("");
        etDNI.setText("");
        etCodigoTrabajador.setText("");
        etPassword.setText("");
        etConfirmPassword.setText("");
    }
}


