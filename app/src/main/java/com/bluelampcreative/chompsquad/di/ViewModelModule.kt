package com.bluelampcreative.chompsquad.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [NetworkModule::class, DataModule::class, PurchasesModule::class])
@ComponentScan("com.bluelampcreative.chompsquad.feature")
class ViewModelModule
