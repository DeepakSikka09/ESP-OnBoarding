package com.ecomexpress.oneBoarding.data.model.onBoarding

data class ReferenceModel(
    var name: String,
    var mobile_number:String,
    var isNameError:Boolean = false,
    var isMobileError:Boolean = false,
    var nameError:String = "",
    var mobileError:String = ""
)
