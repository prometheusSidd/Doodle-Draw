package com.example.drawingapp

// 1. First Create a Kotlin file
import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yuku.ambilwarna.AmbilWarnaDialog
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random
import java.util.*



class MainActivity : AppCompatActivity() , View.OnClickListener { // After implementing View.OnCLickListener : override fun onCLick() created below

    // Creating a variable
    private lateinit var drawingView : DrawingView
    private lateinit var brushButton: ImageButton

    // Adding variable for colored button
    private lateinit var purple : ImageButton
    private lateinit var red : ImageButton
    private lateinit var blue : ImageButton
    private lateinit var green : ImageButton
    private lateinit var orange : ImageButton
    private lateinit var undo : ImageButton
    private lateinit var color_pick : ImageButton
    private lateinit var gallery : ImageButton

    // Declaring variable to pick images from gallery for Users ::******
    private val galleryLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result -> findViewById<ImageView>(R.id.gallery_image).setImageURI(result.data?.data)
    }//****** , Next Go to requestPermission(). 14

    private lateinit var save : ImageButton
    private lateinit var constraint : ConstraintLayout// 15. Next , We add Save image feature for Users *** :::::




    // 16. Creating a Variable for getting External permission from User ***** ::::::::
    val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
                permissions -> permissions.entries.forEach{
            val permissionName = it.key
            val isGranted = it.value

            if (isGranted && permissionName == android.Manifest.permission.READ_MEDIA_IMAGES){
                Toast.makeText(this,"Permission Granted",Toast.LENGTH_SHORT).show()

                // 14. feature to select images from gallery for Users :: *
                val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryLauncher.launch(pickIntent) //**  Next go inside onClick() below

            } else if(isGranted && (permissionName == Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                CoroutineScope(IO).launch{
                    saveImage(getImageBitmapformView(findViewById(R.id.constraint_l1)))

                }


            }
            else{
                if (permissionName == android.Manifest.permission.READ_MEDIA_IMAGES){
                    Toast.makeText(this,"Permission denied", Toast.LENGTH_SHORT).show()
                }
            } /// **** ::::::::: Also go to Manifest file to Declare permission *** :::::::
        }
        }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 13. Connecting Color with Buttons ::
        red = findViewById(R.id.red_button)
        green =  findViewById(R.id.green_button)
        blue =  findViewById(R.id.blue_button)
        purple =  findViewById(R.id.purple_button)
        orange =  findViewById(R.id.orange_button) // Next , setClickListner() for Each button also
        undo = findViewById(R.id.undo_button)
        color_pick = findViewById(R.id.color_picker_button)
        save = findViewById(R.id.save_button)
        gallery = findViewById(R.id.gallery_button) // After adding gallery , This process need to take internal permission for getting access to User External storage.
        constraint = findViewById(R.id.constraint_l1)


