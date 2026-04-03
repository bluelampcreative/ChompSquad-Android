package com.bluelampcreative.chompsquad.di

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module

@Module
@ComponentScan("com.bluelampcreative.chompsquad.data.scanner")
@Configuration
class ScannerModule
