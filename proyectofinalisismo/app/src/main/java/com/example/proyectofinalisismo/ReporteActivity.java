package com.example.proyectofinalisismo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class ReporteActivity extends AppCompatActivity {

    private AutoCompleteTextView tipoResiduoInput;
    private Button btnFiltrar, backinblack, btnFechaFiltro;
    private TableLayout tablaResiduos;
    private DatabaseReference databaseReference; // Referencia a Firebase

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte);

        // Vinculación con el XML
        tipoResiduoInput = findViewById(R.id.tipoResiduoAutoComplete);
        btnFiltrar = findViewById(R.id.btnFiltrar);
        tablaResiduos = findViewById(R.id.tablaResiduos);
        backinblack = findViewById(R.id.backinblack);
        // Configurar Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("residuos");

        // Cargar tipos de residuos
        cargarTipoResiduoInput();

        //fecha
        btnFechaFiltro = findViewById(R.id.btnFechaFiltro);

        btnFechaFiltro.setOnClickListener(v -> mostrarSelectorDeFecha());

        // Filtrar al presionar el botón
        btnFiltrar.setOnClickListener(v -> filtrarResultados());
        backinblack.setOnClickListener(v -> {
            Intent intent = new Intent(ReporteActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Opcional: cerrar la actividad actual
        });

        // Cargar todos los datos al inicio
        cargarTodosLosRegistros();
    }

    private void cargarTipoResiduoInput() {
        String[] tiposResiduos = {"Plástico", "Vidrio", "Metal", "Papel", "Cartón", "Textiles", "Orgánico",
                "Residuos Hospitalarios", "Productos Químicos", "Baterías y Pilas", "Electrónicos",
                "Residuos Industriales no peligrosos", "Residuos Industriales peligrosos",
                "Escorias y Cenizas", "Concreto", "Ladrillos", "Madera", "Asfalto",
                "Residuos Municipales", "Desechos Voluminosos", "Residuos de Limpieza Urbana",
                "Lámparas Fluorescentes", "Cartuchos de Tinta", "Aceites Usados"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, tiposResiduos);
        tipoResiduoInput.setAdapter(adapter);
        tipoResiduoInput.setThreshold(1);

        tipoResiduoInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) tipoResiduoInput.showDropDown();
        });

        tipoResiduoInput.setOnClickListener(v -> tipoResiduoInput.showDropDown());
    }

    private void cargarTodosLosRegistros() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tablaResiduos.removeAllViews();
                boolean hayResultados = false;

                for (DataSnapshot residuoSnapshot : snapshot.getChildren()) {
                    agregarFilaSiCoincideFiltro(residuoSnapshot);
                    hayResultados = true;
                }

                if (!hayResultados) {
                    mostrarMensajeSinResultados();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReporteActivity.this, "Error en Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filtrarResultados() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tablaResiduos.removeAllViews();
                boolean hayResultados = false;

                for (DataSnapshot residuoSnapshot : snapshot.getChildren()) {
                    if (agregarFilaSiCoincideFiltro(residuoSnapshot)) {
                        hayResultados = true;
                    }
                }

                if (!hayResultados) {
                    mostrarMensajeSinResultados();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReporteActivity.this, "Error en Firebase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean agregarFilaSiCoincideFiltro(DataSnapshot residuoSnapshot) {
        String tipo = residuoSnapshot.child("tipoResiduo").getValue(String.class);
        String filtroTipoResiduo = tipoResiduoInput.getText().toString().toLowerCase();

        boolean coincideTipo = filtroTipoResiduo.isEmpty() || (tipo != null && tipo.toLowerCase().contains(filtroTipoResiduo));

        if (coincideTipo) {
            String fecha = residuoSnapshot.child("fechaHora").getValue(String.class);
            String cantidad = residuoSnapshot.child("cantidad").getValue(String.class);
            String descripcion = residuoSnapshot.child("descripcion").getValue(String.class);
            String estado = residuoSnapshot.child("estadoResiduo").getValue(String.class);
            String registradoPor = residuoSnapshot.child("registradoPor").getValue(String.class);
            String uid = residuoSnapshot.child("uid").getValue(String.class);

            agregarFilaTabla(fecha, tipo, cantidad, descripcion, estado, registradoPor, uid);
            return true;
        }
        return false;
    }

    private void agregarFilaTabla(String fecha, String tipoResiduo, String cantidad, String descripcion, String estado, String registradoPor, String uid) {
        TableRow fila = new TableRow(this);
        TextView txtFecha = new TextView(this);
        TextView txtTipo = new TextView(this);

        txtFecha.setText(fecha);
        txtTipo.setText(tipoResiduo);

        Typeface fuentePersonalizada = ResourcesCompat.getFont(this, R.font.zeloso);
        txtFecha.setTypeface(fuentePersonalizada);
        txtTipo.setTypeface(fuentePersonalizada);

        txtFecha.setTextColor(Color.WHITE);
        txtTipo.setTextColor(Color.YELLOW);
        txtFecha.setTextSize(16);
        txtTipo.setTextSize(16);
        txtFecha.setPadding(8, 8, 8, 8);
        txtTipo.setPadding(8, 8, 8, 8);

        fila.setBackgroundResource(R.drawable.borde_redondeado);
        fila.addView(txtFecha);
        fila.addView(txtTipo);
        fila.setOnClickListener(v -> mostrarDetallesResiduo(fecha, tipoResiduo, cantidad, descripcion, estado, registradoPor, uid));

        tablaResiduos.addView(fila);
    }

    private void mostrarMensajeSinResultados() {
        TableRow filaMensaje = new TableRow(this);
        TextView mensaje = new TextView(this);
        mensaje.setText("No hay registros.");
        mensaje.setTextSize(18);
        mensaje.setTypeface(Typeface.DEFAULT_BOLD);
        mensaje.setTextColor(Color.RED);
        mensaje.setPadding(8, 8, 8, 8);

        filaMensaje.addView(mensaje);
        tablaResiduos.addView(filaMensaje);
    }

    private void mostrarDetallesResiduo(String fecha, String tipoResiduo, String cantidad, String descripcion, String estado, String registradoPor, String uid) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("DETALLES DEL RESIDUO");
        String mensaje = "CANTIDAD: " + cantidad + "\nDESCRIPCION: " + descripcion + "\nESTADO: " + estado +
                "\nFECHA/HORA: " + fecha + "\nREGISTRADO POR: " + registradoPor + "\nTIPO: " + tipoResiduo + "\nUID: " + uid;
        builder.setMessage(mensaje);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void mostrarSelectorDeFecha() {
        // Obtener la fecha actual
        final Calendar calendario = Calendar.getInstance();
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int dia = calendario.get(Calendar.DAY_OF_MONTH);

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                ReporteActivity.this,
                (view, year, month, dayOfMonth) -> {
                    String fechaSeleccionada = dayOfMonth + "/" + (month + 1) + "/" + year;
                    Toast.makeText(ReporteActivity.this, "Hoy es: " + fechaSeleccionada, Toast.LENGTH_SHORT).show();
                },
                año, mes, dia
        );

        datePickerDialog.show();
    }

}







