package com.almalika.salon.di

import android.content.Context
import androidx.room.Room
import com.almalika.salon.data.local.SalonDatabase
import com.almalika.salon.data.local.dao.SalonConfigDao
import com.almalika.salon.data.remote.SalonApi
import com.almalika.salon.data.repository.SalonRepositoryImpl
import com.almalika.salon.domain.repository.SalonRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://er-md-hafiz.github.io/api.almalaikasalon/"

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideSalonApi(okHttpClient: OkHttpClient, moshi: Moshi): SalonApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SalonApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SalonDatabase {
        return Room.databaseBuilder(
            context,
            SalonDatabase::class.java,
            "salon_database.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideSalonConfigDao(database: SalonDatabase): SalonConfigDao {
        return database.salonConfigDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    private const val FIREBASE_RTDB_URL = "https://al-malika-salon-default-rtdb.firebaseio.com/"

    @Provides
    @Singleton
    fun provideFirebaseAuth(): com.google.firebase.auth.FirebaseAuth {
        return com.google.firebase.auth.FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): com.google.firebase.database.FirebaseDatabase {
        return com.google.firebase.database.FirebaseDatabase.getInstance(FIREBASE_RTDB_URL)
    }

    @Provides
    @Singleton
    fun provideUsersReference(
        database: com.google.firebase.database.FirebaseDatabase
    ): com.google.firebase.database.DatabaseReference {
        return database.getReference("users")
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSalonRepository(
        repositoryImpl: SalonRepositoryImpl
    ): SalonRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repositoryImpl: com.almalika.salon.data.repository.AuthRepositoryImpl
    ): com.almalika.salon.domain.repository.AuthRepository
}
