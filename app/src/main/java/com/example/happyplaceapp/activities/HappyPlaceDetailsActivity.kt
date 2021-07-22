package com.example.happyplaceapp.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaceapp.R
import com.example.happyplaceapp.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_happy_place_details.*

class HappyPlaceDetailsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_details)




        var happyPlaceModel : HappyPlaceModel? = null

        if (intent.hasExtra(MainActivity.HAPPY_PLACE_DETAILS)){
            happyPlaceModel = intent.getParcelableExtra(MainActivity.HAPPY_PLACE_DETAILS) as HappyPlaceModel?
            setSupportActionBar(activity_HappyPlace_details_tool_bar)
            val actionBar = supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
            activity_HappyPlace_details_tool_bar.setNavigationOnClickListener {
                onBackPressed()
            }
            actionBar?.title = happyPlaceModel!!.title
            iv_happy_place_details.setImageURI(Uri.parse(happyPlaceModel!!.image))
            tv_title_details.text = happyPlaceModel!!.title
            tv_description_details.text = happyPlaceModel!!.description

        }
    }
}