package com.example.mycompi


import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mycompi.databinding.ActivityMainBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.languageid.LanguageIdentificationOptions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var text : String
    private  var identifier : String = " "
    private var flag = false

    private var imageUri : Uri? = null

    private lateinit var progressDialog: ProgressDialog
    private lateinit var textRecogniser : TextRecognizer
    private lateinit var textRecogniserHindi : TextRecognizer
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



      //  binding.speakButton.setOnClickListener{speakOut()}
        Toast.makeText(this,"If Image is in hindi toggle switch first , படம் ஹிந்தியில் இருந்தால் முதலில் மாறவும்",Toast.LENGTH_LONG).show()
        binding.toggleLanguage.setOnClickListener{
            if(binding.toggleLanguage.isChecked)
            {
                binding.addImage.text = "படத்தைச் சேர்க்கவும்"
                binding.translate.text = "மொழிபெயர்"
              //  binding.recogniseTextButton.text = "உரையை அங்கீகரிக்கவும்"

            }
            else if(!binding.toggleLanguage.isChecked){
                binding.translate.text = "Translate"
                binding.addImage.text = "Add Image"
              //  binding.recogniseTextButton.text = "Text Recognize"
            }
        }

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecogniser = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        textRecogniserHindi = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
        binding.addImage.setOnClickListener {
            showInpuImageDialog()

        }

//        binding.recogniseTextButton.setOnClickListener {
//            if(imageUri == null){
//                Toast.makeText(this,"Check",Toast.LENGTH_SHORT).show()
//            }
//            else{
//                recognizeTextFromImage()
//            }
//        }

        binding.translate.setOnClickListener {
            text = binding.recogniseText.text.toString()
            Log.i(TAG,"Translateduuu : $text")
            val languageIdentifier = LanguageIdentification.getClient(
                LanguageIdentificationOptions.Builder()
                .setConfidenceThreshold(0.2f)
                .build())
            languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener { languageCode ->
                    if (languageCode == "und") {
                        Log.i(TAG, "Can't identify language.")
                    } else {
                        identifier = languageCode
                        if(languageCode.equals("hi")){
                            flag = true
                        }
                        Toast.makeText(this,"Language : / மொழி : $identifier",Toast.LENGTH_LONG).show()
                        if(identifier.equals("hi")){

                            binding.detectedText.text = "Hindi"}
                        else  if(identifier.equals("en")){

                            binding.detectedText.text = "English"}
                        else  if(identifier.equals("fr")){

                            binding.detectedText.text = "French"}
                        else  if(identifier.equals("sp")){

                            binding.detectedText.text = "Spanish"}
                        else  if(identifier.equals("es")){

                            binding.detectedText.text = "Espanol"}
                        else{
                        binding.detectedText.text = identifier}
                        Log.i(TAG, "Language: $identifier")
                        if(text.isNotEmpty()){
                            val options = TranslatorOptions.Builder()
                                .setSourceLanguage(TranslateLanguage.fromLanguageTag(languageCode)!!)
                                .setTargetLanguage(TranslateLanguage.TAMIL)
                                .build()
                            val englishTamilTranslator = Translation.getClient(options)
                            Log.i(TAG, "LanguageTranslator: $englishTamilTranslator")
                            var conditions = DownloadConditions.Builder()
                                .requireWifi()
                                .build()
                            englishTamilTranslator.downloadModelIfNeeded(conditions)
                                .addOnSuccessListener {
                                    englishTamilTranslator.translate(text)
                                        .addOnSuccessListener { translatedText ->
                                            Log.i(TAG,"Translated Language: $translatedText")
                                            binding.translatedText.text = translatedText

                                            // Translation successful.
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(TAG,exception.toString())
                                            // ...
                                        }
                                    // Model downloaded successfully. Okay to start translating.

                                }
                                .addOnFailureListener { exception ->
                                    // Model couldn’t be downloaded or other internal error.
                                    // ...
                                    Log.e(TAG,"Translator Error: ${exception.toString()}")
                                }
                        }



                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this,"Check the text",Toast.LENGTH_SHORT).show()
                }
        }





        /////////////////////////////


        ///////////////////////


    }



    private fun recognizeTextFromImage() {
        progressDialog.setMessage("Image....")
        progressDialog.show()

            if (binding.toggleHindiLanguage.isChecked) {
                Log.d("LanguageIdentified", identifier)
                try {
                    val inputImage = InputImage.fromFilePath(this, imageUri!!)
                    progressDialog.setMessage("Recognizing Text...")
                    val textTaskResult = textRecogniserHindi.process(inputImage)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            val recognizedText = it.text
                            binding.recogniseText.setText(recognizedText)
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Failed to recognise text : ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } catch (e: Exception) {
                    Log.d(TAG, "Error in fetching.... $e")
                }
            }
            else if(!binding.toggleHindiLanguage.isChecked) {
                try {

                    val inputImage = InputImage.fromFilePath(this, imageUri!!)
                    progressDialog.setMessage("Recognizing Text...")
                    val textTaskResult = textRecogniser.process(inputImage)
                        .addOnSuccessListener {
                            progressDialog.dismiss()
                            val recognizedText = it.text
                            binding.recogniseText.setText(recognizedText)
                        }
                        .addOnFailureListener { e ->
                            progressDialog.dismiss()
                            Toast.makeText(
                                this,
                                "Failed to recognise text : ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                } catch (e: Exception) {
                    Log.d(TAG, "Error in fetching.... $e")
                }
            }


    }


    private fun showInpuImageDialog() {
        val popMenu = PopupMenu(this,binding.addImage)
        popMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popMenu.menu.add(Menu.NONE,2,2,"Gallery")

        popMenu.show()

        popMenu.setOnMenuItemClickListener { menuItem ->
            val id = menuItem.itemId
            if(id == 1){
//                if(checkCameraPermission()){
//                    pickImageCamera()
//                }
//                else{
//                    requestCameraPermission()
//                }
                ImagePicker.with(this).cameraOnly().crop().start()

            }
            else if(id == 2){
//                if(checkStoragePermission()){
//                    pickFromGallery()
//                }
//                else{
//                    requestStoragePermission()
//                }
                ImagePicker.with(this).galleryOnly().galleryMimeTypes(arrayOf("image/*")).crop().start()

            }
            return@setOnMenuItemClickListener true

        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == ImagePicker.REQUEST_CODE){
            imageUri = data?.data
            binding.ImageRecognistion.setImageURI(data?.data)
            if(imageUri == null){
                Toast.makeText(this,"Check",Toast.LENGTH_SHORT).show()
            }
            else{
                recognizeTextFromImage()
            }
        }
    }




}