package com.shamweel.example.sample

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.shamweel.multipleimageselect.MultipleImageSelectActivity
import com.shamweel.multipleimageselect.helpers.Constants
import com.shamweel.multipleimageselect.model.ImageData

class MainActivity : AppCompatActivity() {

    lateinit var txtSelectedImage: TextView
    lateinit var btnGallery: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtSelectedImage = findViewById(R.id.txt_selected_images)
        btnGallery = findViewById(R.id.btnGallery)

        btnGallery.setOnClickListener {
            val intent = Intent(this, MultipleImageSelectActivity::class.java)
            intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 10)
            intent.putExtra(Constants.INTENT_GET_URI, false)
            startActivityForResult(intent, Constants.REQUEST_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null){
            val selectedImagesData : ArrayList<ImageData> = data.getSerializableExtra(Constants.INTENT_EXTRA_IMAGES) as ArrayList<ImageData>
            var images : String? = ""

            for (img in selectedImagesData){
                images += "\n${img.Uri}"
            }
            images += "\nTotal Images Selected = ${selectedImagesData.size}"
            txtSelectedImage.text = images
        }
    }
}