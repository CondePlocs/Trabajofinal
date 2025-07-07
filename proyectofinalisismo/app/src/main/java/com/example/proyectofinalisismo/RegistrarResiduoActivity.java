package com.example.proyectofinalisismo;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class RegistrarResiduoActivity extends AppCompatActivity {

    private AutoCompleteTextView autoCompleteTipoResiduo, autoCompleteEstadoResiduo;
    private EditText etCantidad, etDescripcion;
    private Button btnRegistrarResiduo, btnsaslir;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrar_residuo);

        // Configuración de insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("residuos");
        mAuth = FirebaseAuth.getInstance();

        // Vincular campos del XML
        autoCompleteTipoResiduo = findViewById(R.id.autoCompleteTipoResiduo);
        autoCompleteEstadoResiduo = findViewById(R.id.autoCompleteEstadoResiduo);
        etCantidad = findViewById(R.id.etCantidad);
        etDescripcion = findViewById(R.id.etDescripcion);
        btnRegistrarResiduo = findViewById(R.id.btnRegistrarResiduo);
        btnsaslir = findViewById(R.id.btnsaslir);

        btnsaslir.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrarResiduoActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Opcional: cerrar la actividad actual
        });

        // Opciones para el tipo de residuo
        String[] tiposResiduos = {
                "Plástico", "Vidrio", "Metal", "Papel", "Cartón", "Textiles", "Orgánico",
                "Residuos Hospitalarios", "Productos Químicos", "Baterías y Pilas", "Electrónicos",
                "Residuos Industriales no peligrosos", "Residuos Industriales peligrosos",
                "Escorias y Cenizas", "Concreto", "Ladrillos", "Madera", "Asfalto",
                "Residuos Municipales", "Desechos Voluminosos", "Residuos de Limpieza Urbana",
                "Lámparas Fluorescentes", "Cartuchos de Tinta", "Aceites Usados"
        };

        ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposResiduos);
        autoCompleteTipoResiduo.setAdapter(adapterTipo);

        // Opciones para el estado del residuo
        String[] estadosResiduos = {
                "Bueno", "Malo", "RAE", "Chatarra", "Seco", "Húmedo", "Compactado",
                "Suelto", "Contaminado", "Triturado", "Pulverizado", "Fragmentado",
                "Corrosivo", "Voluminoso", "Degradado", "Mezclado", "En Descomposición",
                "Reciclado", "Reutilizable", "Quemado"
        };

        ArrayAdapter<String> adapterEstado = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, estadosResiduos);
        autoCompleteEstadoResiduo.setAdapter(adapterEstado);

        // Mostrar el dropdown al enfocar
        autoCompleteTipoResiduo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteTipoResiduo.showDropDown();
        });

        autoCompleteEstadoResiduo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) autoCompleteEstadoResiduo.showDropDown();
        });

        // Acción del botón Registrar
        btnRegistrarResiduo.setOnClickListener(v -> registrarResiduo());
    }

    private void registrarResiduo() {
        String tipoResiduo = autoCompleteTipoResiduo.getText().toString().trim();
        String estadoResiduo = autoCompleteEstadoResiduo.getText().toString().trim();
        String cantidad = etCantidad.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();

        // Validación de campos
        if (!validarCampos(tipoResiduo, estadoResiduo, cantidad)) {
            return; // No continuar si hay errores
        }

        // Obtener la fecha y hora actual
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String fechaHoraActual = dateFormat.format(new Date());

        FirebaseUser user = mAuth.getCurrentUser();
        String registradoPor = (user != null) ? user.getEmail() : "Desconocido";
        String uid = (user != null) ? user.getUid() : "Sin UID";

        // Crear objeto para guardar en Firebase
        HashMap<String, String> residuo = new HashMap<>();
        residuo.put("tipoResiduo", tipoResiduo);
        residuo.put("estadoResiduo", estadoResiduo);
        residuo.put("cantidad", cantidad + " kg"); // Añadir "kg" al final
        residuo.put("descripcion", descripcion.isEmpty() ? "Sin descripción" : descripcion);
        residuo.put("fechaHora", fechaHoraActual);
        residuo.put("registradoPor", registradoPor);
        residuo.put("uid", uid);

        // Guardar en Firebase
        databaseReference.push().setValue(residuo).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Residuo registrado exitosamente", Toast.LENGTH_LONG).show();
                limpiarCampos();
            } else {
                Toast.makeText(this, "Error al registrar el residuo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Validaciones de campos
    private boolean validarCampos(String tipo, String estado, String cantidad) {
        if (tipo.isEmpty()) {
            Toast.makeText(this, "El tipo de residuo es obligatorio.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (estado.isEmpty()) {
            Toast.makeText(this, "El estado del residuo es obligatorio.", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (cantidad.isEmpty()) {
            Toast.makeText(this, "La cantidad en kg es obligatoria.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validar que la cantidad solo contenga números positivos
        try {
            double cantidadNum = Double.parseDouble(cantidad);
            if (cantidadNum <= 0) {
                Toast.makeText(this, "La cantidad debe ser un número positivo.", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "La cantidad debe ser un número válido.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // Todo correcto
    }



    private void limpiarCampos() {
        autoCompleteTipoResiduo.setText("");
        autoCompleteEstadoResiduo.setText("");
        etCantidad.setText("");
        etDescripcion.setText("");
    }
}




