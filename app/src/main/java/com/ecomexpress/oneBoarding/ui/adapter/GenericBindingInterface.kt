package com.ecomexpress.oneBoarding.ui.adapter

import androidx.databinding.ViewDataBinding

interface GenericBindingInterface<VM : ViewDataBinding, T : Any> {
    fun bindData(binder: VM, model: T, position: Int)
}