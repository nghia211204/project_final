package com.example.projectfinal.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.projectfinal.R
import com.example.projectfinal.api.Cast

class CastAdapter(
    private var castList: List<Cast>
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    class CastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val avatar: ImageView = view.findViewById(R.id.iv_cast_avatar)
        private val name: TextView = view.findViewById(R.id.tv_cast_name)

        fun bind(castMember: Cast) {
            name.text = castMember.name

            val imageUrl = "https://image.tmdb.org/t/p/w185${castMember.profilePath}"

            Glide.with(itemView.context)
                .load(imageUrl)
                .apply(RequestOptions.circleCropTransform())
                .placeholder(R.drawable.ic_person_placeholder)
                .error(R.drawable.ic_person_placeholder)
                .into(avatar)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast_member, parent, false)
        return CastViewHolder(view)
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(castList[position])
    }

    override fun getItemCount() = castList.size

    fun updateCast(newCastList: List<Cast>) {
        castList = newCastList
        notifyDataSetChanged()
    }
}