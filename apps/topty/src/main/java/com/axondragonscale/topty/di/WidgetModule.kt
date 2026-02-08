package com.axondragonscale.topty.di

import com.axondragonscale.topty.repository.WidgetRepository
import com.axondragonscale.topty.widget.common.WidgetStateProvider
import com.axondragonscale.topty.widget.single.SingleTokenConfigVM
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/**
 * Created by Ronak Harkhani on 29/12/24
 */

val WidgetModule = module {

    // Repository (in main repository package)
    singleOf(::WidgetRepository)

    // State Provider
    factoryOf(::WidgetStateProvider)

    // ViewModels
    viewModelOf(::SingleTokenConfigVM)
}
