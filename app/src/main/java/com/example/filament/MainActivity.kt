package com.example.filament

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class MainActivity : AppCompatActivity() {

    var surfaceView: SurfaceView? = null
    var customViewer: CustomViewer = CustomViewer()

    //Variables Traductor
    private lateinit var editText: EditText
    private lateinit var button: Button
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        surfaceView = findViewById<View>(R.id.surface_view) as SurfaceView
        customViewer.run {
            loadEntity()
            setSurfaceView(requireNotNull(surfaceView))

            //directory and model each as param
            loadGlb(this@MainActivity, "grogu", "robot")
            //loadGltf(this@MainActivity, "warcraft", "scene");


            //directory and model as one
            //loadGlb(this@MainActivity, "grogu/grogu");

            //Enviroments and Lightning (OPTIONAL)
            loadIndirectLight(this@MainActivity, "venetian_crossroads_2k")
            //loadEnviroment(this@MainActivity, "venetian_crossroads_2k");

        }
        //Instancias Traductor
        editText = findViewById(R.id.editText)
        button = findViewById(R.id.button)
        textView = findViewById(R.id.textView)

        button.setOnClickListener {
            if (editText.text.toString().trim().isNotEmpty()) {
                val text = editText.text.toString()
                makePythonAPIRequest(text)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        customViewer.onResume()
    }

    override fun onPause() {
        super.onPause()
        customViewer.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        customViewer.onDestroy()
    }

    //Métodos del Traductor

    private fun makePythonAPIRequest(text: String) {
        // Ejecuta la corrutina en el hilo principal de la UI utilizando el GlobalScope
        GlobalScope.launch {
            val result = doInBackgroundAsync(text)
            // Actualizar la UI en el hilo principal utilizando withContext
            withContext(Dispatchers.Main) {
                handleResult(result)
            }
        }
    }

    private suspend fun doInBackgroundAsync(text: String): String =
        withContext(Dispatchers.IO) {
            val url = URL("http://10.0.2.2:5000/analizar?text=$text")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            reader.close()
            stringBuilder.toString()
        }

    private fun handleResult(result: String) {
        var mejorClave = ""
        var similitudMaxima = ""

        val datos = result.split("_")
        if (datos.size >= 2) {
            mejorClave = datos[0]
            similitudMaxima = datos[1]
        }

        // Mostrar el resultado en el consola
        println("Coincide con: $mejorClave Similitud: $similitudMaxima")
        // Mostrar el resultado en el TextView (en el hilo principal)
        textView.text = "Coincide con: $mejorClave Similitud: $similitudMaxima"

        customViewer.crossFadeAnimations(11, 20f, 0f)




    }
}