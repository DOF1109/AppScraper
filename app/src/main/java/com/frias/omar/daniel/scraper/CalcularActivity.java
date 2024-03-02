package com.frias.omar.daniel.scraper;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class CalcularActivity extends AppCompatActivity implements View.OnClickListener {

    private Context context;

    // Para enlazar elementos en vista
    private EditText eGolpes, etPkA, etMetrosA, etPkB, etMetrosB;
    private TextView tpHoraA, tvHoraCalculada, tvVelocidadCalculada;
    private Button btnCalcular;

    // Para obtener los golpes contados en actividad anterior
    private int golpesContados;

    // Para obtener horas y minutos ingresador por usuario
    private int horasPuntoA,minutosPuntoA;

    // Para obtener los puntos A y B ingresados por usuario
    private int pkA, metrosA, pkB, metrosB;

    // Para unificar las PKs con metros
    private double puntoA, puntoB;

    // Longitud del caño en metros
    private double longitudCano = 12;

    // Contempla los factores reales
    private double margenError = 0.5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calcular);

        this.context = getApplicationContext();

        // Enlazo los elementos de la vista
        this.eGolpes = findViewById(R.id.eGolpes);
        this.etPkA = findViewById(R.id.etPkA);
        this.etMetrosA = findViewById(R.id.etMetrosA);
        this.tpHoraA = findViewById(R.id.tpHoraA);
        this.etPkB = findViewById(R.id.etPkB);
        this.etMetrosB = findViewById(R.id.etMetrosB);
        this.tvHoraCalculada = findViewById(R.id.tvHoraCalculada);
        this.tvVelocidadCalculada = findViewById(R.id.tvVelocidadCalculada);
        this.btnCalcular = findViewById(R.id.btnCalcular);

        // Obtengo la cantidad de golpes contados previamente
        this.golpesContados = getIntent().getExtras().getInt("numGolpes");
        this.eGolpes.setText("" + this.golpesContados);

        // Todavia no ingresó el horario
        this.horasPuntoA = 0;
        this.minutosPuntoA = 0;

        // Para seleccionar el texto al tocar el campo
        this.eGolpes.setSelectAllOnFocus(true);
        this.etPkA.setSelectAllOnFocus(true);
        this.etMetrosA.setSelectAllOnFocus(true);
        this.etPkB.setSelectAllOnFocus(true);
        this.etMetrosB.setSelectAllOnFocus(true);

        // Para ésto agregar implements View.OnClickListener a la clase principal
        this.tpHoraA.setOnClickListener(this);
        this.btnCalcular.setOnClickListener(this);
    }


    /** Obtengo la hora mediante un TimePicker ******************************************/
    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            // Obtengo la hora del Punto A
            case R.id.tpHoraA:
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, R.style.Theme_AppCompat_Light_Dialog, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        horasPuntoA = hourOfDay;
                        minutosPuntoA = minute;
                        // Cargo la vista
                        if (hourOfDay < 10 && minute < 10){
                            tpHoraA.setText("0" + hourOfDay + ":0" + minute);
                        }else if (hourOfDay < 10){
                            tpHoraA.setText("0" + hourOfDay + ":" + minute);
                        }else if (minute < 10){
                            tpHoraA.setText("" + hourOfDay + ":0" + minute);
                        }else {
                            tpHoraA.setText("" + hourOfDay + ":" + minute);
                        }
                    }
                },this.horasPuntoA, this.minutosPuntoA,true);
                timePickerDialog.show();
                break;

            // Calculo la hora de llegada al Punto B
            case R.id.btnCalcular:
                // Verifico si tengo cargado un error en Punto B
                if (this.etMetrosB.getError() != null){
                    this.etMetrosB.setError(null);
                }
                aceptarCampos();
                break;

        }
    }
    /************************************************************************************/


    /** Eecutamos al presionar el boton CALCULAR ****************************************/
    private void aceptarCampos(){
        obtenerCampos();
        if (!validarCampos()){
            Toast.makeText(this.context, "Ingrese los datos correctamente", Toast.LENGTH_LONG).show();
        }else{
            calcularHoraLlegada();
        }
    }
    /************************************************************************************/


    /** Quarda los datos ingresados por el usuario **************************************/
    private void obtenerCampos(){
        // Obtengo los campos convertidos en "int"
        this.golpesContados = Integer.parseInt(this.eGolpes.getText().toString());
        this.pkA = Integer.parseInt(this.etPkA.getText().toString());
        this.metrosA = Integer.parseInt(this.etMetrosA.getText().toString());
        this.pkB = Integer.parseInt(this.etPkB.getText().toString());
        this.metrosB = Integer.parseInt(this.etMetrosB.getText().toString());
    }
    /************************************************************************************/


    /** Verifica que el Punto B sea mayor que el Punto A y que haya golpes **************/
    private boolean validarCampos(){
        boolean estado = true;

        if (this.golpesContados == 0) {
            this.eGolpes.setError("No está moviendose el scraper");
            estado = false;
        }

        // Unifico la PK con los metros extra
        this.puntoA = this.pkA + (this.metrosA / 1000);
        this.puntoB = this.pkB + (this.metrosB / 1000);

        if (this.puntoB <= this.puntoA) {
            this.etMetrosB.setError("El Punto B debe ser mayor que el A");
            estado = false;
        }

        return estado;
    }
    /************************************************************************************/


    /** Calculo la velocidad a la que se mueve el scraper en km/h
     * (velocidad=distancia/timepo) y muestro la velocidad teórica */
    private double obtenerVelocidadReal(int golpes){

        double velocidad;
        // Obtengo la longitud del caño de metros a kilometros
        // Aplico el MARGEN DE ERROR
        double longitudKm = ( this.longitudCano - this.margenError) / 1000;
        // Obtengo el timepo en horas a partir de 1 minuto de conteo
        double tiempoHs = 0.0167;

        double velocidadTeorica = golpes * ( (this.longitudCano/1000) / tiempoHs );
        // Muestro la velocidad teórica
        this.tvVelocidadCalculada.setText(String.format("%.2f", velocidadTeorica) + " km/h");

        velocidad = golpes * ( longitudKm / tiempoHs );
        return velocidad;
    }
    /************************************************************************************/


    /** Calcula y muestra la hora de llegada ********************************************/
    private void calcularHoraLlegada(){
        // Obtengo la velocidad del scraper
        double velocidad = obtenerVelocidadReal(this.golpesContados);

        // Obtengo la distancia a recorrer
        double distancia = this.puntoB - this.puntoA;

        double timepo = distancia / velocidad;

        // El valor entero de "tiempo" son las horas
        int horasFaltantes = (int) timepo;

        // A los decimales de "timepo" los multiplico por 60 y
        // la parte entera de ese número son los minutos
        int minutosFaltantes = (int) ( (timepo - horasFaltantes) * 60 );


        /** Sumo las horas y minutos faltantes a la hora cargada por usuario */
        // Calculo los minutos de llegada
        int minutosLlegada = this.minutosPuntoA + minutosFaltantes;
        if (minutosLlegada >= 60){
            horasFaltantes ++;
            minutosLlegada = minutosLlegada - 60;
        }
        // Calculo la hora de llegada
        int horaLlegada = this.horasPuntoA + horasFaltantes;

        // Verifico si se pasó de 24hs para expresar en días
        if (horaLlegada >= 24){
            // Obtengo la cantidad de días que tardará
            int dias = (int) (horaLlegada / 24);
            // Obtengo la cantidad de horas además de los días faltantes
            horaLlegada = horaLlegada - (dias * 24);

            if (dias == 1){
                // Cargo la vista con pasaje hasta el día siguiente
                if (horaLlegada < 10 && minutosLlegada < 10){
                    this.tvHoraCalculada.setText("0" + horaLlegada + ":0" + minutosLlegada +
                            "  mañana aprox.");
                }else if (horaLlegada < 10){
                    this.tvHoraCalculada.setText("0" + horaLlegada + ":" + minutosLlegada +
                            "  mañana aprox.");
                }else if (minutosLlegada < 10){
                    this.tvHoraCalculada.setText("" + horaLlegada + ":0" + minutosLlegada +
                            "  mañana aprox.");
                }else {
                    this.tvHoraCalculada.setText("" + horaLlegada + ":" + minutosLlegada +
                            "  mañana aprox.");
                }
            }else {
                // Cargo la vista con pasaje de 2 días o más
                if (horaLlegada < 10 && minutosLlegada < 10){
                    this.tvHoraCalculada.setText("0" + horaLlegada + ":0" + minutosLlegada +
                            "  +" + dias + " dias aprox.");
                }else if (horaLlegada < 10){
                    this.tvHoraCalculada.setText("0" + horaLlegada + ":" + minutosLlegada +
                            "  +" + dias + " dias aprox.");
                }else if (minutosLlegada < 10){
                    this.tvHoraCalculada.setText("" + horaLlegada + ":0" + minutosLlegada +
                            "  +" + dias + " dias aprox.");
                }else {
                    this.tvHoraCalculada.setText("" + horaLlegada + ":" + minutosLlegada +
                            "  +" + dias + " dias aprox.");
                }
            }
        }else {
            // Cargo la vista con pasaje sólo en horas
            if (horaLlegada < 10 && minutosLlegada < 10){
                this.tvHoraCalculada.setText("0" + horaLlegada + ":0" + minutosLlegada + "  aprox.");
            }else if (horaLlegada < 10){
                this.tvHoraCalculada.setText("0" + horaLlegada + ":" + minutosLlegada + "  aprox.");
            }else if (minutosLlegada < 10){
                this.tvHoraCalculada.setText("" + horaLlegada + ":0" + minutosLlegada + "  aprox.");
            }else {
                this.tvHoraCalculada.setText("" + horaLlegada + ":" + minutosLlegada + "  aprox.");
            }
        }

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
                startActivity(new Intent(this.context, AyudaCalcularActivity.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
    /************************************************************************************/


}