// 7.
        drawingView = findViewById(R.id.drawing_view)  //
        drawingView.changeBrushSize(24.toFloat())

        brushButton = findViewById(R.id.brush_button)

        brushButton.setOnClickListener { // 10. making clickable
            showBrushChooserDialog()
        }
        red.setOnClickListener (this)
        green.setOnClickListener (this)
        blue.setOnClickListener (this)
        purple.setOnClickListener (this)
        orange.setOnClickListener (this)
        undo.setOnClickListener  (this)
        color_pick.setOnClickListener (this)
        gallery.setOnClickListener (this) // 14. feature to select images from gallery for Users :: *
        save.setOnClickListener{
            val bitmap = getImageBitmapformView(constraint)
            lifecycleScope.launch{
                saveImage(bitmap)
            }

        }  // 15. Next , We add Save image feature for Users *** :::::
        }
    // 8.*** Next we Going to import a seekBar to manually change our BrushSize , Create a layout resource file 'dialog_brush' And Root= "Linearlayout.compat"

    // 10.
    private fun showBrushChooserDialog(){
        val brushDialog = Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_seek_bar)
        val showProgressTv = brushDialog.findViewById< TextView>(R.id.dialog_text_view_progress)
        seekBarProgress.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                drawingView.changeBrushSize(seekBar.progress.toFloat())

                showProgressTv.text = seekBar.progress.toString()

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        brushDialog.show() // Next , We add different buttons for ddifferent functionality :::******* \
        // 11. Create a " <Constraintlayout/> " or Other required functions in activity_main.xml
    }

    // 13. Here we add or changed color of brush :::
    override fun onClick(view: View?) {
        when(view?.id){
            R.id.purple_button -> {
                drawingView.setColor("#8F33FF")
            }
            R.id.red_button -> {
                drawingView.setColor("#FF0303")
            }
            R.id.green_button -> {
                drawingView.setColor("#56F805")
            }
            R.id.blue_button -> {
                drawingView.setColor("#0233ED")
            }
            R.id.orange_button -> {
                drawingView.setColor("#FF8B03")
            } // 14. Next , We add Undo in DrawingView.kt

            R.id.undo_button -> {
                drawingView.setUndo()
            } // 15. Next , We add color_picker in DrawingView.kt using
            R.id.color_picker_button -> {
                showColorPick()
            } //15 . We have to add color_picker dialogue in build.gradle.kts -> implementation 'com.github.yukuku:ambiwarna:2.0.1'
            R.id.gallery_button ->{
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED){
                    externalPermission() // from below
                }else{
                    // 14. feature to select images from gallery for Users :: *
                    val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    galleryLauncher.launch(pickIntent) //**

                    // 15. Next , We add Save image feature for Users *** :::::
                }

            }
            R.id.save_button -> {

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    externalPermission()
                }
                else {
                    val layout = findViewById<ConstraintLayout>(R.id.constraint_l1)
                    val bitmap = getImageBitmapformView(layout)

                    CoroutineScope(IO).launch {
                    saveImage(bitmap) // Go inside saveImage
                }
                }

            }
        }

    }

    private fun showColorPick(){
        val dialog = AmbilWarnaDialog(this,Color.GREEN,object : AmbilWarnaDialog.OnAmbilWarnaListener {
            override fun onCancel(dialog: AmbilWarnaDialog?) {

            }

            override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                drawingView.setColor(color)
            }
        })
        dialog.show()
    }

    private fun externalPermission(){ // We have to show Why the Permission is needed ***** :::::
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){
            showRationalwhy()
        }else{
            requestPermission.launch(
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
        }
    }

    private fun showRationalwhy(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage Permission")
            .setMessage("We need this permission in order to access the internal storage ")
            .setPositiveButton(R.string.dialog_yes) {
                dialog, _ ->
                requestPermission.launch(
                    arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                )
                dialog.dismiss()
            }
        builder.create().show()
    }
     // 14. Next , We Add feature to Add gallery image of user :: *** by creating a Varuble "galleryLauncher"

    // 15. Next , We add Save image feature for Users *** :::::

    // Saving Image takes two function (i).getImageBitmapfromView() (ii). saveImage()
    // . (i) getImageBitmpafromView() take view object as parameter it creates a 'Bitmap' object with dimension of 'view' by calling 'bitmap.createBitmap()'
    //   the created Bitmap has configuration of 'Bitmap.Config.ARGB_8888' then '(bitmap)' is created , the 'draw' method is called on 'view' passing the 'canvas' object as parameter

    // . (ii)



    private fun getImageBitmapformView(view:View):Bitmap{
        val bitmap = Bitmap.createBitmap(view.width,view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    private suspend fun saveImage(bitmap: Bitmap){
        val root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val myDir = File("$root/saved_images")
        myDir.mkdir()
        val generator = java.util.Random()
        var n = 10000
        n = generator.nextInt()
        val outputFile = File(myDir,"Images-$n.jpg")
        if(outputFile.exists()){
            outputFile.delete()
        }else{
            try {
                val out = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,out)
                out.flush()
                out.close()

            }


            catch (e:Exception){
                e.stackTrace

            }
            // Here we using Little Coroutine for /IO bound activity
            withContext(Main){
                Toast.makeText(this@MainActivity,"${outputFile.absolutePath} saved !!! ", Toast.LENGTH_SHORT).show()

            }

        }
    }


}
/// HERE PROJECTS END //////////// ****************************