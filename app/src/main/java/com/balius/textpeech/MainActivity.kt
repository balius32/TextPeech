package com.balius.textpeech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.balius.textpeech.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private lateinit var binding: ActivityMainBinding

    private val handler = Handler()
    private var dotCount = 0
    private val maxDots = 3

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null

    private var selectedLanguage = "en" // Default "en selected"
    private var isListening = false // وضعیت گوش دادن

    private var resultText =""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        setListeners()
        checkPermissions()
        resetSpeechRecognizer()
        setRecogniserIntent()
        prepareLocales()
    }

    private fun setListeners() {
        binding.btnStartListen.setOnClickListener {
            Log.e("setListeners()", "setListeners()")
            handler.post(updateTextRunnable)
            binding.tvListening.visibility = View.VISIBLE
            if (!isListening) {
                startListening()
            }
        }

        binding.btnPause.setOnClickListener {
            Log.e("setListeners()", "btn Pause Listening")
            handler.removeCallbacks(updateTextRunnable)
            binding.tvListening.visibility = View.GONE
            if (isListening) {
                stopListening()
            }
        }


        binding.imgDelete.setOnClickListener {
            resultText = ""
            binding.textView1.text = resultText
        }
    }


    private fun resetSpeechRecognizer() {
        Log.e("resetSpeechRecognizer()", "resetSpeechRecognizer()")
        if (speechRecognizer != null) speechRecognizer!!.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(mContext)
        errorLog(
            "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(mContext)
        )
        if (SpeechRecognizer.isRecognitionAvailable(mContext))
            speechRecognizer!!.setRecognitionListener(mRecognitionListener)
        else finish()
    }

    private fun setRecogniserIntent() {
        Log.e("setRecogniserIntent()", "setRecogniserIntent()")
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            selectedLanguage
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            selectedLanguage
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        }


    private fun startListening() {
        isListening = true
        speechRecognizer!!.startListening(recognizerIntent)

        Log.e("startListening()", "startListening()")
    }

    private fun stopListening() {
        isListening = false
        speechRecognizer!!.stopListening()


        Log.e("stopListening()", "stopListening()")
    }

    public override fun onResume() {
        errorLog("resume")
        super.onResume()
        resetSpeechRecognizer()

    }

    override fun onPause() {
        errorLog("pause")
        super.onPause()
        Log.e("onPause", "onPause")
        speechRecognizer!!.stopListening()
    }

    override fun onStop() {
        errorLog("stop")
        super.onStop()
        if (speechRecognizer != null) {
            speechRecognizer!!.destroy()
        }
    }



    private val mRecognitionListener = object : RecognitionListener {
        override fun onBeginningOfSpeech() {
            errorLog("onBeginningOfSpeech")
            /*   binding.progressBar1.isIndeterminate = false
               binding.progressBar1.max = 10*/
        }

        override fun onBufferReceived(buffer: ByteArray) {
            errorLog("onBufferReceived: $buffer")
        }

        override fun onEndOfSpeech() {
            errorLog("onEndOfSpeech")
            //   binding.progressBar1.isIndeterminate = true
        }

        override fun onResults(results: Bundle) {
            errorLog("onResults")
            val matches: ArrayList<String>? = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
             //= binding.textView1.text.toString() // Get current text
            Log.e("result", matches.toString())
            Log.e("result2", matches?.size.toString())

            Log.e("result3", matches?.get(0).toString())
            resultText += " ${matches?.get(0)}"

            binding.textView1.text = resultText // Update the TextView
            if (isListening) {
                resetSpeechRecognizer()
                startListening()
            }
        }
        override fun onPartialResults(partialResults: Bundle) {
             errorLog("onPartialResults")
           /* val matches: ArrayList<String>? = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            var partialText = binding.textView1.text.toString()

            partialText += " ${matches?.get(0)}"

          *//*  for (result in matches!!) {
                Log.e("Presult", result)
                partialText += " $result"
                Log.e("Presult2", result)
            }*//*
            binding.textView1.text = partialText */// Update the TextView
        }

        override fun onError(errorCode: Int) {
            val errorMessage = getErrorText(errorCode)
            errorLog("FAILED $errorMessage")
            binding.tvError.text = errorMessage

            if (isListening) {
                resetSpeechRecognizer()
                startListening()
            }
        }

        override fun onEvent(arg0: Int, arg1: Bundle) {
            errorLog("onEvent")
        }



        override fun onReadyForSpeech(arg0: Bundle) {
            errorLog("onReadyForSpeech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            //binding.progressBar1.progress = rmsdB.toInt()
        }
    }

    //for txt listening
    private val updateTextRunnable = object : Runnable {
        override fun run() {
            dotCount = (dotCount + 1) % (maxDots + 1)
            val dots = ".".repeat(dotCount)
            binding.tvListening.text = "Listening$dots"
            handler.postDelayed(this, 500) // Update every 500 milliseconds
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST_RECORD_AUDIO = 1
        private const val RESULTS_LIMIT = 5
        private const val IS_CONTINUES_LISTEN = true
    }


    //for permissions
    private fun checkPermissions() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSIONS_REQUEST_RECORD_AUDIO
            )
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startListening()
            } else {
                Toast.makeText(mContext, "Permission Denied!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //for lang
    private fun prepareLocales() {
        val availableLocales = Locale.getAvailableLocales()

        val adapterLocalization: ArrayAdapter<Any?> = ArrayAdapter<Any?>(
            mContext,
            android.R.layout.simple_spinner_item,
            availableLocales
        )
        adapterLocalization.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinner1.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedLanguage = availableLocales[position].toString()

                resetSpeechRecognizer()
                setRecogniserIntent()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // TODO
            }
        }

        binding.spinner1.adapter = adapterLocalization

        // Set "en" as selected language by default
        for (i in availableLocales.indices) {
            val locale = availableLocales[i]
            if (locale.toString().equals("en", true)) {
                binding.spinner1.setSelection(i)
                break
            }
        }
    }

}