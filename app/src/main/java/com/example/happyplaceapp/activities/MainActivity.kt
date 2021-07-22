package com.example.happyplaceapp.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaceapp.R
import com.example.happyplaceapp.adapters.HappyPlaceAdapter
import com.example.happyplaceapp.database.DatabaseHelper
import com.example.happyplaceapp.models.HappyPlaceModel
import com.example.happyplaceapp.utils.SwipeToDeleteCallback
import com.example.happyplaceapp.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object{
        private const val ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
        const val HAPPY_PLACE_DETAILS = "happyPlacesDetails"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(activity_main_tool_bar)
        val actionBar = supportActionBar

        actionBar?.title = "happy Place app"



        fab_add_happy_place.setOnClickListener {
            val intent = Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }

        getDataFromDb()

    }

    private fun getDataFromDb(){
        val dbHandler = DatabaseHelper(this)
        val dataList :ArrayList<HappyPlaceModel> = dbHandler.getHappyPlaces()

        if (dataList.size > 0){
            tv_no_data_available.visibility = View.GONE
            rv_happy_places_list.visibility = View.VISIBLE

            setupRecyclerView(dataList)

        }else{
            rv_happy_places_list.visibility = View.GONE
            tv_no_data_available.visibility = View.VISIBLE
        }
    }

    private fun setupRecyclerView(dataList : ArrayList<HappyPlaceModel>){
        val layoutManager = LinearLayoutManager(this)
        val adapter = HappyPlaceAdapter(this,dataList)
        rv_happy_places_list.setHasFixedSize(true)
        rv_happy_places_list.layoutManager = layoutManager
        rv_happy_places_list.adapter = adapter

        adapter.setOnClickListener(object:HappyPlaceAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,HappyPlaceDetailsActivity::class.java)
                intent.putExtra(HAPPY_PLACE_DETAILS,model)
                startActivity(intent)
            }
        })

        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlaceAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rv_happy_places_list.adapter as HappyPlaceAdapter
                adapter.notifyDeleteItem(viewHolder.adapterPosition)

                getDataFromDb()

            }
        }

        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK){
                getDataFromDb()
            }
        }
    }


}