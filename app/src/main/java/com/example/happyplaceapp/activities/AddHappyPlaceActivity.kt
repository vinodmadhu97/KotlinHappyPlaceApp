package com.example.happyplaceapp.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.happyplaceapp.R
import com.example.happyplaceapp.database.DatabaseHelper
import com.example.happyplaceapp.models.HappyPlaceModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private val cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var savedImagePath : Uri? = null
    private var mLatitude : Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlaceDetails : HappyPlaceModel? = null

    companion object{
        private const val GALLERY = 1
        private const val CAMERA = 2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTO_COMPLETE_REQUEST_CODE = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_place)

        setSupportActionBar(add_happy_place_tool_bar)
        val actionBar = supportActionBar
        actionBar?.title = "Add Place"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        add_happy_place_tool_bar.setNavigationOnClickListener {
            onBackPressed()
        }
        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,"AIzaSyBT3LinjjNCEcPUGtTi0VyDRBmgiXGxydY")
        }

        if (intent.hasExtra(MainActivity.HAPPY_PLACE_DETAILS)){
            mHappyPlaceDetails = intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAILS) as HappyPlaceModel?
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if (mHappyPlaceDetails != null){
            supportActionBar?.title = "Edit Happy Place"
            et_title.setText(mHappyPlaceDetails!!.title)
            et_description.setText(mHappyPlaceDetails!!.description)
            et_date.setText(mHappyPlaceDetails!!.date)
            et_location.setText(mHappyPlaceDetails!!.location)
            mLatitude = mHappyPlaceDetails!!.latitude
            mLongitude = mHappyPlaceDetails!!.longitude
            savedImagePath = Uri.parse(mHappyPlaceDetails!!.image)

            iv_select_img.setImageURI(savedImagePath)
            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)
        btn_add_img.setOnClickListener(this)
        et_location.setOnClickListener(this)
        btn_save.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.et_date -> {
                DatePickerDialog(
                    this
                    ,dateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                ).show()

            }

            R.id.btn_add_img ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select a Action")
                val picDialogItem = arrayOf("Select a photo from gallery","take photo from camera")

                pictureDialog.setItems(picDialogItem){
                    dialog,which ->
                        when(which){
                            0-> chooseImageFromGallery()
                            1->choosePhotoFromCamera()
                        }

                }

                pictureDialog.show()
            }
            R.id.btn_save ->{
                when{
                    et_title.text.isNullOrEmpty() ->{
                        Toast.makeText(this,"please add the title",Toast.LENGTH_LONG).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"please add the description",Toast.LENGTH_LONG).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this,"please add the location",Toast.LENGTH_LONG).show()
                    }
                    savedImagePath == null->{
                        Toast.makeText(this,"please add the image",Toast.LENGTH_LONG).show()
                    }
                    else ->{
                        val happyPlaceModel = HappyPlaceModel(
                            if (mHappyPlaceDetails == null) 0 else mHappyPlaceDetails!!.id,
                            et_title.text.toString(),
                            savedImagePath.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            mLatitude,
                            mLongitude
                        )

                        val dbHandler = DatabaseHelper(this)
                        if (mHappyPlaceDetails == null){

                            val result = dbHandler.addHappyPlaces(happyPlaceModel)
                            if (result>0){
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this,"data added",Toast.LENGTH_LONG).show()

                            }else{
                                Toast.makeText(this,"adding failed",Toast.LENGTH_LONG).show()
                            }
                        }else{
                            val result = dbHandler.updateHappyPlaces(happyPlaceModel)
                            if (result>0){
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this,"data updated",Toast.LENGTH_LONG).show()

                            }else{
                                Toast.makeText(this,"adding updated",Toast.LENGTH_LONG).show()
                            }
                        }




                        finish()
                    }
                }
            }
            R.id.et_location ->{
                try {
                    val fields = listOf(Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,Place.Field.ADDRESS)

                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields).build(this@AddHappyPlaceActivity)
                    startActivityForResult(intent, PLACE_AUTO_COMPLETE_REQUEST_CODE)
                }catch (e:Exception){

                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == GALLERY){
                if (data != null){
                    val contentUrl = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentUrl)
                        iv_select_img.setImageBitmap(selectedImageBitmap)

                        savedImagePath = saveImageToInternalStorage(selectedImageBitmap)
                        Log.i("img","$savedImagePath")

                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this,"image Loading error",Toast.LENGTH_LONG)
                    }
                }
            }else if(requestCode == CAMERA){
                val thumbnail : Bitmap = data!!.extras!!.get("data") as Bitmap
                iv_select_img.setImageBitmap(thumbnail)
                savedImagePath = saveImageToInternalStorage(thumbnail)
                Log.i("img","$savedImagePath")
            }else if (requestCode == PLACE_AUTO_COMPLETE_REQUEST_CODE){

                val place :Place = Autocomplete.getPlaceFromIntent(data!!)
                et_location.setText(place.address)
                mLatitude = place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun updateDateInView(){
        val dateFormat = "dd.mm.yyyy"
        val sdf = SimpleDateFormat(dateFormat,Locale.getDefault())
        et_date.setText(sdf.format(cal.time).toString())

    }

    private fun chooseImageFromGallery(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest>,token : PermissionToken) {
                showRationalDialogForPermission()
            }
        }).onSameThread().check();

    }

    private fun showRationalDialogForPermission(){
        AlertDialog.Builder(this).setMessage("This permission required for this app")
            .setPositiveButton("GO TO SETTING"){_,_ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data = uri
                    startActivity(intent)
                }catch (e:ActivityNotFoundException){
                    e.printStackTrace()
                }

            }.setNegativeButton("No"){ dialog,_ -> dialog.dismiss() }.show()
    }

    private fun choosePhotoFromCamera(){
        Dexter.withActivity(this).withPermissions(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
        ).withListener(object: MultiplePermissionsListener {
            override fun onPermissionsChecked(report : MultiplePermissionsReport?) {
                if (report!!.areAllPermissionsGranted()){
                    val galleryIntent = Intent( MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(galleryIntent, CAMERA)
                }
            }
            override fun onPermissionRationaleShouldBeShown(permissions : MutableList<PermissionRequest>,token : PermissionToken) {
                showRationalDialogForPermission()
            }
        }).onSameThread().check();
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap) :Uri{
        val contextWrapper = ContextWrapper(applicationContext)
        var file = contextWrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try {
            val stream :OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)

            stream.flush()
            stream.close()
        }catch (e : IOException){

        }
        return Uri.parse(file.absolutePath)
    }
}