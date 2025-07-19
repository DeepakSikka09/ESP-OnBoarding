package com.ecomexpress.oneBoarding.ui.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ecomexpress.oneBoarding.data.model.onBoarding.ReferenceModel
import com.ecomexpress.oneBoarding.databinding.ReferenceItemBinding
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils

class ReferenceAdapter(
    private val references: ArrayList<ReferenceModel>,
    private val saveReference: (ArrayList<ReferenceModel>) -> Unit,
    private val lastSaveIndex: Int,
    private val hideKeyBoard: () -> Unit
) : RecyclerView.Adapter<ReferenceAdapter.ReferenceViewHolder>() {
    inner class ReferenceViewHolder(val binding: ReferenceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val reference = references[position]
            binding.etName.setText(reference.name)
            binding.etPhoneNumber.setText(reference.mobile_number)
            if(position<lastSaveIndex) {
                binding.etName.isEnabled = false
                binding.etPhoneNumber.isEnabled = false
                binding.tvClear.visibility = View.GONE

            }else{
                binding.etName.isEnabled = true
                binding.etPhoneNumber.isEnabled = true
            }
            if (position == references.size - 1) {
                binding.view.visibility = View.GONE
            } else {
                binding.view.visibility = View.VISIBLE
            }
            var positionReference = "${position + 1}"
            positionReference += when (position) {
                0 -> {
                    "st"
                }

                1 -> {
                    "nd"
                }

                2 -> {
                    "rd"
                }

                else -> {
                    "th"
                }
            }
            binding.referenceCount.text = "$positionReference Reference"
            val nameTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    references[position].name = p0.toString()

                    if (references[position].isNameError) {
                        references[position].isNameError = false
                        binding.tilReferenceName.isErrorEnabled = false
                        binding.nameErrorTv.visibility = View.GONE
                        binding.tvClear.visibility = View.GONE
                    }
                    saveReference(references)
                }

            }

            val mobileTextWatcher = object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun afterTextChanged(p0: Editable?) {
                    references[position].mobile_number = p0.toString()
                    if (p0.toString().length == 10) {
                        hideKeyBoard()
                    }
                    if (references[position].isMobileError) {
                        references[position].isMobileError = false
                        binding.phoneErrorTv.visibility = View.GONE
                        binding.tilPhoneNumber.isErrorEnabled = false
                        binding.tvClear.visibility = View.GONE
                    }
                    saveReference(references)
                }

            }
            if ((references[position].isNameError || references[position].isMobileError)&& position>=lastSaveIndex) {
                binding.tvClear.visibility = View.VISIBLE
            } else {
                binding.tvClear.visibility = View.GONE
            }

            if (references[position].isNameError && position>=lastSaveIndex) {
                binding.nameErrorTv.visibility = View.VISIBLE
                binding.tilReferenceName.isErrorEnabled = true
                binding.tilReferenceName.error = " "
                binding.nameErrorTv.text = reference.nameError
            } else {
                binding.tilReferenceName.isErrorEnabled = false
                binding.nameErrorTv.visibility = View.GONE
            }
            if (references[position].isMobileError && position>=lastSaveIndex) {
                binding.phoneErrorTv.visibility = View.VISIBLE
                binding.phoneErrorTv.text = references[position].mobileError
                binding.tilPhoneNumber.error = " "
                binding.tilPhoneNumber.isErrorEnabled = true

            } else {
                binding.phoneErrorTv.visibility = View.GONE
                binding.tilPhoneNumber.isErrorEnabled = false

            }
            binding.tvClear.setOnClickListener {
                references[position].name = ""
                references[position].mobile_number = ""
                references[position].isNameError = false
                references[position].isMobileError = false
                notifyItemChanged(position)
                saveReference(references)
            }

            binding.etName.addTextChangedListener(nameTextWatcher)
            binding.etPhoneNumber.addTextChangedListener(mobileTextWatcher)

            binding.etName.tag = nameTextWatcher
            binding.etPhoneNumber.tag = mobileTextWatcher
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReferenceViewHolder {
        val view =
            ReferenceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReferenceViewHolder(view)
    }

    override fun getItemCount(): Int {
        return references.size
    }

    override fun onBindViewHolder(holder: ReferenceViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onViewRecycled(holder: ReferenceViewHolder) {
        holder.binding.etName.removeTextChangedListener(holder.binding.etName.tag as TextWatcher)
        holder.binding.etPhoneNumber.removeTextChangedListener(holder.binding.etPhoneNumber.tag as TextWatcher)
        super.onViewRecycled(holder)
    }
}