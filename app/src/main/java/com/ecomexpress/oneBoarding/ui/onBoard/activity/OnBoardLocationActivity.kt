package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.CommonRequest
import com.ecomexpress.oneBoarding.data.model.CommonResponse
import com.ecomexpress.oneBoarding.data.model.onBoarding.DcLocation
import com.ecomexpress.oneBoarding.databinding.ActivityOnBoardLocationBinding
import com.ecomexpress.oneBoarding.databinding.AddressItemLayoutBinding
import com.ecomexpress.oneBoarding.ui.adapter.GenericBindingInterface
import com.ecomexpress.oneBoarding.ui.adapter.GenericListAdapter
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.OnBoardLocationViewModel
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import com.ecomexpress.oneBoarding.utils.comman.onError
import com.ecomexpress.oneBoarding.utils.comman.onLoading
import com.ecomexpress.oneBoarding.utils.comman.onSuccess
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

@AndroidEntryPoint
class OnBoardLocationActivity :
    BaseActivity<OnBoardLocationViewModel, ActivityOnBoardLocationBinding>(), View.OnClickListener {
    override fun getLayout(): Int = R.layout.activity_on_board_location
    override fun getViewModelClass(): Class<OnBoardLocationViewModel> =
        OnBoardLocationViewModel::class.java

    var list: ArrayList<DcLocation> = ArrayList()
    var listAllData: ArrayList<DcLocation> = ArrayList()
    private var pinCode: String = ""
    private var lat = 0.0
    private var lng = 0.0
    private lateinit var locationAdapter: GenericListAdapter<DcLocation, AddressItemLayoutBinding>
    lateinit var dcLocation: DcLocation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel?.getLocationForPinCode()
        search()
        collectData()

        setNavigator(
            mBinding.commonBg.imageView,
            null,
            mBinding.commonBg.view1,
            mBinding.commonBg.view3,
            null,
            mBinding.commonBg.tvRegister,
            null,
            null
        )

        //Hide help
        mBinding.commonBg.helpIv.visibility = View.GONE

        mBinding.continueAppCompatButton.setOnClickListener(this)
        mBinding.commonBg.helpIv.setOnClickListener(this)

        setLocationListener(this)

        mBinding.recyclerAddress.layoutManager = LinearLayoutManager(this)
        locationAdapter = GenericListAdapter(
            R.layout.address_item_layout, getRecyclerView()
        )
        mBinding.recyclerAddress.adapter = locationAdapter
        mBinding.commonBg.tempId.text = mViewModel!!.getTempId()

        analyticsToAllScreen("OnBoard_activity", this@OnBoardLocationActivity.localClassName)
    }

    override fun onResume() {
        super.onResume()
        if (!isLocationEnabled()) {
            openGPS()
        }
    }

    private fun callToService(text: String) {
        if (pinCode.isNotEmpty()) mViewModel!!.callToNearByDCLocations(
            CommonRequest(
                pin_code = text, user_lat = lat.toString(), user_lng = lng.toString()
            )
        )
    }


    private fun collectData() {
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel?.latitude?.collect {
                lat = it
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel?.longitude?.collect {
                lng = it
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel?.pinCode?.collect {
                if (pinCode != it) {
                    pinCode = it
                    mBinding.locationWrite.setText(pinCode)
                    callToService(pinCode)
                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel!!.commonDCLocationResponse.collect { it ->
                it.onError {
                    progressDialog(this@OnBoardLocationActivity).dismiss()
                    snackBar(it.message, false)
                }
                it.onLoading {
                    progressDialog(this@OnBoardLocationActivity).show()
                }
                it.onSuccess { response ->
                    val result = response as CommonResponse
                    if (result.success) {
                        if (!result.data!!.dc_location.isNullOrEmpty()) {
                            mBinding.recyclerAddress.visibility = View.VISIBLE

                            list.removeAll(list)
                            list.addAll(result.data.dc_location!!)
                            listAllData.clear()
                            listAllData.addAll(list)
                            setLocationsAndSearch(list)
                        }
                    } else {
                        mBinding.tvNoLocation.text = result.description
                        mBinding.tvNoLocation.visibility = View.VISIBLE
                    }
                    progressDialog(this@OnBoardLocationActivity).dismiss()

                }
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            mViewModel!!.commonCityUpdateResponse.collect { it ->
                it.onError {
                    progressDialog(this@OnBoardLocationActivity).dismiss()
                    showToast(it.message!!, false)
                }
                it.onLoading {
                    progressDialog(this@OnBoardLocationActivity).show()
                }
                it.onSuccess { response ->
                    val result = response as CommonResponse
                    if (result.success) {
                        showToast(result.description, true)
                        mViewModel!!.setIsDcLocation()
                        launchNewActivity<UploadDocumentsActivity>()
                    } else {
                        showToast(result.description, false)
                    }
                    progressDialog(this@OnBoardLocationActivity).dismiss()

                }
            }
        }
    }

    private fun setLocationsAndSearch(listLatest: List<DcLocation>) {
        locationAdapter.notifyItemRangeRemoved(0, locationAdapter.currentList.size - 1)
        locationAdapter.submitList(listLatest)
        locationAdapter.notifyDataSetChanged()
    }

    private fun getRecyclerView() =
        object : GenericBindingInterface<AddressItemLayoutBinding, DcLocation> {
            override fun bindData(
                binder: AddressItemLayoutBinding, model: DcLocation, position: Int
            ) {
                binder.tvKilometer.text = model.distance_from_user
                binder.firstText1.text = model.address_list
                if (model.isSelected) {
                    binder.constraintAddressList.setBackgroundResource(R.drawable.dc_location_background_colour)
                    binder.firstText1.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.white
                        )
                    )
                } else {
                    binder.constraintAddressList.setBackgroundResource(R.drawable.curved_rectangle_light_blue)
                    binder.firstText1.setTextColor(
                        ContextCompat.getColor(
                            applicationContext, R.color.light_blue
                        )
                    )
                }
                binder.constraintAddressList.setOnClickListener {
                    val customData = mapOf("Screen" to "OnBoardLocationActivity", "key1" to 8)
                    logButtonClick("constraintAddressList", customData)
                    list = resetDcValues(list)
                    list[position].isSelected = true
                    enabledContinueButton()
                    dcLocation = list[position]
                    mViewModel?.setDcAddressLocation(list[position].address_list)
                    mBinding.recyclerAddress.adapter?.notifyDataSetChanged()
                }
                binder.executePendingBindings()
            }
        }

    private fun resetDcValues(list: java.util.ArrayList<DcLocation>): ArrayList<DcLocation> {
        for (li in list) {
            li.isSelected = false
        }
        return list
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            mBinding.continueAppCompatButton.id -> {
                val customData = mapOf("Screen" to "OnBoardLocationActivity", "key1" to 9)
                logButtonClick("help_iv", customData)
                mViewModel!!.setCityUpdate(dcLocation)

            }
        }
    }

    private fun search() {
        mBinding.tvNoLocation.visibility = View.GONE
        mBinding.locationBtn.setOnClickListener {
            val customData = mapOf("Screen" to "OnBoardLocationActivity", "key1" to 10)
            logButtonClick("search_button", customData)
            mBinding.nearByLocation.visibility = View.GONE
            mBinding.search.visibility = View.VISIBLE
            mBinding.searchDivider.visibility = View.VISIBLE
            mBinding.closeSearch.visibility = View.VISIBLE
            mBinding.locationBtn.visibility = View.GONE
        }
        mBinding.closeSearch.setOnClickListener {
            mBinding.search.setText("")
            mBinding.nearByLocation.visibility = View.VISIBLE
            mBinding.search.visibility = View.GONE
            mBinding.searchDivider.visibility = View.GONE
            mBinding.closeSearch.visibility = View.GONE
            mBinding.locationBtn.visibility = View.VISIBLE
        }
        mBinding.locationWrite.setOnFocusChangeListener { _, b ->
            if (b) {
                mBinding.search.setText("")
                mBinding.nearByLocation.visibility = View.VISIBLE
                mBinding.search.visibility = View.GONE
                mBinding.searchDivider.visibility = View.GONE
                mBinding.closeSearch.visibility = View.GONE
                mBinding.locationBtn.visibility = View.VISIBLE
            }
        }
        mBinding.locationWrite.doOnTextChanged { text, start, before, count ->
            disableContinueButton()
            if (text!!.length == 6 && text.isNotEmpty()) {
                callToService(text.toString())
            } else {
                mBinding.recyclerAddress.visibility = View.GONE
                mBinding.tvNoLocation.visibility = View.GONE
            }
        }

        mBinding.search.addTextChangedListener {
            val newText = it.toString()
            val newlistData: ArrayList<DcLocation>
            if (newText.isEmpty()) {
                list.clear()
                list.addAll(listAllData)
                setLocationsAndSearch(list)
                mBinding.tvNoLocation.visibility = View.GONE
                disableContinueButton()
            } else {
                newlistData = ArrayList()
                for (i in list.indices) {
                    if (list[i].address_list.uppercase(Locale.getDefault()).contains(
                            newText.uppercase(Locale.getDefault())
                        ) || list[i].distance_from_user.contains(
                            newText
                        )
                    ) {
                        newlistData.add(list[i])
                    }
                }
                mBinding.tvNoLocation.visibility = View.GONE
                if (newlistData.size == 0) {
                    mBinding.tvNoLocation.text = resources.getString(R.string.no_location_found)
                    mBinding.tvNoLocation.visibility = View.VISIBLE
                } else {
                    mBinding.tvNoLocation.visibility = View.GONE
                }
                if (newlistData.size > 0) {
                    list.clear()
                    list = newlistData
                }
                if (list.size != listAllData.size) {
                    listAllData = resetDcValues(listAllData)
                }
                setLocationsAndSearch(newlistData)
            }
        }

    }

    private fun enabledContinueButton() {
        mBinding.continueAppCompatButton.isEnabled = true
        mBinding.continueAppCompatButton.setBackgroundResource((R.drawable.button))
        mBinding.continueAppCompatButton.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.white
            )
        )
    }

    private fun disableContinueButton() {
        mBinding.continueAppCompatButton.isEnabled = false
        mBinding.continueAppCompatButton.setBackgroundResource((R.drawable.disable_button))
        mBinding.continueAppCompatButton.setTextColor(
            ContextCompat.getColor(
                applicationContext, R.color.grey_A3
            )
        )
    }

    override fun onBackPressed() {
        super.onBackPressedDispatcher.onBackPressed()
        finishAffinity()
    }
}
