package com.example.happyplaceapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.happyplaceapp.R
import com.example.happyplaceapp.activities.AddHappyPlaceActivity
import com.example.happyplaceapp.activities.MainActivity
import com.example.happyplaceapp.database.DatabaseHelper
import com.example.happyplaceapp.models.HappyPlaceModel
import kotlinx.android.synthetic.main.item_happy_place.view.*

class HappyPlaceAdapter(val context: Context,private val itemList:ArrayList<HappyPlaceModel>) : RecyclerView.Adapter<HappyPlaceAdapter.ViewHolder>() {
    private var onClickListener : OnClickListener? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.civ_place_image
        val titleView = itemView.tv_title
        val descriptionView = itemView.tv_description
        val ll_item_row = itemView.ll_item_row
    }

    interface OnClickListener{
        fun onClick(position: Int, model : HappyPlaceModel )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_happy_place,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.titleView.text = itemList[position].title
        holder.descriptionView.text = itemList[position].description
        holder.imageView.setImageURI(Uri.parse(itemList[position].image))

        holder.ll_item_row.setOnClickListener {
            if (onClickListener != null){
                onClickListener?.onClick(position,itemList[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setOnClickListener(onClickListener: OnClickListener){
        this.onClickListener = onClickListener
    }

    fun notifyEditItem(activity : Activity, position : Int, requestCode : Int){
        val intent = Intent(context,AddHappyPlaceActivity::class.java)
        intent.putExtra(MainActivity.HAPPY_PLACE_DETAILS,itemList[position])
        activity.startActivityForResult(intent,requestCode)
        notifyItemChanged(position)
    }
    fun notifyDeleteItem(position :Int){
        val dbHandler = DatabaseHelper(context)
        val result = dbHandler.deleteHappyPlaces(itemList[position])

        if (result > 0){
            itemList.removeAt(position)
            notifyItemRemoved(position)
        }

    }
}