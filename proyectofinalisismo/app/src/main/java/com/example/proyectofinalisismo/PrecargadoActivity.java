package com.example.proyectofinalisismo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.animation.ObjectAnimator;

public class PrecargadoActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precargado);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Animación del logo
        ImageView logo = findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_scale);
        logo.startAnimation(animation);

        // Animación del carrito
        LottieAnimationView carritoAnimado = findViewById(R.id.carritoAnimado);
        float screenWidth = getResources().getDisplayMetrics().widthPixels;

        ObjectAnimator animationCarrito = ObjectAnimator.ofFloat(carritoAnimado, "translationX", -300f, screenWidth + 300f);
        animationCarrito.setDuration(5000);
        animationCarrito.start();

        animationCarrito.addListener(new android.animation.Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(android.animation.Animator animator) {
                carritoAnimado.setTranslationX(screenWidth + 300f); // Mantiene el carrito oculto a la derecha
            }

            @Override
            public void onAnimationStart(android.animation.Animator animator) { }
            @Override
            public void onAnimationCancel(android.animation.Animator animator) { }
            @Override
            public void onAnimationRepeat(android.animation.Animator animator) { }
        });

        // Verificar si la sesión está activa o cerrada
        boolean cerrarSesion = getIntent().getBooleanExtra("cerrarSesion", false);
        new Handler().postDelayed(() -> {
            FirebaseUser user = mAuth.getCurrentUser();
            if (cerrarSesion || user == null) {
                // Si se presionó 'Cerrar sesión' o no hay usuario autenticado
                startActivity(new Intent(PrecargadoActivity.this, LoginActivity.class));
            } else {
                // Si la sesión sigue activa
                startActivity(new Intent(PrecargadoActivity.this, MenuActivity.class));
            }
            finish();
        }, 4000); // 4 segundos para la precarga
    }
}










