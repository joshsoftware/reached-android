package com.joshsoftware.reachedapp.di

import android.app.Application
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.joshsoftware.reachedapp.R
import dagger.Module
import dagger.Provides

@Module
class AppModule {

    @Provides
    fun provideGoogleSignInClient(application: Application): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(application.getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        return GoogleSignIn.getClient(application.applicationContext, gso)
    }


}