package com.frias.omar.daniel.scraper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    // Para enlazar elementos en vista
    private Chronometer chTiempo;
    private TextView Golpes;
    private FloatingActionButton fabContar;
    private Button btnReiniciar, btnCalcularTiempo, btnSalir;


    // Lleva la cuenta de golpes
    private int contador;

    // Indica si está corriendo el tiempo
    private boolean tiempoCorriendo;
    // Indica si contó el primer toque
    private boolean primeraVez;
    // Indica si está hailitado el boton +
    private boolean contadorHabilitado;

    // Para pregunta de confirmación
    AlertDialog.Builder alertDialogBuilder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        // Enlazo los elementos de la vista
        this.chTiempo = findViewById(R.id.chTiempo);
        this.Golpes = findViewById(R.id.Golpes);
        this.fabContar = findViewById(R.id.fabContar);
        this.btnReiniciar = findViewById(R.id.btnReiniciar);
        this.btnCalcularTiempo = findViewById(R.id.btnCalcularTiempo);
        this.btnSalir = findViewById(R.id.btnSalir);

        // Formateo el cronómetro a mm:ss
        this.chTiempo.setFormat("%s");

        // Para ésto agregar implements View.OnClickListener a la clase principal
        this.fabContar.setOnClickListener(this);
        this.btnReiniciar.setOnClickListener(this);
        this.btnCalcularTiempo.setOnClickListener(this);
        this.btnSalir.setOnClickListener(this);

        // Inicia la cuenta en 0
        this.contador = 0;
        this.chTiempo.setBase(SystemClock.elapsedRealtime());

        // No está contando inicialmente
        this.tiempoCorriendo = false;
        // No contó por primera vez
        this.primeraVez = false;
        // El boton + está habilitado
        this.contadorHabilitado = true;

        // Cuando llega al minuto se detiene
        this.chTiempo.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if ((SystemClock.elapsedRealtime() - chTiempo.getBase()) >= 60000) {
                    chTiempo.stop();
                    contadorHabilitado = false;
                    Toast.makeText(MainActivity.this, "Tiempo cumplido", Toast.LENGTH_LONG).show();
                }
            }
        });

    }


    /************************************************************************************/
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.fabContar:
                if (this.contadorHabilitado){
                    if (!this.tiempoCorriendo){
                        // Cuenta por primera vez
                        iniciarConteo();
                    }
                    if( this.tiempoCorriendo &&
                            (SystemClock.elapsedRealtime() - chTiempo.getBase()) <= 60000 &&
                            this.primeraVez ) {
                        // Sigue contando
                        this.contador ++;
                        this.Golpes.setText("" + this.contador);
                    }
                    this.primeraVez = true;
                }else {
                    Toast.makeText(MainActivity.this, "Reinicia para contar", Toast.LENGTH_LONG).show();
                }
                break;

            case R.id.btnReiniciar:
                reiniciar();
                break;

            case R.id.btnCalcularTiempo:
                Intent intent = new Intent(this.context, CalcularActivity.class);
                // Envío la cantidad de golpes contados
                intent.putExtra("numGolpes", this.contador);
                startActivity(intent);
                break;

            case R.id.btnSalir:
                cerrarApp();
                break;

        }
    }
    /************************************************************************************/


    /** Empieza a correr cronómetro y a contar toques al fabContar **********************/
    private void iniciarConteo() {
        // Inicio en 1 la cuenta
        this.contador ++;
        // Seteo el valor del cronómetro
        this.chTiempo.setBase(SystemClock.elapsedRealtime());
        // Empieza a correr el tiempo del cronómetro
        this.chTiempo.start();
        this.tiempoCorriendo = true;
        // Actualizo el texto en Golpes contados
        this.Golpes.setText("" + this.contador);
    }
    /************************************************************************************/


    /** Reinicio el cronómetro y el contador de golpes *********************************/
    private void reiniciar(){
        this.chTiempo.stop();
        this.chTiempo.setBase(SystemClock.elapsedRealtime());
        this.contador = 0;
        this.Golpes.setText("" + this.contador);
        this.primeraVez = false;
        this.tiempoCorriendo = false;
        this.contadorHabilitado = true;
    }
    /************************************************************************************/


    /** Cierra la aplicación con mensaje de confirmación ********************************/
    private void cerrarApp(){
        // crea alert dialog
        createDialog(this);
        AlertDialog alertDialog = alertDialogBuilder.create();
        // muestra
        alertDialog.show();
    }
    /************************************************************************************/


    /** Pop up de confirmacion **********************************************************/
    private void createDialog(Context context){
        alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("¿Desea salir?");
        //alertDialogBuilder.setMessage("¿Desea salir?");
        alertDialogBuilder.setCancelable(true);
        // setea popup
        alertDialogBuilder.setPositiveButton("Si", new
                DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        startActivity(new Intent(getBaseContext(), MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //cancela
            }
        });
    }
    /************************************************************************************/


    /** Creo el menú ********************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflo o muestro el menu creado
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // acciono según el item seleccionado
        switch (item.getItemId()){
            case R.id.mAyuda:
                startActivity(new Intent(this.context, Ayuda.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    /************************************************************************************/

}
