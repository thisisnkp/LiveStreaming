package com.comman.gallerypicker.viewholders

import androidx.recyclerview.widget.RecyclerView
import com.comman.gallerypicker.databinding.CardPickerHeaderBinding

class TitleViewHolder(val binding: CardPickerHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setTitle(name: String) {
        binding.txtTitle.text = name
    }

}