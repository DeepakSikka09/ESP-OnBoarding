package com.ecomexpress.oneBoarding.ui.onBoard.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ecomexpress.oneBoarding.R
import com.ecomexpress.oneBoarding.data.model.onBoarding.LanguageListing
import com.ecomexpress.oneBoarding.databinding.ActivityChooseLanguageBinding
import com.ecomexpress.oneBoarding.databinding.ChooseLanguageBinding
import com.ecomexpress.oneBoarding.ui.adapter.GenericBindingInterface
import com.ecomexpress.oneBoarding.ui.adapter.GenericListAdapter
import com.ecomexpress.oneBoarding.ui.base.BaseActivity
import com.ecomexpress.oneBoarding.ui.onBoard.viewmodel.ChooseLanguageViewModel
import com.ecomexpress.oneBoarding.utils.comman.CommonUtils
import com.ecomexpress.oneBoarding.utils.comman.launchNewActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Locale

@AndroidEntryPoint
class ChooseLanguageActivity :
    BaseActivity<ChooseLanguageViewModel, ActivityChooseLanguageBinding>(), View.OnClickListener {

    private var selected: Boolean = false
    private var listData: ArrayList<LanguageListing> = ArrayList()
    private lateinit var chooseLanguageAdapter: GenericListAdapter<LanguageListing, ChooseLanguageBinding>
    var bundle: Bundle? = null
    var currentCheck = 0
    var selectedImageList = arrayListOf(
        R.drawable.english_icon,
        R.drawable.hindi_logo,
        R.drawable.tamil_logo,
        R.drawable.telugu_logo,
        R.drawable.gujarati_logo,
        R.drawable.punjabi_logo,
        R.drawable.odia_selected_logo,
        R.drawable.marathi_logo,
        R.drawable.bangla_selected_logo,
        R.drawable.malayalam_selected_logo
    )
    var unselectedImageList = arrayListOf(
        R.drawable.english_unselected_logo,
        R.drawable.hindi_unselected_logo,
        R.drawable.tamil_unselected_logo,
        R.drawable.telugu_unselected_logo,
        R.drawable.gujrati_unselected_logo,
        R.drawable.punjabi_unselected_logo,
        R.drawable.odia_unselected_logo,
        R.drawable.marathi_unselected_logo,
        R.drawable.bangla_unselected_logo,
        R.drawable.malayalam_unselected_logo
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.chooseLanguage = mViewModel
        mBinding.lifecycleOwner = this
        mViewModel!!.checkAccessState()
        setPopulateData()
        setRecyclerView()
        manageAccessState()
        manageCountingButton(true)
        analyticsToAllScreen("ChooseLanguageActivity", this@ChooseLanguageActivity.localClassName)
        mBinding.btnSelect.setOnClickListener(this)
        setDefaultLanguage("en")
        startCoroutine()
    }

    private fun manageAccessState() {
        lifecycleScope.launch {
            launch {
                mViewModel!!.activityLauncher.collect {
                    when (it) {
                        1 -> {
                            finishAffinity()
                            launchNewActivity<FinalStatusActivity>()
                        }

                        2 -> {
                            finishAffinity()
                            launchNewActivity<OnBoardLocationActivity> { }
                        }

                        3 -> {
                            finishAffinity()
                            launchNewActivity<UploadDocumentsActivity> { }
                        }
                    }
                }
            }
            launch {
                mViewModel!!.isUserLoggedIn.collect {
                    if (it) {
                        finishAffinity()
                        launchNewActivity<OnBoardLocationActivity>()
                    }
                }
            }
        }
    }

    private fun setPopulateData() {
        listData = arrayListOf(
            LanguageListing(
                description = getString(R.string.English),
                title = getString(R.string.English),
                imageId = R.drawable.english_icon,
                isSelected = true
            ), LanguageListing(
                description = getString(R.string.Hindi),
                title = getString(R.string.Hindi_convert),
                imageId = R.drawable.hindi_unselected_logo
            ), LanguageListing(
                description = getString(R.string.Tamil),
                title = getString(R.string.Tamil_convert),
                imageId = R.drawable.tamil_unselected_logo
            ), LanguageListing(
                description = getString(R.string.Telugu),
                title = getString(R.string.Telegu_convert),
                imageId = R.drawable.telugu_unselected_logo
            ), LanguageListing(
                description = getString(R.string.Gujarati),
                title = getString(R.string.Gujrati_convert),
                imageId = R.drawable.gujrati_unselected_logo
            ),
            LanguageListing(
                description = getString(R.string.Punjabi),
                title = getString(R.string.Punjabi_convert),
                imageId = R.drawable.punjabi_unselected_logo
            ),
            LanguageListing(
                description = getString(R.string.Odia),
                title = getString(R.string.Odia_convert),
                imageId = R.drawable.odia_unselected_logo
            ),
            LanguageListing(
                description = getString(R.string.Marathi),
                title = getString(R.string.Marathi_convert),
                imageId = R.drawable.marathi_unselected_logo
            ),
            LanguageListing(
                description = getString(R.string.Bangla),
                title = getString(R.string.Bangla_convert),
                imageId = R.drawable.bangla_unselected_logo
            ),
            LanguageListing(
                description = getString(R.string.Malayalam),
                title = getString(R.string.Malayalam_convert),
                imageId = R.drawable.malayalam_unselected_logo
            )
        )
    }

    private fun setRecyclerView() {
        chooseLanguageAdapter = GenericListAdapter(R.layout.choose_language, getRecyclerViewData())
        mBinding.recyclerChooseLang.layoutManager = LinearLayoutManager(this)
        mBinding.recyclerChooseLang.adapter = chooseLanguageAdapter
        chooseLanguageAdapter.submitList(listData)
    }

    private fun getRecyclerViewData() =
        object : GenericBindingInterface<ChooseLanguageBinding, LanguageListing> {
            override fun bindData(
                binder: ChooseLanguageBinding, model: LanguageListing, position: Int
            ) {
                if (model.isSelected) {
                    binder.tvLang1.setTextColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.white
                        )
                    )
                    binder.tvLang2.setTextColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.white
                        )
                    )
                    binder.cardLanguage.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.blue
                        )
                    )
                    binder.radioBtn.setImageResource(R.drawable.language_selected)
                    model.imageId = selectedImageList[position]

                } else {
                    binder.tvLang1.setTextColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.black
                        )
                    )
                    binder.tvLang2.setTextColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.black
                        )
                    )
                    binder.cardLanguage.setCardBackgroundColor(
                        ContextCompat.getColor(
                            this@ChooseLanguageActivity, R.color.light_sky
                        )
                    )
                    binder.radioBtn.setImageResource(R.drawable.language_unselect)
                    model.imageId = unselectedImageList[position]

                }

                binder.cardLanguage.setOnClickListener {
                    if (position == 0) {
                        listData[currentCheck].isSelected = false
                        listData[position].isSelected = true
                        chooseLanguageAdapter.notifyItemChanged(position)
                        chooseLanguageAdapter.notifyItemChanged(currentCheck)
                        currentCheck = position
                        manageCountingButton(true)
                    }
                }
                binder.tvLang1.text = model.title
                binder.tvLang2.text = model.description
                binder.ivLang.setImageResource(model.imageId!!)
                if (position == 0) {
                    binder.tvLang2.visibility = View.GONE
                } else {
                    binder.tvLang2.visibility = View.VISIBLE
                }
                binder.executePendingBindings()
            }
        }

    override fun getLayout(): Int = R.layout.activity_choose_language

    override fun getViewModelClass(): Class<ChooseLanguageViewModel> =
        ChooseLanguageViewModel::class.java


    fun manageCountingButton(select: Boolean) {
        selected = select
        mBinding.btnSelect.isEnabled = true
        mBinding.btnSelect.setBackgroundResource(R.drawable.button)
    }

    override fun onClick(p0: View?) {
        Log.e("id", "${mBinding.btnSelect.id} , ${p0!!.id}")
        when (p0.id) {
            mBinding.btnSelect.id -> {
                if (currentCheck == 0) {
                    setLanguage(Locale("en"))
                }
                if (currentCheck == 1) {
                    setLanguage(Locale("hi"))
                }
                val customData = mapOf("Screen" to "ChooseLanguageActivity", "Key1" to 1)
                logButtonClick("chooseLanguage_button", customData)
                if (selected) {
                    if (CommonUtils.isInternetAvailable(this)) {
                        launchNewActivity<LandingActivity>()
                    } else {
                        snackBar(getString(R.string.no_internet), false)
                    }
                } else {
                    snackBar(getString(R.string.please_select_one_lang), false)
                }
            }
        }
    }

    /**
     * Understanding Coroutine Execution Order
     */
    private fun startCoroutine() {
        val myScope: CoroutineScope = CoroutineScope(Job())

        myScope.launch {
            delay(500L)
            println("A")

            coroutineScope {
                launch {
                    delay(1000L)
                    println("B")
                }

                delay(100L)
                println("C")
            }

            println("D")
        }
    } // output : A,C,B,D


    /**
     * Handling Exceptions in Coroutines with SupervisorJob
     *  Output-
     *  child1
     * Exception caught
     * child2
     */
    fun main() = runBlocking {
        val exceptionHandler = CoroutineExceptionHandler { _, _ ->
            println("Exception caught")
        }

        val scope = CoroutineScope(SupervisorJob() + exceptionHandler)

        val job = scope.launch {
            val child1 = scope.launch {
                delay(500)
                println("child1")
                throw Exception()
            }

            val child2 = scope.launch {
                delay(1000)
                println("child2")
            }

            joinAll(child1, child2)
        }

        job.join()
    }

    /**
     *  Output -
     *  Bird
     * Cat
     * Dog
     * Fish
     */
    companion object {
        /**
         *   Coroutine Execution Order and the Role of job.join()
         */
        fun main() = runBlocking {
            val job = launch {
                delay(2000)
                println("Dog")
            }

            launch {
                delay(1000)
                println("Cat")
            }

            println("Bird")

            job.join()

            println("Fish")
        }
    }
}


